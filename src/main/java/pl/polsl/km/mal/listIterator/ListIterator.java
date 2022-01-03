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
package pl.polsl.km.mal.listIterator;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import pl.polsl.km.mal.services.AggregateSupplierService;
import pl.polsl.km.mal.mal.Aggregate;
import pl.polsl.km.mal.statistics.Statistics;

public class ListIterator
{
	private final LocalDateTime startDate;
	private final LocalDateTime endDate;
	private final AggregateSupplierService aggregateSupplierService;
	private final Statistics statistics;
	private final List<Aggregate> aggregateList;
	private final LocalDateTime actualDate;
	private final AtomicInteger iterator = new AtomicInteger(0);
	private final UUID uuid;

	public ListIterator(final UUID uuid,final LocalDateTime startDate, final LocalDateTime endDate, final AggregateSupplierService aggregateSupplierService,
			final Statistics statistics)
	{
		this.startDate = startDate;
		this.endDate = endDate;
		this.aggregateSupplierService = aggregateSupplierService;
		this.statistics = statistics;
		this.uuid = uuid;
		aggregateList = new LinkedList<>();
		actualDate = startDate;
	}

	public Aggregate next(){
		aggregateList.add(aggregateSupplierService.getAggregateByDate(actualDate,iterator.getAndAdd(1)));
		return aggregateList.get(aggregateList.size()-1);
	}

	public Statistics getStatistics()
	{
		return statistics;
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