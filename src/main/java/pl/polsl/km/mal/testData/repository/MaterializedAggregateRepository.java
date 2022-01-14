/*
 * [y] hybris Platform
 *
 * Copyright (c) 2021 SAP SE or an SAP affiliate company.  All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with SAP.
 */
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
	Optional<List<MaterializedAggregate>> findByStartTimestampAndEndTimestamp(LocalDateTime startDate, LocalDateTime endDate);
}
