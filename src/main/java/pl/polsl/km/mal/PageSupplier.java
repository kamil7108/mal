package pl.polsl.km.mal;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.polsl.km.mal.data.SensorReadingRepository;
import pl.polsl.km.mal.mal.Aggregate;
import pl.polsl.km.mal.mal.AggregatePage;


public class PageSupplier
{
    private static final Logger LOG = LoggerFactory.getLogger(PageSupplier.class);

    private final SensorReadingRepository sensorReadingRepository;
    public final long aggregationWindowWidthMinutes;

    public long getAggregationWindowWidthMinutes()
    {
        return aggregationWindowWidthMinutes;
    }

    public PageSupplier(final SensorReadingRepository sensorReadingRepository,final long aggregationWindowWidthMinutes)
    {
        this.sensorReadingRepository = sensorReadingRepository;
        this.aggregationWindowWidthMinutes = aggregationWindowWidthMinutes;
    }

    /**
     * Create single MAL page, creating aggregates that
     * begin from given timestamp
     */
    public AggregatePage createSinglePage(int pageSize, LocalDateTime timestamp)
    {
        final AggregatePage aggregatePage = new AggregatePage();
        for (int i = 0; i < pageSize; i++)
        {
            final LocalDateTime startTimestamp = timestamp.plusMinutes(aggregationWindowWidthMinutes * i);
            final LocalDateTime endTimestamp = timestamp.plusMinutes(aggregationWindowWidthMinutes * (i + 1));
            //LOG.info("Create single page startTimestamp={}, endTimestamp={}, thread= {}", startTimestamp, endTimestamp,
          //          Thread.currentThread().getId());
            final List<Integer> sensorReadings = sensorReadingRepository.findAllByTimestampBetween(startTimestamp, endTimestamp);
            final Aggregate aggregate = createAggregate(sensorReadings, startTimestamp);
            aggregatePage.append(aggregate);
        }
        return aggregatePage;
    }

    /**
     * Collect sensor readings and build single aggregate
     */
    private Aggregate createAggregate(List<Integer> sensorReadings, LocalDateTime startTimestamp)
    {
        //   LOG.info("Create agrregate  startTimestamp={}, thread= {}", startTimestamp, Thread.currentThread().getId());
        return Aggregate.builder()//
                .waterLevelReadings(sensorReadings)//
                .startTimestamp(startTimestamp)//
                .endTimestamp(startTimestamp.plusMinutes(aggregationWindowWidthMinutes))//
                .isAllReadyRead(false)//
                .build();
    }

    /**
     * Asynchronously creates specified number of pages,
     * creating aggregates starting from given startTimestamp
     *
     */
    public List<AggregatePage> createPages(LocalDateTime startTimestamp, int pageSize,
            int numberOfPagesToBeCreated)
    {
        return IntStream.range(0, numberOfPagesToBeCreated).mapToObj(value -> createSinglePage(pageSize,
                startTimestamp.plusMinutes(value * pageSize * aggregationWindowWidthMinutes))).collect(
                Collectors.toCollection(LinkedList::new));
    }
}
