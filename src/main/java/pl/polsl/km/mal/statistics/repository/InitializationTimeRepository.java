package pl.polsl.km.mal.statistics.repository;

import java.util.UUID;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import pl.polsl.km.mal.statistics.data.InitializationTime;

@Repository
public interface InitializationTimeRepository extends MongoRepository<InitializationTime, UUID>
{
	InitializationTime findInitializationTimeByIteratorIdentifier(final UUID iteratorIdentifier);
}
