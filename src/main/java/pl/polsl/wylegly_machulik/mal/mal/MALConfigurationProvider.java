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
package pl.polsl.wylegly_machulik.mal.mal;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;

import lombok.Getter;
import pl.polsl.wylegly_machulik.mal.algorithm.PageFillingAlgorithm;
import pl.polsl.wylegly_machulik.mal.algorithm.RENEW;
import pl.polsl.wylegly_machulik.mal.algorithm.SPARE;
import pl.polsl.wylegly_machulik.mal.algorithm.TRIGG;
import pl.polsl.wylegly_machulik.mal.facade.Algorithm;

@Getter
public class MALConfigurationProvider
{
	private final Integer pageSize;
	private final Integer malSize;
	private PageFillingAlgorithm algorithm;
	private final LocalDateTime startDate;
	private final LocalDateTime endDate;
	private final long aggregationWindowWidthMinutes;

	public MALConfigurationProvider(final Integer pageSize, final Integer malSize, final Algorithm algorithm,
			final LocalDateTime startDate, final LocalDateTime endDate, final long aggregationWindowWidthMinutes)
	{
		this.pageSize = pageSize;
		this.malSize = malSize;
		this.startDate = startDate;
		this.endDate = endDate;
		this.aggregationWindowWidthMinutes = aggregationWindowWidthMinutes;
		if(algorithm.equals(Algorithm.SPARE)){
			this.algorithm = new SPARE();
		}
		else if(algorithm.equals(Algorithm.TRIGG)){
			this.algorithm = new TRIGG();
		}
		else if(algorithm.equals(Algorithm.RENEW)){
			this.algorithm = new RENEW();
		}
	}
}
