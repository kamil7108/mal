package pl.polsl.km.mal.algorithm;

import java.util.Queue;
import java.util.concurrent.CompletableFuture;

import org.springframework.data.util.Pair;

import pl.polsl.km.mal.iterator.CursorMetadata;
import pl.polsl.km.mal.mal.Page;

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
    public boolean next(int malPageSize, CursorMetadata cursorMetadata)
    {
        return malPageSize - 1 == cursorMetadata.getCurrentAggregate();
    }

    @Override
    public boolean waitForResult(final Queue<Pair<CompletableFuture<Page>, Integer>> queue, final CursorMetadata metadata,
            final int malPageSize)
    {
       return !queue.isEmpty() && queue.element().getSecond().equals(metadata.getCurrentPage()) && metadata.getCurrentAggregate() == 0;
    }

    @Override
    public String toString(){
        return "SPARE";
    }
}
