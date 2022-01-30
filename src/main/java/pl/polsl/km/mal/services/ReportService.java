package pl.polsl.km.mal.services;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import pl.polsl.km.mal.exception.ReportPreparingException;
import pl.polsl.km.mal.statistics.data.GlobalResultTestVersion;
import pl.polsl.km.mal.statistics.data.SingleRecordTime;
import pl.polsl.km.mal.statistics.data.Type;
import pl.polsl.km.mal.statistics.repository.GlobalResultTestVersionRepository;
import pl.polsl.km.mal.statistics.repository.InitializationTimeRepository;
import pl.polsl.km.mal.statistics.repository.ListIteratorDataRepository;
import pl.polsl.km.mal.statistics.repository.SingleRecordTimeRepository;
import pl.polsl.km.mal.statistics.repository.TestParametersRepository;

@AllArgsConstructor
@Service
public class ReportService
{
	private final static Logger LOGGER = LoggerFactory.getLogger(ReportService.class);
	private final static String RESULT_DIRECTORY = "../results";
	private final static List<String> FIRST_LINE_HEADERS = List.of("Identifier", "Algorithm", "Mal Size", "Page size",
			"Aggregation time window", "Aggregation time in months", "Type");
	private final static List<String> FIRST_LINE_HEADERS_LIST_ITERATOR = List.of("Identifier", "Type", "Aggregation time window",
			"Aggregation time in months", "Ended with error", "Number of processed elements", "Average aggregate size in bytes");
	private final InitializationTimeRepository initializationTimeRepository;
	private final TestParametersRepository testParametersRepository;
	private final SingleRecordTimeRepository singleRecordTimeRepository;
	private final ListIteratorDataRepository listIteratorDataRepository;
	private final GlobalResultTestVersionRepository globalResultTestVersionRepository;

	public void prepareListIteratorReport(final UUID iteratorId, final String testName, final String dir)
	{
		try
		{
			var metadata = listIteratorDataRepository.findListIteratorDataByUuid(iteratorId);
			var recordTimes = singleRecordTimeRepository.findSingleRecordTimesByIteratorIdentifier(iteratorId);
			var totalTime = calculateTotalTime(recordTimes);
			var fileWriter = new FileWriter(prepareFile(iteratorId, metadata.getType(), dir));
			var csvWriter = new CSVPrinter(fileWriter, CSVFormat.EXCEL.withDelimiter(';'));
			csvWriter.printRecord("Test description");
			csvWriter.printRecord(testName);
			csvWriter.printRecord(FIRST_LINE_HEADERS_LIST_ITERATOR);
			csvWriter.printRecord(metadata.getUuid(), metadata.getType(), metadata.getAggregationTimeWindow(),
					metadata.getAggregationTime(), metadata.isEndWithError(), metadata.getNumberOfProcessedElements(),
					metadata.getAggregateSizeInBytes());
			csvWriter.printRecord(" ");
			csvWriter.printRecord("Total time in milliseconds");
			csvWriter.printRecord(totalTime / 1000000);
			csvWriter.printRecord(" ");
			csvWriter.printRecord("Aggregate number", "Waiting time for the aggregate in nanos");
			var index = 1;
			for (SingleRecordTime recordTime : recordTimes)
			{
				for (long record : recordTime.getRecords())
				{
					csvWriter.printRecord(index++, record);
				}
			}
			csvWriter.flush();
		}
		catch (IOException e)
		{
			LOGGER.error("IOException occurred {}", e);
		}
	}

	public void prepareReport(final UUID iteratorId, final String testName, final String dir)
	{
		try
		{
			var metadata = testParametersRepository.findIteratorDataByUuid(iteratorId);
			var recordTimes = singleRecordTimeRepository.findSingleRecordTimesByIteratorIdentifier(iteratorId);
			var totalTime = calculateTotalTime(recordTimes);
			var initializationTime = initializationTimeRepository.findInitializationTimeByIteratorIdentifier(iteratorId)
					.getInitializationTime();
			var fileWriter = new FileWriter(prepareFile(iteratorId, metadata.getType(), dir));
			var csvWriter = new CSVPrinter(fileWriter, CSVFormat.EXCEL.withDelimiter(';'));
			csvWriter.printRecord("Test description");
			csvWriter.printRecord(testName);
			csvWriter.printRecord(FIRST_LINE_HEADERS);
			csvWriter.printRecord(metadata.getUuid(), metadata.getAlgorithmEnum(), metadata.getMalSize(), metadata.getPageSize(),
					metadata.getAggregationTimeWindow(), metadata.getAggregationTime(), metadata.getType());
			csvWriter.printRecord(" ");
			csvWriter.printRecord("Total time in milliseconds");
			csvWriter.printRecord((totalTime + initializationTime) / 1000000);
			csvWriter.printRecord(" ");
			csvWriter.printRecord("Initialization time in milliseconds");
			csvWriter.printRecord(initializationTime / 1000000);
			csvWriter.printRecord(" ");
			csvWriter.printRecord("Aggregate or page number", "Waiting time for the aggregate in nanos");
			var index = 1;
			for (SingleRecordTime recordTime : recordTimes)
			{
				for (long record : recordTime.getRecords())
				{
					csvWriter.printRecord(index++, record);
				}
			}
			csvWriter.flush();
		}
		catch (IOException e)
		{
			LOGGER.error("IOException occurred {}", e);
			throw new ReportPreparingException("Error occurred while preparing report");
		}
	}

	private Long calculateTotalTime(final List<SingleRecordTime> singleRecordTimes)
	{
		Long totalTime = 0L;
		for (SingleRecordTime recordTime : singleRecordTimes)
		{
			for (long time : recordTime.getRecords())
			{
				totalTime += time;
			}
		}
		return totalTime;
	}

	private String prepareFile(final UUID iteratorId, final Type type, final String dir)
	{
		return String.format("%s/%s-%s.csv", dir, iteratorId, type);
	}

	public String prepareDirectoryForReports(final String testName)
	{
		var query = globalResultTestVersionRepository.findTopByOrderByVersionDesc();
		int nextVersion = 1;
		if (query.isPresent())
		{
			nextVersion = query.get().getVersion() + 1;
		}
		var globalResultTestVersion = new GlobalResultTestVersion(nextVersion, testName);
		globalResultTestVersionRepository.save(globalResultTestVersion);
		File resultDir = new File(RESULT_DIRECTORY);
		if (!resultDir.exists())
		{
			resultDir.mkdir();
		}
		var dir = RESULT_DIRECTORY + "/" + nextVersion;
		File newDir = new File(dir);
		newDir.mkdir();
		return dir;
	}

	public void prepareReportPageSize(final String testName, final String dir, final LinkedList<UUID> uuidList)
	{
		try
		{
			var filename = String.format("%s/%s.csv", dir, "pageSizeTest");
			var fileWriter = new FileWriter(filename);
			var csvWriter = new CSVPrinter(fileWriter, CSVFormat.EXCEL.withDelimiter(';'));
			csvWriter.printRecord("Test description");
			csvWriter.printRecord(testName);
			csvWriter.printRecord("");
			csvWriter.printRecord("Total time","Page size");
			for (UUID uuid : uuidList)
			{
				var metadata = testParametersRepository.findIteratorDataByUuid(uuid);
				var recordTimes = singleRecordTimeRepository.findSingleRecordTimesByIteratorIdentifier(uuid);
				var time = calculateTotalTime(recordTimes);
				var initializationTime = initializationTimeRepository.findInitializationTimeByIteratorIdentifier(uuid)
						.getInitializationTime();
				var totalTime = (time + initializationTime) / 1000000;
				csvWriter.printRecord(totalTime, metadata.getPageSize());
			}
			csvWriter.flush();
		}
		catch (IOException e)
		{
			LOGGER.error("IOException occurred {}", e);
			throw new ReportPreparingException("Error occurred while preparing report");
		}
	}

	public void prepareReportMalSize(final String testName, final String dir, final LinkedList<UUID> uuidList)
	{
		try
		{
			var filename = String.format("%s/%s.csv", dir, "malSizeTest");
			var fileWriter = new FileWriter(filename);
			var csvWriter = new CSVPrinter(fileWriter, CSVFormat.EXCEL.withDelimiter(';'));
			csvWriter.printRecord("Test description");
			csvWriter.printRecord(testName);
			csvWriter.printRecord("");
			csvWriter.printRecord("Total time", "Mal size");
			for (UUID uuid : uuidList)
			{
				var metadata = testParametersRepository.findIteratorDataByUuid(uuid);
				var recordTimes = singleRecordTimeRepository.findSingleRecordTimesByIteratorIdentifier(uuid);
				var time = calculateTotalTime(recordTimes);
				var initializationTime = initializationTimeRepository.findInitializationTimeByIteratorIdentifier(uuid)
						.getInitializationTime();
				var totalTime = (time + initializationTime) / 1000000;
				csvWriter.printRecord(totalTime, metadata.getMalSize());
			}
			csvWriter.flush();
		}
		catch (IOException e)
		{
			LOGGER.error("IOException occurred {}", e);
			throw new ReportPreparingException("Error occurred while preparing report");
		}
	}

	public void prepareReportAlgorithmInfluence(final String testName, final String dir, final LinkedList<UUID> uuidList)
	{
		try
		{
			var filename = String.format("%s/%s.csv", dir, "algorithmInfluenceTest");
			var fileWriter = new FileWriter(filename);
			var csvWriter = new CSVPrinter(fileWriter, CSVFormat.EXCEL.withDelimiter(';'));
			csvWriter.printRecord("Test description");
			csvWriter.printRecord(testName);
			csvWriter.printRecord("");
			csvWriter.printRecord("Total time", "Algorithm", "Aggregation period");
			for (UUID uuid : uuidList)
			{
				var metadata = testParametersRepository.findIteratorDataByUuid(uuid);
				var totalTime = 0L;
				if (metadata != null)
				{
					var recordTimes = singleRecordTimeRepository.findSingleRecordTimesByIteratorIdentifier(uuid);
					var time = calculateTotalTime(recordTimes);
					var initializationTime = initializationTimeRepository.findInitializationTimeByIteratorIdentifier(uuid)
							.getInitializationTime();
					totalTime = (time + initializationTime);
					float xd = ((float)totalTime/ 1000000);
					csvWriter.printRecord(xd, metadata.getAlgorithmEnum().toString(), metadata.getAggregationTime());
				}
				else
				{
					var listMetadata = listIteratorDataRepository.findListIteratorDataByUuid(uuid);
					var recordTimes = singleRecordTimeRepository.findSingleRecordTimesByIteratorIdentifier(uuid);
					totalTime = calculateTotalTime(recordTimes);
					csvWriter.printRecord(totalTime/ 1000000, "List", listMetadata.getAggregationTime());
				}
			}
			csvWriter.flush();
		}
		catch (IOException e)
		{
			LOGGER.error("IOException occurred {}", e);
			throw new ReportPreparingException("Error occurred while preparing report");
		}
	}

	public void prepareReportMaterializingInfluence(final String testName, final String dir,
			final LinkedList<UUID> uuidListMaterialized, final LinkedList<UUID> uuidListNotMaterialized)
	{
		try
		{
			var filename = String.format("%s/%s.csv", dir, "materializationinInfluence");
			var fileWriter = new FileWriter(filename);
			var csvWriter = new CSVPrinter(fileWriter, CSVFormat.EXCEL.withDelimiter(';'));
			csvWriter.printRecord("Test description");
			csvWriter.printRecord(testName);
			csvWriter.printRecord("");
			csvWriter.printRecord("Total time", "Algorithm", "Aggregation period");
			csvWriter.printRecord("Materialized", "", "", "Not Materialized");
			for (int i = 0; i < uuidListMaterialized.size(); i++)
			{
				var uuidMaterialized = uuidListMaterialized.get(i);
				var uuidNotMaterialized = uuidListNotMaterialized.get(i);
				var metadataMaterialized = testParametersRepository.findIteratorDataByUuid(uuidMaterialized);
				var metadataNotMaterialized = testParametersRepository.findIteratorDataByUuid(uuidNotMaterialized);
				var totalTimeMaterialized = 0L;
				var totalTimeNotMaterialized = 0L;
				if (metadataMaterialized != null && metadataNotMaterialized != null)
				{
					var recordTimesMaterialized = singleRecordTimeRepository.findSingleRecordTimesByIteratorIdentifier(
							uuidMaterialized);
					var timeMaterialized = calculateTotalTime(recordTimesMaterialized);
					var initializationTimeMaterialized = initializationTimeRepository.findInitializationTimeByIteratorIdentifier(
							uuidMaterialized).getInitializationTime();
					totalTimeMaterialized = (timeMaterialized + initializationTimeMaterialized);
					float timeMaterializedMs = ((float) totalTimeMaterialized / 1000000);
					var recordTimesNotMaterialized = singleRecordTimeRepository.findSingleRecordTimesByIteratorIdentifier(
							uuidNotMaterialized);
					var timeNotMaterialized = calculateTotalTime(recordTimesNotMaterialized);
					var initializationTimeNotMaterialized = initializationTimeRepository
							.findInitializationTimeByIteratorIdentifier(uuidNotMaterialized).getInitializationTime();
					totalTimeNotMaterialized = (timeNotMaterialized + initializationTimeNotMaterialized);
					float timeNotMaterializedMs = ((float) totalTimeNotMaterialized / 1000000);
					csvWriter.printRecord(timeMaterializedMs, metadataMaterialized.getAlgorithmEnum().toString(),
							metadataMaterialized.getAggregationTime(), timeNotMaterializedMs,
							metadataNotMaterialized.getAlgorithmEnum().toString(), metadataNotMaterialized.getAggregationTime());
				}
			}
			csvWriter.flush();
		}
		catch (IOException e)
		{
			LOGGER.error("IOException occurred {}", e);
			throw new ReportPreparingException("Error occurred while preparing report");
		}
	}
}
