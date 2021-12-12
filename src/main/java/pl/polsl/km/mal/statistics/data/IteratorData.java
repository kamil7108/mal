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

import java.util.UUID;

import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Builder;
import lombok.Getter;
import pl.polsl.km.mal.facade.Algorithm;

@Document
@Builder
@Getter
public class IteratorData
{
	private final UUID uuid;
	private final Type type;
	private final Algorithm algorithm;
	private final int pageSize;
	private final int malSize;
	private final long aggregationTime;
	private final long aggregationTimeWindow;
}
