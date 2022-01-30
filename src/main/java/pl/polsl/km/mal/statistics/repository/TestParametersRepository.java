package pl.polsl.km.mal.statistics.repository;

import java.util.UUID;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import pl.polsl.km.mal.statistics.data.TestParameters;

@Repository
public interface TestParametersRepository extends MongoRepository<TestParameters, UUID>
{
	TestParameters findIteratorDataByUuid(final UUID uuid);
}
