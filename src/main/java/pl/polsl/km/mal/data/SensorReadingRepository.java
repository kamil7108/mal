package pl.polsl.km.mal.data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface SensorReadingRepository extends JpaRepository<SensorReading, UUID>
{
    @Query(value = "Select r.waterLevel from SensorReading r where r.timestamp >= ?1 and r.timestamp <= ?2")
    List<Integer> findAllWaterLevelsByTimestampBetween(LocalDateTime startDate, LocalDateTime endDate);
    List<ProjectionSensorReading> findAllByTimestampBetween(LocalDateTime startDate, LocalDateTime endDate);
}
