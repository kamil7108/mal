package pl.polsl.km.mal.testData.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import pl.polsl.km.mal.testData.data.MaterializedAggregate;

@Repository
public interface MaterializedAggregateRepository extends JpaRepository<MaterializedAggregate, UUID>
{
	@Query(value = "Select m from MaterializedAggregate m where m.startTimestamp BETWEEN :startDate AND :endDate ORDER BY m.startTimestamp ASC")
	Optional<List<MaterializedAggregate>> getAllBetweenDates(LocalDateTime startDate, LocalDateTime endDate);
}
