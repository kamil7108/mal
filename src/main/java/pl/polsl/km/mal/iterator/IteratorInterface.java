package pl.polsl.km.mal.iterator;

import pl.polsl.km.mal.mal.Aggregate;
import pl.polsl.km.mal.statistics.TestScenarioStatistics;

public interface IteratorInterface
{
	Aggregate next();
	TestScenarioStatistics getStatistics();
}
