package pl.polsl.wylegly_machulik.mal.algorithm;

import java.util.Queue;
import java.util.concurrent.CompletableFuture;

import org.springframework.data.util.Pair;

import pl.polsl.wylegly_machulik.mal.iterator.IteratorMetadata;
import pl.polsl.wylegly_machulik.mal.mal.MAL;

public class SPARE extends PageFillingAlgorithm {
    public static final int NUMBER_OF_PAGES_FILLED_ON_INITIALIZATION = 2;

    @Override
    public int numberOfPagesToBeFilledOnInitialization() {
        return NUMBER_OF_PAGES_FILLED_ON_INITIALIZATION;
    }

    @Override
    public int numberOfNextPageToBeFilled(int currentPageNumber, int malSize) {
        if(currentPageNumber==malSize-2)
            return 0;
        if (currentPageNumber == malSize - 1)
        {
            return 1;
        }

        return currentPageNumber + 2;
    }

    @Override
    public boolean malReadyForFilling(int malPageSize, IteratorMetadata iteratorMetadata)
    {
        return malPageSize - 1 == iteratorMetadata.getCurrentAggregate();
    }

    @Override
    public int calculateIndexOfPageWhichCanBeRead(final MAL mal, final IteratorMetadata metadata)
    {
        var pageIndex = metadata.getCurrentPage() + 2 <= mal.size -1 ?  metadata.getCurrentPage() + 2 : 0;
        return mal.size - 1 == metadata.getCurrentPage() ? 0 : metadata.getCurrentPage() + 2;
    }

    @Override
    public boolean waitForResult(final Queue<Pair<CompletableFuture<Void>, Integer>> queue, final IteratorMetadata metadata,
            final int malPageSize)
    {
       return !queue.isEmpty() && queue.element().getSecond().equals(metadata.getCurrentPage())&& metadata.getCurrentAggregate() == 0;
    }

    @Override
    public String toString(){
        return "SPARE";
    }
}
