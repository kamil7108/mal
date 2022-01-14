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

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TestMalSizeDTO
{
	private Integer sizeIncrementation;
	private Integer minMalSize;
	private Integer maxMalSize;
	private Integer pageSize;
	private String algorithm;
	private LocalDateTime startDate;
	private LocalDateTime endDate;
	private long aggregationWindowWidthMinutes;
}
