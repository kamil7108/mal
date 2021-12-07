package pl.polsl.wylegly_machulik.mal.algorithm;

import java.util.Queue;
import java.util.concurrent.CompletableFuture;

import org.springframework.data.util.Pair;

import pl.polsl.wylegly_machulik.mal.iterator.IteratorMetadata;
import pl.polsl.wylegly_machulik.mal.mal.MAL;

public class TRIGG extends PageFillingAlgorithm{
    @Override
    public int numberOfPagesToBeFilledOnInitialization() {
        return 1;
    }

    @Override
    public int numberOfNextPageToBeFilled(int currentPageNumber, int malSize) {
        return currentPageNumber == malSize -1  ? 0 : ++currentPageNumber ;
    }

    @Override
    public boolean malReadyForFilling(int malPageSize, IteratorMetadata iteratorMetadata) {
        return malPageSize - 2 == iteratorMetadata.getCurrentAggregate();
    }

    @Override
    public int calculateIndexOfPageWhichCanBeRead(final MAL mal, final IteratorMetadata metadata)
    {
        return metadata.getCurrentPage()+1<=mal.size-1 ?  metadata.getCurrentPage()+1: 0;
    }

    @Override
    public boolean waitForResult(final Queue<Pair<CompletableFuture<Void>, Integer>> queue, final IteratorMetadata metadata,
            final int malPageSize)
    {
        return !queue.isEmpty() && queue.element().getSecond().equals(metadata.getCurrentPage())&& metadata.getCurrentAggregate() == 0;
    }

    @Override
    public String toString(){
        return "TRIGG";
    }
}
