package pl.polsl.km.mal.statistics;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import pl.polsl.km.mal.algorithm.PageFillingAlgorithm;
import pl.polsl.km.mal.algorithm.RENEW;
import pl.polsl.km.mal.algorithm.SPARE;
import pl.polsl.km.mal.algorithm.TRIGG;
import pl.polsl.km.mal.facade.dto.AlgorithmEnum;
import pl.polsl.km.mal.mal.MAL;
import pl.polsl.km.mal.statistics.data.InitializationTime;
import pl.polsl.km.mal.statistics.data.IteratorData;
import pl.polsl.km.mal.statistics.data.ListIteratorData;
import pl.polsl.km.mal.statistics.data.SingleRecordTime;
import pl.polsl.km.mal.statistics.data.Type;
import pl.polsl.km.mal.statistics.repository.InitializationTimeRepository;
import pl.polsl.km.mal.statistics.repository.IteratorDataRepository;
import pl.polsl.km.mal.statistics.repository.ListIteratorDataRepository;
import pl.polsl.km.mal.statistics.repository.SingleRecordTimeRepository;

public class Statistics
{

	private final static int MAX_SINGLE_RECORD_TIME_SIZE = 500;
	private final InitializationTimeRepository initializationTimeRepository;
	private final SingleRecordTimeRepository singleRecordTimeRepository;
	private final IteratorDataRepository iteratorDataRepository;
	private final ListIteratorDataRepository listIteratorDataRepository;
	private final UUID iteratorId;
	private SingleRecordTime singleRecordTime;

	public Statistics(final UUID uuid, final IteratorDataRepository iteratorDataRepository,
			final ListIteratorDataRepository listIteratorDataRepository,
			final InitializationTimeRepository initializationTimeRepository,
			final SingleRecordTimeRepository singleRecordTimeRepository)
	{
		this.iteratorDataRepository = iteratorDataRepository;
		this.initializationTimeRepository = initializationTimeRepository;
		this.singleRecordTimeRepository = singleRecordTimeRepository;
		this.listIteratorDataRepository = listIteratorDataRepository;
		this.iteratorId = uuid;
		this.singleRecordTime = new SingleRecordTime();
		singleRecordTime.setIteratorIdentifier(iteratorId);
	}

	public void saveSingleRecordTime(final long time)
	{
		singleRecordTime.addNewRecord(time);
		if (singleRecordTime.getRecordsSize() == MAX_SINGLE_RECORD_TIME_SIZE)
		{
			singleRecordTimeRepository.save(singleRecordTime);
			singleRecordTime = new SingleRecordTime();
			singleRecordTime.setIteratorIdentifier(iteratorId);
		}
	}

	public void saveInitializationTimeAsync(final long time)
	{
		initializationTimeRepository.save(new InitializationTime(iteratorId, time));
	}

	public void finalSaveStatistics()
	{
		singleRecordTimeRepository.save(singleRecordTime);
	}

	public void saveListIteratorMetadataAsync(final UUID uuid, final Type type, final long time,
			final long aggregationTimeWindow,final boolean endWithError,final int numberOfProcessedElements,final int aggregateSizeInBytes)
	{
		try
		{
			CompletableFuture.runAsync(() -> {
				var iteratorData = ListIteratorData.builder()//
						.type(type)//
						.uuid(uuid)//
						.aggregationTime(time)//
						.aggregationTimeWindow(aggregationTimeWindow)//
						.endWithError(endWithError)//
						.numberOfProcessedElements(numberOfProcessedElements)//
						.aggregateSizeInBytes(aggregateSizeInBytes)
						.build();
				listIteratorDataRepository.save(iteratorData);
			}).get();
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
		catch (ExecutionException e)
		{
			e.printStackTrace();
		}
	}

	public void saveIteratorMetadataAsync(final MAL mal, final PageFillingAlgorithm algorithm, final Type type, final long time,
			final long aggregationTimeWindow)
	{
		CompletableFuture.runAsync(() -> {
			AlgorithmEnum algorithmEnum = null;
			if (algorithm instanceof SPARE)
			{
				algorithmEnum = AlgorithmEnum.SPARE;
			}
			if (algorithm instanceof RENEW)
			{
				algorithmEnum = AlgorithmEnum.RENEW;
			}
			if(algorithm instanceof TRIGG){
				algorithmEnum = AlgorithmEnum.TRIGG;
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
