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
package pl.polsl.km.mal.testData.data;

import java.time.LocalDateTime;
import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

import lombok.Getter;

@Entity
@Table(indexes = {@Index(name = "timeStampIndex2", columnList = "startTimestamp, endTimestamp ASC ",unique = true)})
@Getter
public class MaterializedAggregate
{
	@Id
	private UUID uuid;
	private Integer waterLevelReadings;
	private LocalDateTime startTimestamp;
	private LocalDateTime endTimestamp;

	public MaterializedAggregate()
	{
	}

	public MaterializedAggregate(final UUID uuid, final Integer waterLevelReadings, final LocalDateTime startTimestamp,
			final LocalDateTime endTimestamp)
	{
		this.uuid = uuid;
		this.waterLevelReadings = waterLevelReadings;
		this.startTimestamp = startTimestamp;
		this.endTimestamp = endTimestamp;
	}
}
