package pl.polsl.km.mal;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import lombok.AllArgsConstructor;
import pl.polsl.km.mal.data.SensorReadingRepository;
import pl.polsl.km.mal.facade.dto.AlgorithmEnum;
import pl.polsl.km.mal.facade.dto.RequestMalConfigurationDTO;
import pl.polsl.km.mal.facade.dto.ResponseMalConfigurationDTO;
import pl.polsl.km.mal.iterator.Iterator;
import pl.polsl.km.mal.mal.Aggregate;
import pl.polsl.km.mal.mal.MAL;
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
	private final List<Pair<Iterator, LocalDateTime>> iterators = new ArrayList<>();
	private final InitializationTimeRepository initializationTimeRepository;
	private final SingleRecordTimeRepository singleRecordTimeRepository;
	private final IteratorDataRepository iteratorDataRepository;
	private final ReportService reportService;

	/**
	 * Method creates new iterator and add it to list for iterators waiting for test
	 *
	 * @param dto provides all information needed for configuring iterator
	 */
	public void createNewIterator(final RequestMalConfigurationDTO dto)
	{
		var mal = new MAL(dto.getPageSize(), dto.getMalSize());
		var iteratorPlusPlus = new Iterator(dto.getStartDate(), dto.getAlgorithm(),
				new PageSupplier(sensorReadingRepository, dto.getAggregationWindowWidthMinutes()), mal,
				new Statistics(iteratorDataRepository, initializationTimeRepository, singleRecordTimeRepository));
		iterators.add(Pair.of(iteratorPlusPlus, dto.getEndDate()));
	}

	/**
	 * Provide all logic of testing iterator.
	 * Iterator consume data form given timestamp to timestamp and collect statistics
	 *
	 * @param iterator iterator
	 * @param endTime end timestamp
	 * @param type type of run can be PARALLEL or SEQUENCE
	 * @return iterator uuid
	 */
	private UUID runIterator(final Iterator iterator, final LocalDateTime endTime, final Type type)
	{
		var statistics = iterator.getStatistics();
		var timeWindow = ChronoUnit.MONTHS.between(iterator.getStartTimestamp(), endTime);
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
		LOG.info("Ended processing data.");
		statistics.finalSaveStatistics();
		LOG.info("Saved final statistics");
		return statistics.getIteratorId();
	}

	/**
	 * Run configured iterators in sequence.
	 */
	public void runInSequence()
	{
		for (final Pair<Iterator, LocalDateTime> pair : iterators)
		{
			try
			{
				var iterator = pair.getFirst();
				runIterator(iterator, pair.getSecond(), Type.SEQUENCE);
				LOG.info("Preparing report for iterator with uuid {}.",iterator.getStatistics().getIteratorId());
				reportService.prepareReport(iterator.getStatistics().getIteratorId());
			}
			catch (Exception e)
			{
				LOG.info("Error occurred while waiting for result of iterator {}. Error {}", pair, e);
			}
		}
		iterators.removeAll(iterators);
	}

	/**
	 * Run all configured iterator in separate threads. Running parallel.
	 */
	public void runParallel()
	{
		List<CompletableFuture<UUID>> futures = new LinkedList<>();
		for (final Pair<Iterator, LocalDateTime> pair : iterators)
		{
			futures.add(CompletableFuture.supplyAsync(() -> runIterator(pair.getFirst(), pair.getSecond(), Type.PARALLEL)));
		}
		for (CompletableFuture<UUID> future : futures)
		{
			try
			{
				var uuid = future.get();
				LOG.info("Preparing report for iterator with uuid {}.",uuid);
				reportService.prepareReport(uuid);
			}
			catch (Exception e)
			{
				LOG.info("Error occurred while waiting for result of iterator {}. Error {}", future, e);
			}
		}
		iterators.removeAll(iterators);
	}

	/**
	 * Prepare all iterators data to dto.
	 *
	 * @return List of iterators configuration in dto objects
	 */
	public List<ResponseMalConfigurationDTO> getAllIterators()
	{
		var result = new ArrayList<ResponseMalConfigurationDTO>();
		IntStream.range(0, iterators.size()).forEach(i -> {
			var ir = iterators.get(i);
			var iterator = ir.getFirst();
			result.add(ResponseMalConfigurationDTO.builder()//
					.malSize(iterator.getMal().size)//
					.pageSize(iterator.getMal().pageSize)//
					.algorithmEnum(AlgorithmEnum.valueOf(iterator.getAlgorithm().toString()))//
					.startDate(iterator.getStartTimestamp())//
					.endDate(ir.getSecond())//
					.aggregationWindowWidthMinutes(iterator.getSupplier().getAggregationWindowWidthMinutes())//
					.id(i)//
					.build());
		});
		return result;
	}
}
