package pl.polsl.km.mal.facade.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.sun.istack.NotNull;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TestMaterializingDTO
{
	@NotNull
	@ApiModelProperty(name = "Numerous of MAL pages", position = 0, example = "10")
	private Integer malSize;
	@NotNull
	@ApiModelProperty(name = "Numerous of aggregates on MAL single page", position = 1, example = "500")
	private Integer pageSize;
	@NotNull
	@ApiModelProperty(name = "Algorithms under test", position = 2, example = "[\"SPARE\",\"TRIGG\",\"RENEW\"]")
	private List<String> algorithm;
	@NotNull
	@ApiModelProperty(name = "The maximum number of months to be processed by the iterator", position = 3, example = "48")
	private Integer aggregationTimeInMonths;
	@NotNull
	@ApiModelProperty(name = "Width of aggregation window in minutes", position = 4, example = "30")
	private long aggregationWindowWidthMinutes;
	@NotNull
	@ApiModelProperty(name = "Iterator start date of processing records", position = 5, example = "2022-01-01T00:00:00.000Z")
	private LocalDateTime startDate;
	@NotNull
	@ApiModelProperty(name = "Incrementation of number of month to process by iterator", position = 6, example = "1")
	private Integer step;
}
