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
package pl.polsl.km.mal.statistics.data;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Document
@Getter
@Setter
@NoArgsConstructor
public class SingleRecordTime
{
	private UUID iteratorIdentifier;
	private List<Long> records = new LinkedList<>();

	public void setIteratorIdentifier(final UUID uuid)
	{
		this.iteratorIdentifier = uuid;
	}

	public int getRecordsSize()
	{
		return records.size();
	}

	public void addNewRecord(final Long time)
	{
		records.add(time);
	}
}
