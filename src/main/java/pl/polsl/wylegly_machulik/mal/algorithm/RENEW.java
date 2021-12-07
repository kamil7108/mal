package pl.polsl.wylegly_machulik.mal.algorithm;

import java.util.Queue;
import java.util.concurrent.CompletableFuture;

import org.springframework.data.util.Pair;

import pl.polsl.wylegly_machulik.mal.iterator.IteratorMetadata;
import pl.polsl.wylegly_machulik.mal.mal.MAL;


public class RENEW extends PageFillingAlgorithm{
    @Override
    public int numberOfPagesToBeFilledOnInitialization() {
        /**
         * Not use
         */
        return 0;
    }

    @Override
    public int numberOfNextPageToBeFilled(int currentPageNumber, int malSize) {
        return currentPageNumber;
    }

    /**
     * This algorithm do one unnecessary filling (Iteration: 0 end of 0 page)
     * @param
     * @param iteratorMetadata
     * @return
     */
    @Override
    public boolean malReadyForFilling(int malPageSize, IteratorMetadata iteratorMetadata) {
        return malPageSize - 1 == iteratorMetadata.getCurrentAggregate();
    }

    @Override
    public int calculateIndexOfPageWhichCanBeRead(final MAL mal, final IteratorMetadata metadata)
    {
        return metadata.getCurrentPage();
    }

    @Override
    public boolean waitForResult(final Queue<Pair<CompletableFuture<Void>, Integer>> queue, final IteratorMetadata metadata,
            final int malPageSize)
    {
        return !queue.isEmpty() && queue.element().getSecond().equals(metadata.getCurrentPage())&& metadata.getCurrentAggregate() == 0;
    }

    @Override
    public String toString(){
        return "RENEW";
    }
}
