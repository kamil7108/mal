package pl.polsl.km.mal.facade.dto;

import java.time.LocalDateTime;

import com.sun.istack.NotNull;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;

@Getter
public class TestPageSizeDTO
{
	@NotNull
	@ApiModelProperty(name = "Incrementation of mal page size between single tests", position = 0, example = "10")
	private Integer pageIncrementation;
	@NotNull
	@ApiModelProperty(name = "Minimal mal page size", position = 1, example = "10")
	private Integer minPageSize;
	@NotNull
	@ApiModelProperty(name = "Maximum mal page size", position = 2, example = "1000")
	private Integer maxPageSize;
	@NotNull
	@ApiModelProperty(name = "Mal size", position = 3, example = "10")
	private Integer malSize;
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

	public TestPageSizeDTO()
	{
	}

	public TestPageSizeDTO(final Integer pageIncrementation, final Integer minPageSize, final Integer maxPageSize,
			final Integer malSize, final String algorithm, final LocalDateTime startDate, final LocalDateTime endDate,
			final long aggregationWindowWidthMinutes)
	{
		this.pageIncrementation = pageIncrementation;
		this.minPageSize = minPageSize;
		this.maxPageSize = maxPageSize;
		this.malSize = malSize;
		this.algorithm = algorithm;
		this.startDate = startDate;
		this.endDate = endDate;
		this.aggregationWindowWidthMinutes = aggregationWindowWidthMinutes;
	}
}
