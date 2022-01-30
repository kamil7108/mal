package pl.polsl.km.mal.statistics.repository;

import java.util.UUID;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import pl.polsl.km.mal.statistics.data.ListIteratorData;

@Repository
public interface ListIteratorDataRepository extends MongoRepository<ListIteratorData, UUID>
{
	ListIteratorData findListIteratorDataByUuid(final UUID uuid);
}
