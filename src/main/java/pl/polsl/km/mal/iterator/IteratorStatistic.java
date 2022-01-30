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

