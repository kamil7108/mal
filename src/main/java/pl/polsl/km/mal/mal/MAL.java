package pl.polsl.km.mal.mal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MAL {
    private final static Logger LOG = LoggerFactory.getLogger(MAL.class);
    public final int pageSize;
    public final int size;

    private List<AggregatePage> pages;

    public MAL(int pageSize, int size) {
        this.pageSize = pageSize;
        this.size = size;
        /*
        * Ensure that mal is initialized with empty pages, so they can be replaced in first iteration
        * */
        pages = new ArrayList<>();
        pages.addAll(Collections.nCopies(size, new AggregatePage()));
    }

    /**
     * Returns next aggregate if it exists otherwise return Optional.Empty()
     *
     * @param pageNumber
     * @param aggregateNumber
     * @return
     */
    public Optional<Aggregate> get(int pageNumber, int aggregateNumber)
    {
        return getPage(pageNumber).flatMap(aggregatePage -> aggregatePage.get(aggregateNumber));
    }

    public void replacePages(List<AggregatePage> newPages)
    {
        pages.addAll(0, newPages);
        IntStream.range(0, newPages.size()).forEach(i -> pages.remove(i + newPages.size()));
    }

    public void replacePage(int pageNumber,AggregatePage newPage)
    {
            pages.add(pageNumber, newPage);
            pages.remove(pageNumber + 1);

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
}
