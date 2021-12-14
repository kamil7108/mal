package pl.polsl.km.mal.data;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface SensorReadingRepository extends JpaRepository<SensorReading, Integer>
{
    @Query("Select r.waterLevel from SensorReading r where r.timestamp >= ?1 and r.timestamp <= ?2")
    List<Integer> findAllByTimestampBetween(LocalDateTime startDate, LocalDateTime endDate);
}
