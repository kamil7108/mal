package pl.polsl.km.mal;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import lombok.AllArgsConstructor;
import pl.polsl.km.mal.data.SensorReadingRepository;
import pl.polsl.km.mal.facade.Algorithm;
import pl.polsl.km.mal.facade.ResponseMalConfigurationDTO;
import pl.polsl.km.mal.iterator.IteratorPlusPlus;
import pl.polsl.km.mal.mal.Aggregate;
import pl.polsl.km.mal.mal.MAL;
import pl.polsl.km.mal.mal.MALConfigurationProvider;
import pl.polsl.km.mal.statistics.Statistics;
import pl.polsl.km.mal.statistics.data.Type;
import pl.polsl.km.mal.statistics.repository.InitializationTimeRepository;
import pl.polsl.km.mal.statistics.repository.IteratorDataRepository;
import pl.polsl.km.mal.statistics.repository.SingleRecordTimeRepository;

@Service
@AllArgsConstructor
public class TestService
{
	private final static Logger LOG = LoggerFactory.getLogger(TestService.class);

	private final SensorReadingRepository sensorReadingRepository;
	private final List<Pair<IteratorPlusPlus, LocalDateTime>> iterators = new ArrayList<>();
	private final InitializationTimeRepository initializationTimeRepository;
	private final SingleRecordTimeRepository singleRecordTimeRepository;
	private final IteratorDataRepository iteratorDataRepository;
	private final ReportService reportService;

	public void createNewIterator(final MALConfigurationProvider configurationProvider)
	{
		var mal = new MAL(configurationProvider.getPageSize(), configurationProvider.getMalSize());
		var iteratorPlusPlus = new IteratorPlusPlus(configurationProvider.getStartDate(), configurationProvider.getAlgorithm(),
				new PageSupplier(sensorReadingRepository, configurationProvider.getAggregationWindowWidthMinutes()), mal,
				new Statistics(iteratorDataRepository, initializationTimeRepository, singleRecordTimeRepository));
		iterators.add(Pair.of(iteratorPlusPlus, configurationProvider.getEndDate()));
	}

	private void runIterator(final Pair<IteratorPlusPlus, LocalDateTime> pair, final Type type)
	{
		var iterator = pair.getFirst();
		var endTime = pair.getSecond();
		var statistics = iterator.getStatistics();
		var timeWindow = ChronoUnit.YEARS.between(pair.getSecond(), iterator.getStartTimestamp());
		statistics.saveIteratorMetadataAsync(iterator.getMal(), iterator.getAlgorithm(), type, timeWindow,
				iterator.getSupplier().getAggregationWindowWidthMinutes());
		var stopWatch = new StopWatch();

		//measure initialization time
		stopWatch.start();
		iterator.initializeMalData();
		stopWatch.stop();
		statistics.saveInitializationTimeAsync(stopWatch.getLastTaskTimeNanos());

		Aggregate element = null;
		do
		{
			stopWatch.start();
			element = iterator.next();
			stopWatch.stop();
			statistics.saveSingleRecordTime(stopWatch.getLastTaskTimeNanos());
		} while (element.getStartTimestamp().isBefore(endTime));
		statistics.finalSaveStatistics();
	}

	public List<ResponseMalConfigurationDTO> getAllIterators()
	{
		var result = new ArrayList<ResponseMalConfigurationDTO>();
		IntStream.range(0, iterators.size()).forEach(i -> {
			var ir = iterators.get(i);
			var iterator = ir.getFirst();
			result.add(ResponseMalConfigurationDTO.builder()//
					.malSize(iterator.getMal().size)//
					.pageSize(iterator.getMal().pageSize)//
					.algorithm(Algorithm.valueOf(iterator.getAlgorithm().toString()))//
					.startDate(iterator.getStartTimestamp())//
					.endDate(ir.getSecond())//
					.aggregationWindowWidthMinutes(iterator.getSupplier().getAggregationWindowWidthMinutes())//
					.id(i)//
					.build());
		});
		return result;
	}

	public void runInSequence()
	{
		for (final Pair<IteratorPlusPlus, LocalDateTime> pair : iterators)
		{
			try
			{
				CompletableFuture.runAsync(() -> runIterator(pair, Type.SEQUENCE)).get();
				reportService.prepareReport(pair.getFirst().getStatistics().getIteratorId());
			}
			catch (Exception e)
			{
				LOG.info("Error occurred while waiting for result of iterator {}. Error {}", pair, e);
			}
		}
	}

	public void runParallel()
	{
		List<CompletableFuture> futures = new LinkedList<>();
		for (final Pair<IteratorPlusPlus, LocalDateTime> pair : iterators)
		{
			futures.add(CompletableFuture.runAsync(() -> runIterator(pair, Type.SEQUENCE)));
		}
		for (CompletableFuture future : futures)
		{
			try
			{
				future.get();
			}
			catch (Exception e)
			{
				LOG.info("Error occurred while waiting for result of iterator {}. Error {}", future, e);
			}
		}
	}
}
