package pl.polsl.wylegly_machulik.mal;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.IntStream;

import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import lombok.RequiredArgsConstructor;
import pl.polsl.wylegly_machulik.mal.data.SensorReadingRepository;
import pl.polsl.wylegly_machulik.mal.facade.Algorithm;
import pl.polsl.wylegly_machulik.mal.facade.ResponseMalConfigurationDTO;
import pl.polsl.wylegly_machulik.mal.iterator.IteratorPlusPlus;
import pl.polsl.wylegly_machulik.mal.mal.MAL;
import pl.polsl.wylegly_machulik.mal.mal.MALConfigurationProvider;
import pl.polsl.wylegly_machulik.mal.statistics.Statistics;

@Service
@RequiredArgsConstructor
public class TestService
{
	private final SensorReadingRepository sensorReadingRepository;
	private final Statistics statistics;
	private static final StopWatch allWatch = new StopWatch();
	private final List<Pair<IteratorPlusPlus, LocalDateTime>> iterators = new ArrayList<>();

	public void createNewIterator(final MALConfigurationProvider configurationProvider)
	{
		var mal = new MAL(configurationProvider.getPageSize(), configurationProvider.getMalSize());
		var iteratorPlusPlus = new IteratorPlusPlus(configurationProvider.getStartDate(), configurationProvider.getAlgorithm(),
				new PageSupplier(sensorReadingRepository, configurationProvider.getAggregationWindowWidthMinutes()), mal,
				statistics);
		iterators.add(Pair.of(iteratorPlusPlus, configurationProvider.getEndDate()));
	}

	public void provideTestForAllIteratorsParallel()
	{
		var list = new ArrayList<CompletableFuture<Void>>();
		IntStream.range(0, iterators.size()).forEach(
				i -> list.add(CompletableFuture.runAsync(() -> runIterator(iterators.get(i)))));
		list.forEach(future -> {
			try
			{
				future.get();
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
			catch (ExecutionException e)
			{
				e.printStackTrace();
			}
		});
	}

	private void runIterator(final Pair<IteratorPlusPlus, LocalDateTime> pair)
	{
		var iterator = pair.getFirst();
		var endTime = pair.getSecond();
		allWatch.start();
		iterator.startWatch();
		do
		{
		} while (iterator.next().getStartTimestamp().isBefore(endTime));
		allWatch.stop();
		iterator.getStatistics().saveStatistics(allWatch.getLastTaskTimeMillis());
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
		for(final Pair<IteratorPlusPlus, LocalDateTime> pair : iterators){
			CompletableFuture.runAsync(()->runIterator(pair));
		}
		
	}

	public void runParallel()
	{
	}
}
