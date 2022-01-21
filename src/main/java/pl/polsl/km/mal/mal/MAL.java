package pl.polsl.km.mal.mal;

import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.util.Pair;

import lombok.Getter;
import pl.polsl.km.mal.algorithm.PageFillingAlgorithm;
import pl.polsl.km.mal.algorithm.RENEW;
import pl.polsl.km.mal.iterator.IteratorMetadata;
import pl.polsl.km.mal.services.AggregateSupplierService;
@Getter
public class MAL {
    private final static Logger LOG = LoggerFactory.getLogger(MAL.class);
    public final int pageSize;
    public final int size;
    private final PageFillingAlgorithm algorithm;
    private final AggregateSupplierService supplier;
    private final LocalDateTime startTimestamp;
    private final Queue<Pair<CompletableFuture<AggregatePage>, Integer>> queue;

    private List<AggregatePage> pages;

    public MAL(int pageSize, int size,LocalDateTime startTimestamp, PageFillingAlgorithm algorithm, AggregateSupplierService supplier) {
        this.pageSize = pageSize;
        this.size = size;
        /*
        * Ensure that mal is initialized with empty pages, so they can be replaced in first iteration
        * */
        pages = new ArrayList<>(size);
        pages.addAll(Collections.nCopies(size, new AggregatePage()));
        this.startTimestamp = startTimestamp;
        this.algorithm = algorithm;
        this.supplier = supplier;
        queue = new ArrayDeque<>();
    }

    /**
     * Returns next aggregate if it exists otherwise return Optional.Empty()
     *
     * @return
     */
    public Aggregate get(final IteratorMetadata metadata)
    {
        Optional<Aggregate> result = Optional.empty();
        if (algorithm.waitForResult(queue,metadata,pageSize))
        {
            try
            {
                LOG.info("Waiting for result of filling mal page.");
                var thread = queue.poll();
                var page = thread.getFirst().get();
                replacePage(thread.getSecond(), page);
                LOG.info("Got result of filling mal page.");
            }
            catch (Exception e)
            {
                LOG.error("Error occurred while waiting for result. E ={}", e.getMessage());
            }
        }
        if (algorithm.next(pageSize, metadata))
        {
            fill(metadata);
        }
        while (result.isEmpty())
        {
            result = getPage(metadata.getCurrentPage()).flatMap(aggregatePage -> aggregatePage.get(metadata.getCurrentAggregate()));
        }
        return result.get();
    }

    public void replacePages(List<AggregatePage> newPages)
    {
        for (int i = 0; i < newPages.size(); i++)
        {
            var page = newPages.get(i);
            pages.remove(i);
            pages.add(i, page);
        }
    }

    public void replacePage(int pageNumber,AggregatePage newPage)
    {
        pages.remove(pageNumber);
        pages.add(pageNumber, newPage);
    }

    public Optional<AggregatePage> getPage(int pageNumber)
    {
        if (pages.size() > pageNumber)
        {
            try
            {
                return Optional.of(pages.get(pageNumber));
            }
            catch (Exception e)
            {
                return Optional.empty();
            }
        }
        else
        {
            return Optional.empty();
        }
    }

    public void initializMalData() {
        try
        {
            //measure time of mal init
            List<AggregatePage> initialAggregatePages = retrieveInitialPages();
            LOG.info("Waiting for init mal pages.Pages to fill = {}.", initialAggregatePages.size());
            this.replacePages(initialAggregatePages);
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
            numberOfPagesToBeFilledOnInitialization = size;
        }
        int finalNumberOfPagesToBeFilledOnInitialization = numberOfPagesToBeFilledOnInitialization;
        return supplier.createPages(startTimestamp, pageSize, finalNumberOfPagesToBeFilledOnInitialization);
    }

    /**
     * Method starts filling the mal page.
     *
     * @return Future promise
     */
    private void fill(final IteratorMetadata metadata)
    {
        int indexOfPageToBeReplaced = algorithm.numberOfNextPageToBeFilled(metadata.getCurrentPage(), size);
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
        final var iteration = metadata.getCurrentMalIterationOfFilling();
        var future = CompletableFuture//
                .supplyAsync(() -> createAndFillNewPage(indexOfPageToBeReplaced, iteration));
        queue.add(Pair.of(future, indexOfPageToBeReplaced));
    }

    private AggregatePage createAndFillNewPage(int indexOfPageToBeReplaced, int currentIteration)
    {
        var timestamp = getTimestampForNextPageToBeFilled(indexOfPageToBeReplaced, currentIteration);
        return supplier.createSinglePage(pageSize, timestamp);
    }

    private LocalDateTime getTimestampForNextPageToBeFilled(int indexOfPageToBeReplaced, int currentIteration)
    {
        var iterationValueTimeStamp = currentIteration * pageSize * supplier.getAggregationWindowWidthMinutes() * size;//2*25*30*10=500*30=15000=7500
        var currentPageValueTimeStamp = (indexOfPageToBeReplaced) * pageSize * supplier.getAggregationWindowWidthMinutes();
        var total=currentPageValueTimeStamp + iterationValueTimeStamp;//7*25*30=7*750
        return startTimestamp.plusMinutes(total);
    }

    private LocalDateTime renew(int indexOfPageToBeReplaced, int currentIteration)
    {
        var iterationValueTimeStamp = currentIteration * pageSize *size *supplier.getAggregationWindowWidthMinutes();//2*25*30*10=500*30=15000
        var currentPageValueTimeStamp = (indexOfPageToBeReplaced) * pageSize * supplier.getAggregationWindowWidthMinutes();//7*25*30
        return startTimestamp.plusMinutes(currentPageValueTimeStamp + iterationValueTimeStamp);
    }

}
