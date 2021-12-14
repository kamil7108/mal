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
import pl.polsl.km.mal.algorithm.PageFillingAlgorithm;
import pl.polsl.km.mal.algorithm.RENEW;
import pl.polsl.km.mal.algorithm.SPARE;
import pl.polsl.km.mal.algorithm.TRIGG;

@Getter
public class RequestMalConfigurationDTO
{
	private final Integer pageSize;
	private final Integer malSize;
	private PageFillingAlgorithm algorithm;
	private final LocalDateTime startDate;
	private final LocalDateTime endDate;
	private final long aggregationWindowWidthMinutes;

	public RequestMalConfigurationDTO(final Integer pageSize, final Integer malSize, final AlgorithmEnum algorithm,
			final LocalDateTime startDate, final LocalDateTime endDate, final long aggregationWindowWidthMinutes)
	{
		this.pageSize = pageSize;
		this.malSize = malSize;
		this.startDate = startDate;
		this.endDate = endDate;
		this.aggregationWindowWidthMinutes = aggregationWindowWidthMinutes;
		if(algorithm.equals(AlgorithmEnum.SPARE)){
			this.algorithm = new SPARE();
		}
		else if(algorithm.equals(AlgorithmEnum.TRIGG)){
			this.algorithm = new TRIGG();
		}
		else if(algorithm.equals(AlgorithmEnum.RENEW)){
			this.algorithm = new RENEW();
		}
	}
}
