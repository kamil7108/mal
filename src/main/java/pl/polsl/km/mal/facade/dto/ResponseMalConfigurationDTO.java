package pl.polsl.km.mal.facade.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ResponseMalConfigurationDTO
{
	private Integer id;
	private Integer pageSize;
	private Integer malSize;
	private AlgorithmEnum algorithmEnum;
	private LocalDateTime startDate;
	private LocalDateTime endDate;
	private long aggregationWindowWidthMinutes;
}
