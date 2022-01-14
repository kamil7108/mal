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
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

import lombok.AllArgsConstructor;
import pl.polsl.km.mal.facade.dto.TestAlgorithmDTO;
import pl.polsl.km.mal.facade.dto.TestMalSizeDTO;
import pl.polsl.km.mal.facade.dto.TestPageSizeDTO;
import pl.polsl.km.mal.services.TestService;
import pl.polsl.km.mal.facade.dto.RequestMalConfigurationDTO;
import pl.polsl.km.mal.facade.dto.ResponseMalConfigurationDTO;

@RestController
@RequestMapping("/iterator")
@AllArgsConstructor
public class ConfigurationTestScenarioApi
{
	private final TestService testService;

	@PostMapping
	@JsonSerialize(using = LocalDateTimeSerializer.class)
	public ResponseEntity<UUID> createNewMal(final @RequestBody RequestMalConfigurationDTO configuration)
	{
		return new ResponseEntity<>(testService.createNewIterator(configuration), HttpStatus.CREATED);
	}

	@GetMapping
	public ResponseEntity<List<ResponseMalConfigurationDTO>> getAllIterators()
	{
		return ResponseEntity.of(Optional.ofNullable(testService.getAllIterators()));
	}

	@PutMapping("/runInSequence")
	@ResponseBody
	public void runInSequence(@RequestHeader String testName,@RequestHeader Boolean useMaterializedData)
	{
		testService.runInSequence(testName,useMaterializedData);
	}

	@PutMapping("/runParallel")
	@ResponseBody
	public void runParallel(@RequestHeader String testName)
	{
		testService.runParallel(testName);
	}

	@PutMapping("/testPageSize")
	public void testPageSize(@RequestBody TestPageSizeDTO dto){

		testService.testPageSize(dto);
	}

	@PutMapping("/testMalSize")
	public void testMalSize(@RequestBody TestMalSizeDTO dto){
		testService.testMalSize(dto);
	}

	@PutMapping("/testAlgorithmInfluence")
	public void testAlgorithmInfluence(@RequestBody TestAlgorithmDTO dto){
		testService.testAlgorithmInfluence(dto);
	}

	@PutMapping("/testMaterializingInfluence")
	public void testMaterializingInfluence(@RequestBody TestAlgorithmDTO dto){
		testService.testMaterializingInfluence(dto);
	}

}
