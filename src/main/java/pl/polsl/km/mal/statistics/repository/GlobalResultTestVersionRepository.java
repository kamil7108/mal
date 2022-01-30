package pl.polsl.km.mal.statistics.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import pl.polsl.km.mal.statistics.data.GlobalResultTestVersion;

@Repository
public interface GlobalResultTestVersionRepository extends MongoRepository<GlobalResultTestVersion, Integer>
{
	Optional<GlobalResultTestVersion> findTopByOrderByVersionDesc();
}
