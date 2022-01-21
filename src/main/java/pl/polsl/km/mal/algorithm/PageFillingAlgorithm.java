package pl.polsl.km.mal.algorithm;

import java.util.Queue;
import java.util.concurrent.CompletableFuture;

import org.springframework.data.util.Pair;

import pl.polsl.km.mal.iterator.IteratorMetadata;
import pl.polsl.km.mal.mal.AggregatePage;

public abstract class PageFillingAlgorithm {
    public abstract int numberOfPagesToBeFilledOnInitialization();

    /**
     * Based on current page, calculate number of next page to be filled
     */
    public abstract int numberOfNextPageToBeFilled(int currentPageNumber,int malSize);

    public abstract boolean next(int malPageSize, IteratorMetadata iteratorMetadata);

    public abstract boolean waitForResult(Queue<Pair<CompletableFuture<AggregatePage>,Integer>> queue,IteratorMetadata metadata,int malPageSize);
}