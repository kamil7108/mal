package pl.polsl.km.mal.iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.Getter;
import pl.polsl.km.mal.mal.Aggregate;
import pl.polsl.km.mal.mal.MAL;
import pl.polsl.km.mal.statistics.Statistics;

@Getter
public class MalIterator extends IteratorStatistic
{
    private final static Logger LOG = LoggerFactory.getLogger(MalIterator.class);
    private final MAL mal;
    private final IteratorMetadata metadata;
    private int i = 0;

    public MalIterator(MAL mal, Statistics statistics)
    {
        super(statistics);
        this.metadata = new IteratorMetadata();
        this.mal = mal;
    }

    public void initializeMalData() {
        mal.initializMalData();
    }

    /**
     * Retrieve next aggregate in the MAL
     *
     * @return next aggregate
     */
    public Aggregate next()
    {
        var result = mal.get(metadata);
        LOG.info("# {} Retrieved aggregate: {} Aggregate {} Page {}", i++, result, metadata.getCurrentAggregate(),
                metadata.getCurrentPage());
        metadata.nextAggregate();
        validateIndexes();
        return result;
    }

    /**
     * Method responsible for incrementing appropriate indexes,
     * depending on the index of last retrieved aggregate
     */
    private void validateIndexes()
    {
        if (metadata.getCurrentAggregate() == mal.pageSize)
        {
            if (metadata.getCurrentPage() != mal.size - 1)
            {
                metadata.nextPage();
            }
            else
            {
                metadata.resetPageIndex();
            }
        }
    }
}