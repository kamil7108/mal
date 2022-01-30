package pl.polsl.km.mal.statistics.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import pl.polsl.km.mal.statistics.data.SingleRecordTime;

@Repository
public interface SingleRecordTimeRepository extends MongoRepository<SingleRecordTime, Long>
{
	List<SingleRecordTime> findSingleRecordTimesByIteratorIdentifier(final UUID iteratorIdentifier);
}
