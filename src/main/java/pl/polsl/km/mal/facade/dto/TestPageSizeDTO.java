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
public class TestPageSizeDTO
{
	private Integer pageIncrementation;
	private Integer minPageSize;
	private Integer maxPageSize;
	private Integer malSize;
	private String algorithm;
	private LocalDateTime startDate;
	private LocalDateTime endDate;
	private long aggregationWindowWidthMinutes;

	public TestPageSizeDTO()
	{
	}

	public TestPageSizeDTO(final Integer pageIncrementation, final Integer minPageSize, final Integer maxPageSize,
			final Integer malSize, final String algorithm,final LocalDateTime startDate, final LocalDateTime endDate,
			final long aggregationWindowWidthMinutes)
	{
		this.pageIncrementation = pageIncrementation;
		this.minPageSize = minPageSize;
		this.maxPageSize = maxPageSize;
		this.malSize = malSize;
		this.algorithm = algorithm;
		this.startDate = startDate;
		this.endDate = endDate;
		this.aggregationWindowWidthMinutes = aggregationWindowWidthMinutes;
	}
}
