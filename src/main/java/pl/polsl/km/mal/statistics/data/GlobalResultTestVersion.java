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

import javax.persistence.Id;

import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Document
@AllArgsConstructor
@Setter
@Getter
public class GlobalResultTestVersion
{
	@Id
	private final Integer version;
	private final String testResultName;
}
