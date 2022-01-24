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

import com.sun.istack.NotNull;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;

@Getter
public class RequestMalConfigurationDTO
{
	@NotNull
	@ApiModelProperty(name = "Numerous of aggregates on MAL single page", position = 0, example = "500")
	private final Integer pageSize;

	@NotNull
	@ApiModelProperty(name = "Numerous of MAL pages", position = 1, example = "10")
	private final Integer malSize;

	@NotNull
	@ApiModelProperty(name = "Algorithm use for iterator", position = 2, example = "SPARE")
	private final String algorithm;

	@NotNull
	@ApiModelProperty(name = "Iterator start date of processing records", position = 3, example = "2022-01-01T00:00:00.000Z")
	private final LocalDateTime startDate;

	@NotNull
	@ApiModelProperty(name = "Iterator end date of processing records", position = 4, example = "2023-01-01T00:00:00.000Z")
	private final LocalDateTime endDate;

	@NotNull
	@ApiModelProperty(name = "Width of aggregation window in minutes", position = 5, example = "30")
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
