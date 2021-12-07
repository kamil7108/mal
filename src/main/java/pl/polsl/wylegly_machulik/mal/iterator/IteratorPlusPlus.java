package pl.polsl.wylegly_machulik.mal.iterator;

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
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.util.StopWatch;

import lombok.Getter;
import pl.polsl.wylegly_machulik.mal.PageSupplier;
import pl.polsl.wylegly_machulik.mal.algorithm.PageFillingAlgorithm;
import pl.polsl.wylegly_machulik.mal.algorithm.RENEW;
import pl.polsl.wylegly_machulik.mal.mal.Aggregate;
import pl.polsl.wylegly_machulik.mal.mal.AggregatePage;
import pl.polsl.wylegly_machulik.mal.mal.MAL;
import pl.polsl.wylegly_machulik.mal.statistics.Statistics;

@Getter
public class IteratorPlusPlus {
    private final static Logger LOG = LoggerFactory.getLogger(IteratorPlusPlus.class);
    static int i = 0;
    private final MAL mal;
    private final PageFillingAlgorithm algorithm;
    private final PageSupplier supplier;
    private static final StopWatch recordWatch = new StopWatch();
    private static final StopWatch pageWatch = new StopWatch();
    private static final StopWatch malWatch = new StopWatch();
    private final IteratorMetadata metadata;
    private final Statistics statistics;
    private final LocalDateTime startTimestamp;
    private final Queue<Pair<CompletableFuture<Void>,Integer>> queue;

    public IteratorPlusPlus(LocalDateTime startTimestamp,
                            PageFillingAlgorithm algorithm,
                            PageSupplier supplier,
                            MAL mal,
                            Statistics statistics
    ) {
        this.startTimestamp = startTimestamp;
        this.algorithm = algorithm;
        this.supplier = supplier;
        this.statistics = statistics;
        this.metadata = new IteratorMetadata();
        this.mal = mal;
        queue = new ArrayDeque<>(2);
    }

    public void startWatch(){
        malWatch.start();
        pageWatch.start();
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
        recordWatch.start();
        Optional<Aggregate> result = Optional.empty();
        if (algorithm.malReadyForFilling(mal.pageSize, metadata))
        {
            fill();
        }
        if (algorithm.waitForResult(queue,metadata,mal.pageSize))
        {
            try
            {
                LOG.info("Waiting for result");
                queue.poll().getFirst().get();
                LOG.info("Got result");
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
        recordWatch.stop();
        var time = recordWatch.getLastTaskTimeNanos();
        CompletableFuture.supplyAsync(() -> saveSingleIterationTime(time));
        return result.get();
    }

    private Future<Void> saveSingleIterationTime(long time){
        statistics.saveSingleRecordTime(time);
        return new AsyncResult<>(null);
    }
    /**
     * Get aggregate
     *
     * @return
     */
    private Optional<Aggregate> getAggregate() {
        Optional<Aggregate> aggregate = mal.get(metadata.getCurrentPage(), metadata.getCurrentAggregate());
        return aggregate;
    }

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
        LOG.info("Start process.");
        var future = CompletableFuture.supplyAsync(
                () -> createAndFillNewPage(indexOfPageToBeReplaced, metadata.getCurrentMalIterationOfFilling()))//
                .thenAccept(result -> mal.replacePage(indexOfPageToBeReplaced, result));
        //var indexOfPageThatCanBeRead = algorithm.calculateIndexOfPageWhichCanBeRead(mal,metadata);
        queue.add(Pair.of(future,indexOfPageToBeReplaced));
        LOG.info("Good");
        return future;
    }

    private AggregatePage createAndFillNewPage(int indexOfPageToBeReplaced, int currentIteration)
    {
        //  LOG.info("Create and fill page indexOfPageToBeReplced={}, currentIteration={}, thread={}", indexOfPageToBeReplaced,
        //          currentIteration, Thread.currentThread().getId());
        return supplier.createSinglePage(mal.pageSize,
                getTimestampForNextPageToBeFilled(indexOfPageToBeReplaced, currentIteration));
    }

    private LocalDateTime getTimestampForNextPageToBeFilled(int indexOfPageToBeReplaced, int currentIteration)
    {
        var iterationValueTimeStamp = currentIteration * mal.pageSize * supplier.getAggregationWindowWidthMinutes() * mal.size;
        var currentPageValueTimeStamp = (indexOfPageToBeReplaced) * mal.pageSize * supplier.getAggregationWindowWidthMinutes();
        return startTimestamp.plusMinutes(currentPageValueTimeStamp + iterationValueTimeStamp);
    }

    private Future<Void> savePageTimeRecord(long time)
    {
        statistics.savePageRecordTime(time);
        return new AsyncResult<>(null);
    }

    private Future<Void> saveMalTimeRecord(long time)
    {
        statistics.saveMalRecordTime(time);
        return new AsyncResult<>(null);
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
                pageWatch.stop();
                CompletableFuture.supplyAsync(() -> savePageTimeRecord(pageWatch.getLastTaskTimeMillis()));
                pageWatch.start();
            }
            else
            {
                metadata.resetPageIndex();
                malWatch.stop();
                CompletableFuture.supplyAsync(() -> saveMalTimeRecord(malWatch.getLastTaskTimeMillis()));
                malWatch.start();
            }
        }
    }
}


