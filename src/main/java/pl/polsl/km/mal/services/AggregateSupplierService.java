package pl.polsl.km.mal.services;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import pl.polsl.km.mal.mal.Aggregate;
import pl.polsl.km.mal.mal.Page;
import pl.polsl.km.mal.testData.data.MaterializedAggregate;
import pl.polsl.km.mal.testData.data.SensorReading;
import pl.polsl.km.mal.testData.repository.MaterializedAggregateRepository;
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

    public Page createSinglePage(int pageSize, LocalDateTime timestamp)
    {
        final Page page = new Page();
        LocalDateTime startTimestamp = timestamp;
        LocalDateTime endTimestamp = timestamp.plusMinutes(aggregationWindowWidthMinutes * (pageSize-1));
        final List<MaterializedAggregate> materializedAggregate = materializedAggregateRepository//
                .getAllBetweenDates(startTimestamp, endTimestamp).get();
        if (materializedAggregate.size() == pageSize)
        {
            materializedAggregate.forEach(r -> page.append(createAggregate(r.getWaterLevelReadings(), r.getStartTimestamp())));
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
                page.append(aggregate);
                tempDate = tempDate.plusMinutes(aggregationWindowWidthMinutes);
            }
        }
        return page;
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
    public List<Page> createPages(LocalDateTime startTimestamp, int pageSize, int numberOfPagesToBeCreated)
    {
        return IntStream.range(0, numberOfPagesToBeCreated).mapToObj(
                value -> createSinglePage(pageSize, startTimestamp.plusMinutes(value * pageSize * aggregationWindowWidthMinutes)))
                .collect(Collectors.toCollection(LinkedList::new));
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

    @Transactional
    public void syntheticAggregation(final LocalDateTime startDate, final LocalDateTime endDate)
    {
        List<MaterializedAggregate> materializedAggregates = new LinkedList<>();
        var startTimeStamp = startDate;
        while (!startTimeStamp.isAfter(endDate))
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

    @Transactional
    public void cleanMaterializedAggregates()
    {
        materializedAggregateRepository.deleteAll();
    }
}
