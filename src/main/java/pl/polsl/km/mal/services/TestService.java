package pl.polsl.km.mal.services;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.util.SerializationUtils;
import org.springframework.util.StopWatch;
import org.springframework.web.server.ResponseStatusException;

import lombok.AllArgsConstructor;
import pl.polsl.km.mal.algorithm.PageFillingAlgorithm;
import pl.polsl.km.mal.algorithm.RENEW;
import pl.polsl.km.mal.algorithm.SPARE;
import pl.polsl.km.mal.algorithm.TRIGG;
import pl.polsl.km.mal.exception.NoInitializedScenarioException;
import pl.polsl.km.mal.exception.PassedNotProperTypeOfStructException;
import pl.polsl.km.mal.facade.dto.AlgorithmEnum;
import pl.polsl.km.mal.facade.dto.RequestMalConfigurationDTO;
import pl.polsl.km.mal.facade.dto.ResponseMalConfigurationDTO;
import pl.polsl.km.mal.facade.dto.TestPageSizeDTO;
import pl.polsl.km.mal.iterator.MalIterator;
import pl.polsl.km.mal.listIterator.ListIterator;
import pl.polsl.km.mal.mal.Aggregate;
import pl.polsl.km.mal.mal.MAL;
import pl.polsl.km.mal.statistics.Statistics;
import pl.polsl.km.mal.statistics.data.Type;
import pl.polsl.km.mal.statistics.repository.InitializationTimeRepository;
import pl.polsl.km.mal.statistics.repository.IteratorDataRepository;
import pl.polsl.km.mal.statistics.repository.ListIteratorDataRepository;
import pl.polsl.km.mal.statistics.repository.SingleRecordTimeRepository;
import pl.polsl.km.mal.testData.data.StreamDatabaseVariable;
import pl.polsl.km.mal.testData.repository.MaterializedAggregateRepository;
import pl.polsl.km.mal.testData.repository.SensorReadingRepository;

@Service
@AllArgsConstructor
public class TestService
{
	private final static Logger LOG = LoggerFactory.getLogger(TestService.class);

	private final SensorReadingRepository sensorReadingRepository;
	private final List<Pair<MalIterator, LocalDateTime>> iterators = new ArrayList<>();
	private final List<ListIterator> listIterators = new ArrayList<>();
	private final InitializationTimeRepository initializationTimeRepository;
	private final SingleRecordTimeRepository singleRecordTimeRepository;
	private final IteratorDataRepository iteratorDataRepository;
	private final ListIteratorDataRepository listIteratorDataRepository;
	private final ReportService reportService;
	private final RecordProducerService recordProducerService;
	private final MaterializedAggregateRepository materializedAggregateRepository;

	/**
	 * Method creates new iterator and add it to list for iterators waiting for test
	 *
	 * @param dto provides all information needed for configuring iterator
	 */
	public UUID createNewIterator(final RequestMalConfigurationDTO dto)
	{
		var algorithmEnum = dto.getAlgorithm();
		var uuid = UUID.randomUUID();
		if (AlgorithmEnum.contains(algorithmEnum))
		{
			PageFillingAlgorithm algorithm = new TRIGG();
			if (AlgorithmEnum.SPARE.name().equals(algorithmEnum))
			{
				algorithm = new SPARE();
			}
			else if (AlgorithmEnum.RENEW.name().equals(algorithmEnum))
			{
				algorithm = new RENEW();
			}
			var mal = new MAL(dto.getPageSize(), dto.getMalSize(), dto.getStartDate(), algorithm,
					new AggregateSupplierService(sensorReadingRepository, materializedAggregateRepository,
							dto.getAggregationWindowWidthMinutes()));
			var iteratorPlusPlus = new MalIterator(mal,
					new Statistics(uuid, iteratorDataRepository, listIteratorDataRepository, initializationTimeRepository,
							singleRecordTimeRepository));
			iterators.add(Pair.of(iteratorPlusPlus, dto.getEndDate()));
		}
		else if (algorithmEnum.equals("LIST"))
		{
			listIterators.add(new ListIterator(uuid, dto.getStartDate(), dto.getEndDate(),
					new AggregateSupplierService(sensorReadingRepository, materializedAggregateRepository,
							dto.getAggregationWindowWidthMinutes()),
					new Statistics(uuid, iteratorDataRepository, listIteratorDataRepository, initializationTimeRepository,
							singleRecordTimeRepository)));
		}
		else
		{
			throw new PassedNotProperTypeOfStructException("Algorithm enum was initialized with inappropriate value.");
		}
		return uuid;
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
	private UUID runIterator(final MalIterator iterator, final LocalDateTime endTime, final Type type)
	{
		var statistics = iterator.getStatistics();
		var mal = iterator.getMal();
		var timeWindow = ChronoUnit.MONTHS.between(mal.getStartTimestamp(), endTime);
		statistics.saveIteratorMetadataAsync(iterator.getMal(), mal.getAlgorithm(), type, timeWindow,
				mal.getSupplier().getAggregationWindowWidthMinutes());
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
	public void runInSequence(final String testName, final Boolean useMaterializedData) throws ResponseStatusException
	{
		if (iterators.size() > 0 || listIterators.size() > 0)
		{
			if (!StreamDatabaseVariable.isStream())
			{
				recordProducerService.cleanRecordInRecordProducerDatabase();
				materializedAggregateRepository.deleteAll();
				var dir = reportService.prepareDirectoryForReports(testName);
				if (iterators.size() > 0)
				{
					recordProducerService.produceRecordBetweenTwoDates(iterators.get(0).getFirst().getMal().getStartTimestamp(),
							iterators.get(0).getSecond());
					iteratorTest(testName, dir, useMaterializedData);
				}
				if (listIterators.size() > 0)
				{
					recordProducerService.produceRecordBetweenTwoDates(listIterators.get(0).getStartDate(),
							listIterators.get(0).getEndDate());
					listTest(testName, dir, useMaterializedData);
				}
			}
		}
		else
		{
			throw new NoInitializedScenarioException("There is no initialized scenarios.");
		}
	}

	private void iteratorTest(final String testName, final String dir, final Boolean useMaterializedData)
	{
		for (final Pair<MalIterator, LocalDateTime> pair : iterators)
		{
			try
			{
				if (!useMaterializedData)
				{
					materializedAggregateRepository.deleteAll();
				}
				var iterator = pair.getFirst();
				//request start process of stream database filling
				if (StreamDatabaseVariable.isStream())
				{
					recordProducerService.cleanRecordInRecordProducerDatabase();
					recordProducerService.runRecordProducer(iterator.getMal().getStartTimestamp());
				}
				runIterator(iterator, pair.getSecond(), Type.SEQUENCE);
				if (StreamDatabaseVariable.isStream())
				{
					recordProducerService.stopRecordProducer();
				}
				LOG.info("Preparing report for iterator with uuid {}.", iterator.getStatistics().getIteratorId());
				reportService.prepareReport(iterator.getStatistics().getIteratorId(), testName, dir);
			}
			catch (Exception e)
			{
				LOG.info("Error occurred while waiting for result of iterator {}. Error {}", pair, e);
			}
		}
		iterators.removeAll(iterators);
	}

	private void listTest(final String testName, final String dir, final Boolean useMaterializedData)
	{
		for (final ListIterator iterator : listIterators)
		{
			try
			{
				if (!useMaterializedData)
				{
					materializedAggregateRepository.deleteAll();
				}
				//request start process of stream database filling
				if (StreamDatabaseVariable.isStream())
				{
					recordProducerService.cleanRecordInRecordProducerDatabase();
					recordProducerService.runRecordProducer(iterator.getStartDate());
				}
				runListIterator(iterator);
				if (StreamDatabaseVariable.isStream())
				{
					recordProducerService.stopRecordProducer();
				}
				LOG.info("Preparing report for iterator with uuid {}.", iterator.getStatistics().getIteratorId());
				reportService.prepareListIteratorReport(iterator.getStatistics().getIteratorId(), testName, dir);
			}
			catch (Exception e)
			{
				LOG.info("Error occurred while waiting for result of iterator {}. Error {}", e);
			}
		}
		listIterators.removeAll(listIterators);
	}

	private void runListIterator(final ListIterator iterator)
	{
		var statistics = iterator.getStatistics();
		var timeWindow = ChronoUnit.MONTHS.between(iterator.getStartDate(), iterator.getEndDate());
		var uuid = iterator.getUuid();
		var stopWatch = new StopWatch();
		Aggregate element = iterator.next();
		int numberOfProcessedElements = 0;
		try
		{
			do
			{
				stopWatch.start();
				element = iterator.next();
				stopWatch.stop();
				numberOfProcessedElements++;
				statistics.saveSingleRecordTime(stopWatch.getLastTaskTimeNanos());
			} while (element.getStartTimestamp().isBefore(iterator.getEndDate()));
		}
		catch (Exception exception)
		{
			LOG.info("End of memory while testing list iterator with uuid {}.", uuid);

			statistics.saveListIteratorMetadataAsync(uuid, Type.LIST, timeWindow, iterator.getAggregationWindowTimeInMinutes(),
					true, numberOfProcessedElements, Objects.requireNonNull(SerializationUtils.serialize(element)).length);
		}
		LOG.info("Ended processing data.");
		statistics.finalSaveStatistics();
		LOG.info("Saved final statistics");
		statistics.saveListIteratorMetadataAsync(uuid, Type.LIST, timeWindow, iterator.getAggregationWindowTimeInMinutes(),
				false,
				numberOfProcessedElements, Objects.requireNonNull(SerializationUtils.serialize(element)).length);
	}

	/**
	 * Run all configured iterator in separate threads. Running parallel.
	 */
	public void runParallel(final String testName)
	{
		var dir = reportService.prepareDirectoryForReports(testName);
		List<CompletableFuture<UUID>> futures = new LinkedList<>();
		prepareDataForParallelTest();
		for (final Pair<MalIterator, LocalDateTime> pair : iterators)
		{
			futures.add(CompletableFuture.supplyAsync(() -> runIterator(pair.getFirst(), pair.getSecond(), Type.PARALLEL)));
		}
		for (CompletableFuture<UUID> future : futures)
		{
			try
			{
				var uuid = future.get();
				LOG.info("Preparing report for iterator with uuid {}.", uuid);
				reportService.prepareReport(uuid, testName, dir);
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
					.algorithmEnum(AlgorithmEnum.valueOf(iterator.getMal().getAlgorithm().toString()))//
					.startDate(iterator.getMal().getStartTimestamp())//
					.endDate(ir.getSecond())//
					.aggregationWindowWidthMinutes(iterator.getMal().getSupplier().getAggregationWindowWidthMinutes())//
					.id(i)//
					.build());
		});
		IntStream.range(0, listIterators.size()).forEach(i -> {
			var ir = listIterators.get(i);
			result.add(ResponseMalConfigurationDTO.builder()//
					.malSize(0)//
					.pageSize(0)//
					.algorithmEnum(AlgorithmEnum.LIST)//
					.startDate(ir.getStartDate())//
					.endDate(ir.getEndDate())//
					.aggregationWindowWidthMinutes(ir.getAggregationWindowTimeInMinutes())//
					.id(i + iterators.size())//
					.build());
		});

		return result;
	}

	private void prepareDataForParallelTest()
	{
		var minDate = iterators.get(0).getFirst().getMal().getStartTimestamp();
		var maxDate = iterators.get(0).getSecond();
		for (int i = 1; i < iterators.size(); i++)
		{
			var startDate = iterators.get(i).getFirst().getMal().getStartTimestamp();
			var endDate = iterators.get(i).getSecond();
			if (minDate.isAfter(startDate))
			{
				minDate = startDate;
			}
			if (maxDate.isBefore(endDate))
			{
				maxDate = endDate;
			}
		}
		recordProducerService.cleanRecordInRecordProducerDatabase();
		recordProducerService.produceRecordBetweenTwoDates(minDate, maxDate);
	}

	public void testPageSize(final TestPageSizeDTO dto)
	{
		var minPageSize = dto.getMinPageSize();
		var maxPageSize = dto.getMaxPageSize();
		var pageIncrementation = dto.getPageIncrementation();
		var malSize = dto.getMalSize();
		var starDate = dto.getStartDate();
		var endDate = dto.getEndDate();
		var algorithmEnum = dto.getAlgorithm();

		var testName = "Page size test";
		recordProducerService.cleanRecordInRecordProducerDatabase();
		materializedAggregateRepository.deleteAll();
		var dir = reportService.prepareDirectoryForReports(testName);
		var uuidList = new LinkedList<UUID>();
		for (int i = minPageSize; i <= maxPageSize; i = i + pageIncrementation)
		{

			var uuid = UUID.randomUUID();
			uuidList.add(uuid);
			PageFillingAlgorithm algorithm = new TRIGG();
			if (AlgorithmEnum.SPARE.name().equals(algorithmEnum))
			{
				algorithm = new SPARE();
			}
			else if (AlgorithmEnum.RENEW.name().equals(algorithmEnum))
			{
				algorithm = new RENEW();
			}
			var mal = new MAL(i, malSize, dto.getStartDate(), algorithm,
					new AggregateSupplierService(sensorReadingRepository, materializedAggregateRepository,
							dto.getAggregationWindowWidthMinutes()));
			var iteratorPlusPlus = new MalIterator(mal,
					new Statistics(uuid, iteratorDataRepository, listIteratorDataRepository, initializationTimeRepository,
							singleRecordTimeRepository));
			iterators.add(Pair.of(iteratorPlusPlus, endDate));
		}
		if (iterators.size() > 0)
		{
			recordProducerService.produceRecordBetweenTwoDates(starDate, endDate);
			iteratorTest(testName, dir, false);
			reportService.prepareReportPageSize(testName,dir,uuidList);
		}
	}
}
