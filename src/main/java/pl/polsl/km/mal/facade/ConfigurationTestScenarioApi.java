package pl.polsl.km.mal.facade;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.AllArgsConstructor;
import pl.polsl.km.mal.facade.dto.RequestMalConfigurationDTO;
import pl.polsl.km.mal.facade.dto.ResponseMalConfigurationDTO;
import pl.polsl.km.mal.facade.dto.TestAlgorithmDTO;
import pl.polsl.km.mal.facade.dto.TestMalSizeDTO;
import pl.polsl.km.mal.facade.dto.TestMaterializingDTO;
import pl.polsl.km.mal.facade.dto.TestPageSizeDTO;
import pl.polsl.km.mal.services.TestService;

@RestController
@RequestMapping("/iterator")
@AllArgsConstructor
@Api(description = "Test Scenario", tags = "Test Scenario")
public class ConfigurationTestScenarioApi
{
	private final TestService testService;

	@Operation(summary = "Configure a new mal iterator use in manual created scenario.",//
			responses = {//
					@ApiResponse(responseCode = "201", description = "New iterator has been created with returned UUID"),//
					@ApiResponse(responseCode = "500", description = "An unexpected error has occurred.")//
			})
	@PostMapping
	@JsonSerialize(using = LocalDateTimeSerializer.class)
	public ResponseEntity<UUID> createNewMal(final @RequestBody RequestMalConfigurationDTO configuration)
	{
		return new ResponseEntity<>(testService.createNewIterator(configuration), HttpStatus.CREATED);
	}

	@Operation(summary = "Get all configured mal iterators for manual created scenario.",//
			responses = {//
					@ApiResponse(responseCode = "200", description = "Successfully returned list of initialized iterators"),//
					@ApiResponse(responseCode = "500", description = "An unexpected error has occurred.")//
			})
	@GetMapping
	public ResponseEntity<List<ResponseMalConfigurationDTO>> getAllIterators()
	{
		return ResponseEntity.of(Optional.ofNullable(testService.getAllIterators()));
	}

	@Operation(summary = "Run manual created scenario of initialized iterators in sequence",//
			responses = {//
					@ApiResponse(responseCode = "200", description = "Return direction of reports"),//
					@ApiResponse(responseCode = "500", description = "An unexpected error has occurred.")//
			})
	@PutMapping("/runInSequence")
	@ResponseBody
	public ResponseEntity<String> runInSequence(
			@RequestHeader @ApiParam(name = "testName", type = "String", value = "Self configured test scenario name", example =
					"Page size test", required = true) String testName,
			@RequestHeader @ApiParam(name = "useMaterializedData", type = "Boolean", value = "false", required = true) Boolean useMaterializedData)
	{
		var dir = testService.runInSequence(testName, useMaterializedData);
		return ResponseEntity.of(Optional.ofNullable(dir));
	}

	@Operation(summary = "Run manual created scenario of initialized iterators in parallel mode",//
			responses = {//
					@ApiResponse(responseCode = "200", description = "Return direction of reports"),//
					@ApiResponse(responseCode = "500", description = "An unexpected error has occurred.")//
			})
	@PutMapping("/runParallel")
	@ResponseBody
	public ResponseEntity<String> runParallel(
			@RequestHeader @ApiParam(name = "testName", type = "String", value = "Self configured test scenario name", example =
					"Page size test", required = true) String testName)
	{
		var dir = testService.runParallel(testName);
		return ResponseEntity.of(Optional.ofNullable(dir));
	}

	@Operation(summary = "Run test scenario investigated the influence of page size on mal work",//
			responses = {//
					@ApiResponse(responseCode = "200", description = "Return direction of reports"),//
					@ApiResponse(responseCode = "500", description = "An unexpected error has occurred.")//
			})
	@PutMapping("/testPageSize")
	public ResponseEntity<String>  testPageSize(@RequestBody TestPageSizeDTO dto)
	{
		var testName = "Page size test";
		var dir = testService.testPageSize(dto, testName);
		return ResponseEntity.of(Optional.ofNullable(dir));
	}

	@Operation(summary = "Run test scenario investigated the influence of mal size on mal work",//
			responses = {//
					@ApiResponse(responseCode = "200", description = "Return direction of reports"),//
					@ApiResponse(responseCode = "500", description = "An unexpected error has occurred.")//
			})
	@PutMapping("/testMalSize")
	public ResponseEntity<String> testMalSize(@RequestBody TestMalSizeDTO dto)
	{
		var testName = "Mal size test";
		var dir = testService.testMalSize(dto, testName);
		return ResponseEntity.of(Optional.ofNullable(dir));
	}

	@Operation(summary = "Run test scenario investigated the influence of page filling algorithm on mal work",//
			responses = {//
					@ApiResponse(responseCode = "200", description = "Return direction of reports"),//
					@ApiResponse(responseCode = "500", description = "An unexpected error has occurred.")//
			})
	@PutMapping("/testAlgorithmInfluence")
	public ResponseEntity<String> testAlgorithmInfluence(@RequestBody TestAlgorithmDTO dto)
	{
		var testName = "Algorithm influence test";
		var dir =testService.testAlgorithmInfluence(dto, testName);
		return ResponseEntity.of(Optional.ofNullable(dir));
	}

	@Operation(summary = "Run test scenario investigated the influence of materialization on mal work",//
			responses = {//
					@ApiResponse(responseCode = "200", description = "Successfully passed scenario"),//
					@ApiResponse(responseCode = "500", description = "An unexpected error has occurred.")//
			})
	@PutMapping("/testMaterializingInfluence")
	public void testMaterializingInfluence(@RequestBody TestMaterializingDTO dto)
	{
		var testName = "Materializning influence test";
		testService.testMaterializingInfluence(dto, testName);
	}

}
