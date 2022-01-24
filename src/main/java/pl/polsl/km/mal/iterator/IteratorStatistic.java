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
package pl.polsl.km.mal.iterator;

import lombok.Getter;
import pl.polsl.km.mal.statistics.TestScenarioStatistics;

@Getter
public class IteratorStatistic
{
	TestScenarioStatistics testScenarioStatistics;

	IteratorStatistic(final TestScenarioStatistics testScenarioStatistics)
	{
		this.testScenarioStatistics = testScenarioStatistics;
	}
}

