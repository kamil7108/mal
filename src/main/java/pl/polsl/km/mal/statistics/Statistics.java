package pl.polsl.km.mal.statistics;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import pl.polsl.km.mal.algorithm.PageFillingAlgorithm;
import pl.polsl.km.mal.algorithm.RENEW;
import pl.polsl.km.mal.algorithm.SPARE;
import pl.polsl.km.mal.facade.dto.AlgorithmEnum;
import pl.polsl.km.mal.mal.MAL;
import pl.polsl.km.mal.statistics.data.InitializationTime;
import pl.polsl.km.mal.statistics.data.IteratorData;
import pl.polsl.km.mal.statistics.data.SingleRecordTime;
import pl.polsl.km.mal.statistics.data.Type;
import pl.polsl.km.mal.statistics.repository.InitializationTimeRepository;
import pl.polsl.km.mal.statistics.repository.IteratorDataRepository;
import pl.polsl.km.mal.statistics.repository.SingleRecordTimeRepository;

public class Statistics
{

	private final static int MAX_SINGLE_RECORD_TIME_SIZE = 2500;
	private final InitializationTimeRepository initializationTimeRepository;
	private final SingleRecordTimeRepository singleRecordTimeRepository;
	private final IteratorDataRepository iteratorDataRepository;
	private final UUID iteratorId;
	private SingleRecordTime singleRecordTime;

	public Statistics(final IteratorDataRepository iteratorDataRepository,
			final InitializationTimeRepository initializationTimeRepository,
			final SingleRecordTimeRepository singleRecordTimeRepository)
	{
		this.iteratorDataRepository = iteratorDataRepository;
		this.initializationTimeRepository = initializationTimeRepository;
		this.singleRecordTimeRepository = singleRecordTimeRepository;
		this.iteratorId = UUID.randomUUID();
		this.singleRecordTime = new SingleRecordTime();
		singleRecordTime.setIteratorIdentifier(iteratorId);
	}

	public void saveSingleRecordTime(final long time)
	{
		singleRecordTime.addNewRecord(time);
		if (singleRecordTime.getRecordsSize() == MAX_SINGLE_RECORD_TIME_SIZE)
		{
			CompletableFuture.runAsync(() -> singleRecordTimeRepository.save(singleRecordTime));
			singleRecordTime = new SingleRecordTime();
			singleRecordTime.setIteratorIdentifier(iteratorId);
		}
	}

	public void savePageRecordTime(final long time)
	{
		//pageRecordTimes.add(time);
	}

	public void saveInitializationTimeAsync(final long time)
	{
		initializationTimeRepository.save(new InitializationTime(iteratorId, time));
	}

	public void saveStatistics(final long lastTaskTimeMillis)
	{
//		try
//		{
//			PrintWriter writer = new PrintWriter("singleRecordTime.csv");
//			StringBuilder sb = new StringBuilder();
//			singleRecordTimes.forEach(singleRecordTime -> {
//				sb.append(singleRecordTime);
//				sb.append(",");
//				sb.append("\n");
//			});
//			writer.write(sb.toString());
//			writer.close();
//		}
//		catch (FileNotFoundException e)
//		{
//			e.printStackTrace();
//		}
//		try
//		{
//			PrintWriter writer = new PrintWriter("pageRecordTime.csv");
//			StringBuilder sb = new StringBuilder();
//			pageRecordTimes.forEach(pageRecordTime -> {
//				sb.append(pageRecordTime);
//				sb.append(",");
//				sb.append("\n");
//			});
//			writer.write(sb.toString());
//			writer.close();
//		}
//		catch (FileNotFoundException e)
//		{
//			e.printStackTrace();
//		}
//		try
//		{
//			PrintWriter writer = new PrintWriter("malRecordTime.csv");
//			StringBuilder sb = new StringBuilder();
//			malRecordTimes.forEach(malRecordTime -> {
//				sb.append(malRecordTime);
//				sb.append(",");
//				sb.append("\n");
//			});
//			writer.write(sb.toString());
//			writer.close();
//		}
//		catch (FileNotFoundException e)
//		{
//			e.printStackTrace();
//		}
//		try
//		{
//			PrintWriter writer = new PrintWriter("allProgramTime.csv");
//			StringBuilder sb = new StringBuilder();
//			sb.append(lastTaskTimeMillis);
//			writer.write(sb.toString());
//			writer.close();
//		}
//		catch (FileNotFoundException e)
//		{
//			e.printStackTrace();
//		}
//	}
	}

	public void finalSaveStatistics()
	{
		singleRecordTimeRepository.save(singleRecordTime);
	}

	public void saveIteratorMetadataAsync(final MAL mal, final PageFillingAlgorithm algorithm, final Type type, final long time,
			final long aggregationTimeWindow)
	{
		CompletableFuture.runAsync(() -> {
			var algorithmEnum = AlgorithmEnum.TRIGG;
			if (algorithm instanceof SPARE)
			{
				algorithmEnum = AlgorithmEnum.TRIGG;
			}
			if (algorithm instanceof RENEW)
			{
				algorithmEnum = AlgorithmEnum.RENEW;
			}
			var iteratorData = IteratorData.builder()//
					.algorithmEnum(algorithmEnum)//
					.malSize(mal.size)//
					.pageSize(mal.pageSize)//
					.type(type)//
					.uuid(iteratorId)//
					.aggregationTime(time)//
					.aggregationTimeWindow(aggregationTimeWindow)//
					.build();
			iteratorDataRepository.save(iteratorData);
		});
	}

	public UUID getIteratorId()
	{
		return iteratorId;
	}
}
