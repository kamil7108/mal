package pl.polsl.wylegly_machulik.mal.data;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SensorReadingRepository extends CrudRepository<SensorReading, Integer>
{
    List<SensorReading> findAllByTimestampBetween(LocalDateTime startDate, LocalDateTime endDate);
}
