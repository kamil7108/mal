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
