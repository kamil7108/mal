package pl.polsl.wylegly_machulik.mal.iterator;

import lombok.Getter;

@Getter
public class IteratorMetadata {
    private int currentPage = 0;
    private int currentAggregate = 0;
    /**
     * Now it indicates current mal iteration of filling
     * It is depend with  PageFillingAlgoritm.numberOfNextPageToBeFilled
     */
    private int currentMalIterationOfFilling = 0;

    public void nextAggregate() {
        this.currentAggregate++;
    }

    public void nextPage() {
        this.currentPage++;
        this.currentAggregate = 0;
    }

    public void nextMalIteration() {
        this.currentMalIterationOfFilling++;
    }

    public void resetPageIndex() {
        this.currentPage = 0;
        this.currentAggregate = 0;
    }
}
