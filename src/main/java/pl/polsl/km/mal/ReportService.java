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
package pl.polsl.km.mal;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import pl.polsl.km.mal.statistics.data.SingleRecordTime;
import pl.polsl.km.mal.statistics.data.Type;
import pl.polsl.km.mal.statistics.repository.InitializationTimeRepository;
import pl.polsl.km.mal.statistics.repository.IteratorDataRepository;
import pl.polsl.km.mal.statistics.repository.SingleRecordTimeRepository;

@AllArgsConstructor
@Service
public class ReportService
{
	private final static Logger LOGGER = LoggerFactory.getLogger(ReportService.class);
	private final static String FILE_DIRECTORY = "/report";
	private final static List<String> FIRST_LINE_HEADERS = List.of("Identifier", "Algorithm", "Mal Size", "Page size",
			"Aggregation time window", "Aggregation time", "Type");

	private final InitializationTimeRepository initializationTimeRepository;
	private final IteratorDataRepository iteratorDataRepository;
	private final SingleRecordTimeRepository singleRecordTimeRepository;

	public void prepareReport(final UUID iteratorId)
	{
		try
		{
			var metadata = iteratorDataRepository.findIteratorDataByUuid(iteratorId);
			var recordTimes = singleRecordTimeRepository.findSingleRecordTimesByIteratorIdentifier(iteratorId);
			var fileWriter = new FileWriter(prepareFile(iteratorId, metadata.getType()));
			var csvWriter = new CSVPrinter(fileWriter, CSVFormat.EXCEL.withDelimiter(';'));
			csvWriter.printRecord(FIRST_LINE_HEADERS);
			csvWriter.printRecord(metadata.getUuid(), metadata.getAlgorithm(), metadata.getMalSize(), metadata.getPageSize(),
					metadata.getAggregationTimeWindow(), metadata.getAggregationTime(), metadata.getType());
			csvWriter.printRecord(" ");
			csvWriter.printRecord("Initialization time in nanos");
			csvWriter.printRecord(" ");
			csvWriter.printRecord(
					initializationTimeRepository.findInitializationTimeByIteratorIdentifier(iteratorId).getInitializationTime());
			csvWriter.printRecord("Aggregate number", "Waiting time for the aggregate in nanos");
			var index = 1;
			for (SingleRecordTime recordTime : recordTimes)
			{
				for (long record : recordTime.getRecords())
				{
					csvWriter.printRecord(index++, record);
				}
			}
		}
		catch (IOException e)
		{
			LOGGER.error("IOException occurred {}", e);
		}
	}

	private String prepareFile(final UUID iteratorId, final Type type)
	{
		return String.format("%s-%s.csv", iteratorId, type);
	}
}
