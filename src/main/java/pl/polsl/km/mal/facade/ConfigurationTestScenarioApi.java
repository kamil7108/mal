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
package pl.polsl.km.mal.facade;

import java.util.List;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

import lombok.AllArgsConstructor;
import pl.polsl.km.mal.TestService;
import pl.polsl.km.mal.mal.MALConfigurationProvider;

@RestController
@RequestMapping("/iterator")
@AllArgsConstructor
public class ConfigurationTestScenarioApi
{
	private final TestService testService;

	@PostMapping
	@JsonSerialize(using = LocalDateTimeSerializer.class)
	public void createNewMal(final @RequestBody MALConfigurationProvider configuration)
	{
		testService.createNewIterator(configuration);
	}

	@GetMapping
	public ResponseEntity<List<ResponseMalConfigurationDTO>> getAllIterators()
	{
		return ResponseEntity.of(Optional.ofNullable(testService.getAllIterators()));
	}

	@PutMapping("/runInSequence")
	public void runInSequence()
	{
		testService.runInSequence();
	}

	@PutMapping("/runParallel")
	public void runParallel()
	{
		testService.runParallel();
	}
}
