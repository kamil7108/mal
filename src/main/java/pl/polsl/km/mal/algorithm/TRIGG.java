package pl.polsl.km.mal.algorithm;

import java.util.Queue;
import java.util.concurrent.CompletableFuture;

import org.springframework.data.util.Pair;

import pl.polsl.km.mal.iterator.IteratorMetadata;
import pl.polsl.km.mal.mal.AggregatePage;

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
    public boolean next(int malPageSize, IteratorMetadata iteratorMetadata) {
        return malPageSize - 2 == iteratorMetadata.getCurrentAggregate();
    }

    @Override
    public boolean waitForResult(final Queue<Pair<CompletableFuture<AggregatePage>, Integer>> queue, final IteratorMetadata metadata,
            final int malPageSize)
    {
        return !queue.isEmpty() && queue.element().getSecond().equals(metadata.getCurrentPage())&& metadata.getCurrentAggregate() == 0;
    }

    @Override
    public String toString(){
        return "TRIGG";
    }
}
