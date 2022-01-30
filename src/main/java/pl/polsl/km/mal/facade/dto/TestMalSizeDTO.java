package pl.polsl.km.mal.facade.dto;

import java.time.LocalDateTime;

import com.sun.istack.NotNull;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TestMalSizeDTO
{
	@NotNull
	@ApiModelProperty(name = "Incrementation of mal size between single tests", position = 0, example = "10")
	private Integer sizeIncrementation;
	@NotNull
	@ApiModelProperty(name = "Minimal mal size", position = 1, example = "10")
	private Integer minMalSize;
	@NotNull
	@ApiModelProperty(name = "Maximum mal size", position = 2, example = "1000")
	private Integer maxMalSize;
	@NotNull
	@ApiModelProperty(name = "Numerous of aggregates on MAL single page", position = 3, example = "500")
	private Integer pageSize;
	@NotNull
	@ApiModelProperty(name = "Algorithm use for iterator", position = 4, example = "SPARE")
	private String algorithm;
	@NotNull
	@ApiModelProperty(name = "Iterator start date of processing records", position = 5, example = "2022-01-01T00:00:00.000Z")
	private LocalDateTime startDate;
	@NotNull
	@ApiModelProperty(name = "Iterator end date of processing records", position = 6, example = "2023-01-01T00:00:00.000Z")
	private LocalDateTime endDate;
	@NotNull
	@ApiModelProperty(name = "Width of aggregation window in minutes", position = 7, example = "30")
	private long aggregationWindowWidthMinutes;
}
