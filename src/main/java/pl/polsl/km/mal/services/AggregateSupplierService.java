package pl.polsl.km.mal.services;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.polsl.km.mal.mal.Aggregate;
import pl.polsl.km.mal.mal.AggregatePage;
import pl.polsl.km.mal.testData.data.MaterializedAggregate;
import pl.polsl.km.mal.testData.data.SensorReading;
import pl.polsl.km.mal.testData.data.StreamDatabaseVariable;
import pl.polsl.km.mal.testData.repository.MaterializedAggregateRepository;
import pl.polsl.km.mal.testData.repository.ProjectionSensorReading;
import pl.polsl.km.mal.testData.repository.SensorReadingRepository;

public class AggregateSupplierService
{
    private static final Logger LOG = LoggerFactory.getLogger(AggregateSupplierService.class);

    private final SensorReadingRepository sensorReadingRepository;
    private final MaterializedAggregateRepository materializedAggregateRepository;
    public final long aggregationWindowWidthMinutes;

    public long getAggregationWindowWidthMinutes()
    {
        return aggregationWindowWidthMinutes;
    }

    public AggregateSupplierService(final SensorReadingRepository sensorReadingRepository,
            final MaterializedAggregateRepository materializedAggregateRepository, final long aggregationWindowWidthMinutes)
    {
        this.sensorReadingRepository = sensorReadingRepository;
        this.materializedAggregateRepository = materializedAggregateRepository;
        this.aggregationWindowWidthMinutes = aggregationWindowWidthMinutes;
    }

    /**
     * Create single MAL page, creating aggregates that
     * begin from given timestamp
     */
    public AggregatePage createSinglePageStream(int pageSize, LocalDateTime timestamp)
    {
        final AggregatePage aggregatePage = new AggregatePage();
        List<MaterializedAggregate> materializedAggregates = new LinkedList<>();
        final List<UUID> idsToDelete = new LinkedList<>();
        for (int i = 0; i < pageSize; i++)
        {
            final LocalDateTime startTimestamp = timestamp.plusMinutes(aggregationWindowWidthMinutes * i);
            final LocalDateTime endTimestamp = timestamp.plusMinutes(aggregationWindowWidthMinutes * (i + 1));
            final Optional<List<MaterializedAggregate>> materializedAggregate = materializedAggregateRepository//
                    .findByStartTimestampAndEndTimestamp(startTimestamp, endTimestamp);
            if (materializedAggregate.isPresent())
            {
                var result = materializedAggregate.get();
                result.forEach(r -> aggregatePage.append(createAggregate(r.getWaterLevelReadings(), r.getStartTimestamp())));
            }
            else
            {
                final List<ProjectionSensorReading> projectionSensorReadings = sensorReadingRepository//
                        .findAllByTimestampBetween(startTimestamp, endTimestamp);
                if (projectionSensorReadings.isEmpty())
                {
                    i--;
                    continue;
                }
                Integer waterLevel = 0;
                for (ProjectionSensorReading pr : projectionSensorReadings)
                {
                    waterLevel += pr.getWaterLevel();
                    idsToDelete.add(pr.getUuid());
                }
                final Aggregate aggregate = createAggregate(waterLevel, startTimestamp);
                aggregatePage.append(aggregate);

                var materializedAggregateToSave = new MaterializedAggregate(UUID.randomUUID(), waterLevel,
                        aggregate.getStartTimestamp(), aggregate.getEndTimestamp());
                materializedAggregates.add(materializedAggregateToSave);
            }
        }
        if (StreamDatabaseVariable.isStream())
        {
            deleteAlreadyReadRecords(idsToDelete);
        }
        return aggregatePage;
    }

    public AggregatePage createSinglePage(int pageSize, LocalDateTime timestamp)
    {
        final AggregatePage aggregatePage = new AggregatePage();
        List<MaterializedAggregate> materializedAggregates = new LinkedList<>();


        LocalDateTime startTimestamp = timestamp;
        LocalDateTime endTimestamp = timestamp.plusMinutes(aggregationWindowWidthMinutes * (pageSize));
        final List<MaterializedAggregate> materializedAggregate = materializedAggregateRepository//
                .getAllBetweenDates(startTimestamp, endTimestamp).get();
        if (materializedAggregate.size() == pageSize)
        {
            materializedAggregate.forEach(
                    r -> aggregatePage.append(createAggregate(r.getWaterLevelReadings(), r.getStartTimestamp())));
        }
        else
        {
            List<SensorReading> sensorReadings = sensorReadingRepository.findSensorReadingByTimestampBetween(startTimestamp,
                    endTimestamp);
            var tempDate = startTimestamp;
            for (int a = 0; a < pageSize; a++)
            {
                final var copyTempDateStart = tempDate;
                final var copyTempDateEnd = tempDate.plusMinutes(aggregationWindowWidthMinutes);
                var result = sensorReadings.stream()//
                        .filter(sensorReading -> !sensorReading.getTimestamp().isBefore(copyTempDateStart) && !sensorReading
                                .getTimestamp().isAfter(copyTempDateEnd))//
                        .map(SensorReading::getWaterLevel)//
                        .mapToInt(i -> i).sum();

                final Aggregate aggregate = createAggregate(result, tempDate);
                aggregatePage.append(aggregate);

                var materializedAggregateToSave = new MaterializedAggregate(UUID.randomUUID(), result,
                        aggregate.getStartTimestamp(), aggregate.getEndTimestamp());
                materializedAggregates.add(materializedAggregateToSave);
                tempDate = tempDate.plusMinutes(aggregationWindowWidthMinutes);
            }
        }
        if (materializedAggregates.size() > 0)
        {
            CompletableFuture.runAsync(() -> materializedAggregateRepository.saveAllAndFlush(materializedAggregates));
        }
        return aggregatePage;
    }

    /**
     * Collect sensor readings and build single aggregate
     */
    private Aggregate createAggregate(Integer sumOfWaterLevels, LocalDateTime startTimestamp)
    {
        return Aggregate.builder()//
                .sumOfWaterLevelReadings(sumOfWaterLevels)//
                .startTimestamp(startTimestamp)//
                .endTimestamp(startTimestamp.plusMinutes(aggregationWindowWidthMinutes))//
                .build();
    }

    /**
     * Asynchronously creates specified number of pages,
     * creating aggregates starting from given startTimestamp
     */
    public List<AggregatePage> createPages(LocalDateTime startTimestamp, int pageSize, int numberOfPagesToBeCreated)
    {
        return IntStream.range(0, numberOfPagesToBeCreated).mapToObj(value -> {
            if (StreamDatabaseVariable.isStream())
            {
                return createSinglePageStream(pageSize,
                        startTimestamp.plusMinutes(value * pageSize * aggregationWindowWidthMinutes));
            }
            else
            {
                return createSinglePage(pageSize, startTimestamp.plusMinutes(value * pageSize * aggregationWindowWidthMinutes));
            }
        }).collect(Collectors.toCollection(LinkedList::new));
    }

    private void deleteAlreadyReadRecords(final List<UUID> ids)
    {
        CompletableFuture.runAsync(() -> sensorReadingRepository.deleteAllById(ids));
    }

    public Aggregate getAggregateByDate(final LocalDateTime date, final int iterator)
    {
        var actualDate = date.plusMinutes(aggregationWindowWidthMinutes * iterator);
        var sensorReading = sensorReadingRepository.findByTimestampBetween(actualDate,
                actualDate.plusMinutes(aggregationWindowWidthMinutes));
        var waterLevels = sensorReading.stream().mapToInt(SensorReading::getWaterLevel).sum();
        return createAggregate(waterLevels, actualDate);
    }

    public void syntheticAggregation(final LocalDateTime startDate, final LocalDateTime endDate)
    {
        List<MaterializedAggregate> materializedAggregates = new LinkedList<>();
        var startTimeStamp = startDate;
        while (startTimeStamp.isBefore(endDate))
        {
            final Integer waterLevels = sensorReadingRepository.findWaterLevelsByTimestampBetween(startTimeStamp,
                    startTimeStamp.plusMinutes(aggregationWindowWidthMinutes));
            final Aggregate aggregate = createAggregate(waterLevels, startTimeStamp);
            var materializedAggregateToSave = new MaterializedAggregate(UUID.randomUUID(), waterLevels,
                    aggregate.getStartTimestamp(), aggregate.getEndTimestamp());
            materializedAggregates.add(materializedAggregateToSave);
            startTimeStamp = startTimeStamp.plusMinutes(aggregationWindowWidthMinutes);
            if (materializedAggregates.size() == 50)
            {
                materializedAggregateRepository.saveAll(materializedAggregates);
                materializedAggregates = new LinkedList<>();
            }
        }

        if (!materializedAggregates.isEmpty())
        {
            materializedAggregateRepository.saveAll(materializedAggregates);
        }
    }
}
