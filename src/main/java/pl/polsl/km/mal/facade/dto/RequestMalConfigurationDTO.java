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
package pl.polsl.km.mal.facade.dto;

import java.time.LocalDateTime;

import lombok.Getter;

@Getter
public class RequestMalConfigurationDTO
{
	private final Integer pageSize;
	private final Integer malSize;
	private final String algorithm;
	private final LocalDateTime startDate;
	private final LocalDateTime endDate;
	private final long aggregationWindowWidthMinutes;

	public RequestMalConfigurationDTO(final Integer pageSize, final Integer malSize, final String algorithm,
			final LocalDateTime startDate, final LocalDateTime endDate, final long aggregationWindowWidthMinutes)
	{
		this.pageSize = pageSize;
		this.malSize = malSize;
		this.startDate = startDate;
		this.endDate = endDate;
		this.aggregationWindowWidthMinutes = aggregationWindowWidthMinutes;
		this.algorithm = algorithm;
	}
}
