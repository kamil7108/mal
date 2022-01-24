package pl.polsl.km.mal.algorithm;

import java.util.Queue;
import java.util.concurrent.CompletableFuture;

import org.springframework.data.util.Pair;

import pl.polsl.km.mal.iterator.CursorMetadata;
import pl.polsl.km.mal.mal.Page;

/**
 * Representation of 1page filing algorithm
 */
public abstract class PageFillingAlgorithm
{
    public abstract int numberOfPagesToBeFilledOnInitialization();

    /**
     * Based on current page, calculate number of next page to be filled
     */
    public abstract int numberOfNextPageToBeFilled(int currentPageNumber, int malSize);

    /**
     * Based on cursor position check if new process of page filing should be started
     *
     * @param malPageSize - page size
     * @param cursorMetadata - cursor metadata
     * @return true if new process should be started otherwise false
     */
    public abstract boolean next(int malPageSize, CursorMetadata cursorMetadata);

    /**
     * Check if iterator must wait for a result of page filling
     *
     * @param queue - queue of reference of future and index of page to be replaced
     * @param metadata - cursor metadata
     * @param malPageSize - page size
     * @return true if iterator must wait for future result
     */
    public abstract boolean waitForResult(Queue<Pair<CompletableFuture<Page>, Integer>> queue, CursorMetadata metadata,
            int malPageSize);
}