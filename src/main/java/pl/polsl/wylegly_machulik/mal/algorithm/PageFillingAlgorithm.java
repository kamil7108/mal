package pl.polsl.wylegly_machulik.mal.algorithm;

import java.util.Queue;
import java.util.concurrent.CompletableFuture;

import org.springframework.data.util.Pair;

import pl.polsl.wylegly_machulik.mal.iterator.IteratorMetadata;
import pl.polsl.wylegly_machulik.mal.mal.MAL;

public abstract class PageFillingAlgorithm {
    public abstract int numberOfPagesToBeFilledOnInitialization();

    /**
     * Based on current page, calculate number of next page to be filled
     */
    public abstract int numberOfNextPageToBeFilled(int currentPageNumber,int malSize);

    public abstract boolean malReadyForFilling(int malPageSize, IteratorMetadata iteratorMetadata);

    public abstract int calculateIndexOfPageWhichCanBeRead(MAL mal,IteratorMetadata metadata);

    public abstract boolean waitForResult(Queue<Pair<CompletableFuture<Void>,Integer>> queue,IteratorMetadata metadata,int malPageSize);
}