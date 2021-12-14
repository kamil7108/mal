package pl.polsl.km.mal.iterator;

import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.util.Pair;

import lombok.Getter;
import pl.polsl.km.mal.PageSupplier;
import pl.polsl.km.mal.algorithm.PageFillingAlgorithm;
import pl.polsl.km.mal.algorithm.RENEW;
import pl.polsl.km.mal.mal.Aggregate;
import pl.polsl.km.mal.mal.AggregatePage;
import pl.polsl.km.mal.mal.MAL;
import pl.polsl.km.mal.statistics.Statistics;

@Getter
public class Iterator extends IteratorStatistic
{
    private final static Logger LOG = LoggerFactory.getLogger(Iterator.class);

    private final Queue<Pair<CompletableFuture<Void>, Integer>> queue;
    private final MAL mal;
    private final PageFillingAlgorithm algorithm;
    private final PageSupplier supplier;
    private final IteratorMetadata metadata;
    private final LocalDateTime startTimestamp;
    private int i = 0;

    public Iterator(LocalDateTime startTimestamp, PageFillingAlgorithm algorithm, PageSupplier supplier, MAL mal,
            Statistics statistics)
    {
        super(statistics);
        this.startTimestamp = startTimestamp;
        this.algorithm = algorithm;
        this.supplier = supplier;
        this.metadata = new IteratorMetadata();
        this.mal = mal;
        queue = new ArrayDeque<>(2);
    }

    public void initializeMalData() {
        try
        {
            //measure time of mal init
            List<AggregatePage> initialAggregatePages = retrieveInitialPages();
            LOG.info("Waiting for init mal pages.Pages to fill = {} Time of init = {}.", initialAggregatePages.size(), "time");
            mal.replacePages(initialAggregatePages);
        }
        catch (Exception e)
        {
            LOG.info("Error occurred while initializeMal. Error {}", e.getMessage());
        }
    }

    /**
     * Inits mal pages depending of filling algorithm
     * @return
     */
    private List<AggregatePage> retrieveInitialPages()
    {
        var numberOfPagesToBeFilledOnInitialization = algorithm.numberOfPagesToBeFilledOnInitialization();
        if (algorithm instanceof RENEW)
        {
            numberOfPagesToBeFilledOnInitialization = mal.size;
        }
        int finalNumberOfPagesToBeFilledOnInitialization = numberOfPagesToBeFilledOnInitialization;
        return supplier.createPages(startTimestamp, mal.pageSize, finalNumberOfPagesToBeFilledOnInitialization);
    }

    /**
     * Retrieve next aggregate in the MAL
     *
     * @return next aggregate
     */
    public Aggregate next()
    {
        Optional<Aggregate> result = Optional.empty();
        if (algorithm.malReadyForFilling(mal.pageSize, metadata))
        {
            fill();
        }
        if (algorithm.waitForResult(queue,metadata,mal.pageSize))
        {
            try
            {
                LOG.info("Waiting for result of filling mal page.");
                queue.poll().getFirst().get();
                LOG.info("Got result of filling mal page.");
            }
            catch (Exception e)
            {
                LOG.error("Error occurred while waiting for result.E ={}", e.getMessage());
            }
        }
        while (result.isEmpty())
        {
            result = getAggregate();
        }
        LOG.info("# {} Retrieved aggregate: {} Aggregate {} Page {}", i++, result.get(), metadata.getCurrentAggregate(),
                metadata.getCurrentPage());
        metadata.nextAggregate();
        validateIndexes();
        result.get().setIsAllReadyRead(true);
        return result.get();
    }

    /**
     * Get aggregate
     *
     * @return Optional.of(Aggregate.class)
     */
    private Optional<Aggregate> getAggregate()
    {
        return mal.get(metadata.getCurrentPage(), metadata.getCurrentAggregate());
    }

    /**
     * Method starts filling the mal page.
     *
     * @return Future promise
     */
    private Future<Void> fill()
    {
        int indexOfPageToBeReplaced = algorithm.numberOfNextPageToBeFilled(metadata.getCurrentPage(), mal.size);
        /**
         * In our case we always want to have one page back ups so f.e. if we are ending read of 3-rd page, want to will
         * 5-th page
         * If f.e mal.size is 5 and currentPage is 3 we want to start revert mal with new pages.
         */
        if (indexOfPageToBeReplaced == 0)
        {
            metadata.nextMalIteration();
        }
        LOG.info("Start process of filling.");
        var future = CompletableFuture.supplyAsync(() -> createAndFillNewPage(indexOfPageToBeReplaced, metadata.getCurrentMalIterationOfFilling()))//
                .thenAccept(result -> mal.replacePage(indexOfPageToBeReplaced, result));
        //var indexOfPageThatCanBeRead = algorithm.calculateIndexOfPageWhichCanBeRead(mal,metadata);
        queue.add(Pair.of(future,indexOfPageToBeReplaced));
        return future;
    }

    private AggregatePage createAndFillNewPage(int indexOfPageToBeReplaced, int currentIteration)
    {
        return supplier.createSinglePage(mal.pageSize,
                getTimestampForNextPageToBeFilled(indexOfPageToBeReplaced, currentIteration));
    }

    private LocalDateTime getTimestampForNextPageToBeFilled(int indexOfPageToBeReplaced, int currentIteration)
    {
        var iterationValueTimeStamp = currentIteration * mal.pageSize * supplier.getAggregationWindowWidthMinutes() * mal.size;
        var currentPageValueTimeStamp = (indexOfPageToBeReplaced) * mal.pageSize * supplier.getAggregationWindowWidthMinutes();
        return startTimestamp.plusMinutes(currentPageValueTimeStamp + iterationValueTimeStamp);
    }

    /**
     * Method responsible for incrementing appropriate indexes,
     * depending on the index of last retrieved aggregate
     */
    private void validateIndexes()
    {
        if (metadata.getCurrentAggregate() == mal.pageSize)
        {
            if (metadata.getCurrentPage() != mal.size - 1)
            {
                metadata.nextPage();
            }
            else
            {
                metadata.resetPageIndex();
            }
        }
    }
}