package pl.polsl.km.mal.testData.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import pl.polsl.km.mal.testData.data.SensorReading;

@Repository
public interface SensorReadingRepository extends JpaRepository<SensorReading, UUID>
{
    List<SensorReading> findSensorReadingByTimestampBetween(LocalDateTime startDate, LocalDateTime endDate);
    @Query(value = "Select SUM(r.waterLevel) from SensorReading r where r.timestamp >= ?1 and r.timestamp <= ?2")
    Integer findWaterLevelsByTimestampBetween(LocalDateTime startDate, LocalDateTime endDate);
    List<ProjectionSensorReading> findAllByTimestampBetween(LocalDateTime startDate, LocalDateTime endDate);
    @Query(value = "Select r from SensorReading r where r.timestamp >= ?1 and r.timestamp <= ?2")
    List<SensorReading> findByTimestampBetween(LocalDateTime startDate, LocalDateTime endDate);
}
