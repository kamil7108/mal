package pl.polsl.km.mal.listIterator;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.polsl.km.mal.mal.Aggregate;
import pl.polsl.km.mal.services.AggregateSupplierService;
import pl.polsl.km.mal.statistics.TestScenarioStatistics;

public class ListIterator
{
	private final Logger LOG = LoggerFactory.getLogger(ListIterator.class);
	private final LocalDateTime startDate;
	private final LocalDateTime endDate;
	private final AggregateSupplierService aggregateSupplierService;
	private final TestScenarioStatistics testScenarioStatistics;
	private final List<Aggregate> aggregateList;
	private final LocalDateTime actualDate;
	private final AtomicInteger iterator = new AtomicInteger(0);
	private final UUID uuid;

	public ListIterator(final UUID uuid,final LocalDateTime startDate, final LocalDateTime endDate, final AggregateSupplierService aggregateSupplierService,
			final TestScenarioStatistics testScenarioStatistics)
	{
		this.startDate = startDate;
		this.endDate = endDate;
		this.aggregateSupplierService = aggregateSupplierService;
		this.testScenarioStatistics = testScenarioStatistics;
		this.uuid = uuid;
		aggregateList = new LinkedList<>();
		actualDate = startDate;
	}

	public Aggregate next(){
		aggregateList.add(aggregateSupplierService.getAggregateByDate(actualDate,iterator.getAndAdd(1)));
		var result = aggregateList.get(aggregateList.size()-1);
		LOG.info("# {} Retrieved aggregate: {}", iterator.get(), result);
		return aggregateList.get(aggregateList.size()-1);
	}

	public TestScenarioStatistics getStatistics()
	{
		return testScenarioStatistics;
	}

	public LocalDateTime getStartDate()
	{
		return startDate;
	}

	public LocalDateTime getEndDate()
	{
		return endDate;
	}

	public UUID getUuid()
	{
		return uuid;
	}

	public long getAggregationWindowTimeInMinutes()
	{
		return aggregateSupplierService.aggregationWindowWidthMinutes;
	}
}