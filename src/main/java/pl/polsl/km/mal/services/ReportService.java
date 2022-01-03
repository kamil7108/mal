/*
 * [y] hybris Platform
 *
 * Copyright (c) 2021 SAP SE or an SAP affiliate company.  All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with SAP.
 */
package pl.polsl.km.mal.services;

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
import pl.polsl.km.mal.statistics.data.SingleRecordTime;
import pl.polsl.km.mal.statistics.data.Type;
import pl.polsl.km.mal.statistics.repository.InitializationTimeRepository;
import pl.polsl.km.mal.statistics.repository.IteratorDataRepository;
import pl.polsl.km.mal.statistics.repository.ListIteratorDataRepository;
import pl.polsl.km.mal.statistics.repository.SingleRecordTimeRepository;

@AllArgsConstructor
@Service
public class ReportService
{
	private final static Logger LOGGER = LoggerFactory.getLogger(ReportService.class);
	private final static List<String> FIRST_LINE_HEADERS = List.of("Identifier", "Algorithm", "Mal Size", "Page size",
			"Aggregation time window", "Aggregation time in months", "Type");
	private final static List<String> FIRST_LINE_HEADERS_LIST_ITERATOR = List.of("Identifier", "Type", "Aggregation time window",
			"Aggregation time in months", "Ended with error", "Number of processed elements", "Average aggregate size in bytes");
	private final InitializationTimeRepository initializationTimeRepository;
	private final IteratorDataRepository iteratorDataRepository;
	private final SingleRecordTimeRepository singleRecordTimeRepository;
	private final ListIteratorDataRepository listIteratorDataRepository;

	public void prepareListIteratorReport(final UUID iteratorId)
	{
		try
		{
			var metadata = listIteratorDataRepository.findListIteratorDataByUuid(iteratorId);
			var recordTimes = singleRecordTimeRepository.findSingleRecordTimesByIteratorIdentifier(iteratorId);
			var totalTime = calculateTotalTime(recordTimes);
			var fileWriter = new FileWriter(prepareFile(iteratorId, metadata.getType()));
			var csvWriter = new CSVPrinter(fileWriter, CSVFormat.EXCEL.withDelimiter(';'));

			csvWriter.printRecord(FIRST_LINE_HEADERS_LIST_ITERATOR);
			csvWriter.printRecord(metadata.getUuid(), metadata.getType(), metadata.getAggregationTime(),
					metadata.getAggregationTimeWindow(),metadata.isEndWithError(),metadata.getNumberOfProcessedElements(),metadata.getAggregateSizeInBytes());
			csvWriter.printRecord(" ");
			csvWriter.printRecord("Total time in milliseconds");
			csvWriter.printRecord(totalTime / 1000);
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

	public void prepareReport(final UUID iteratorId)
	{
		try
		{
			var metadata = iteratorDataRepository.findIteratorDataByUuid(iteratorId);
			var recordTimes = singleRecordTimeRepository.findSingleRecordTimesByIteratorIdentifier(iteratorId);
			var totalTime = calculateTotalTime(recordTimes);
			var fileWriter = new FileWriter(prepareFile(iteratorId, metadata.getType()));
			var csvWriter = new CSVPrinter(fileWriter, CSVFormat.EXCEL.withDelimiter(';'));

			csvWriter.printRecord(FIRST_LINE_HEADERS);
			csvWriter.printRecord(metadata.getUuid(), metadata.getAlgorithmEnum(), metadata.getMalSize(), metadata.getPageSize(),
					metadata.getAggregationTimeWindow(), metadata.getAggregationTime(), metadata.getType());
			csvWriter.printRecord(" ");
			csvWriter.printRecord("Total time in milliseconds");
			csvWriter.printRecord(totalTime / 1000);
			csvWriter.printRecord(" ");
			csvWriter.printRecord("Initialization time in milliseconds");
			csvWriter.printRecord(initializationTimeRepository.findInitializationTimeByIteratorIdentifier(iteratorId)
					.getInitializationTime() / 1000);
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

	private List<Long> calculatePageTotalTime(final List<SingleRecordTime> singleRecordTimes, int pageSize)
	{
		var pageTotalTime = new LinkedList<Long>();
		int startIndex = 0;
		while (startIndex < singleRecordTimes.size())
		{
			Long pageTime = 0L;
			var pageRecords = singleRecordTimes.subList(startIndex, startIndex + pageSize - 1);
			for (SingleRecordTime singleRecordTime : pageRecords)
			{
				for (long time : singleRecordTime.getRecords())
				{
					pageTime += time;
				}
			}
			pageTotalTime.add(pageTime);
			startIndex += pageSize;
		}
		return pageTotalTime;
	}


	private String prepareFile(final UUID iteratorId, final Type type)
	{
		return String.format("%s-%s.csv", iteratorId, type);
	}
}
