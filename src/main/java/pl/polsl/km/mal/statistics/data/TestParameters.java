package pl.polsl.km.mal.statistics.data;

import java.util.UUID;

import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Builder;
import lombok.Getter;
import pl.polsl.km.mal.facade.dto.AlgorithmEnum;

@Document
@Builder
@Getter
public class TestParameters
{
	private final UUID uuid;
	private final Type type;
	private final AlgorithmEnum algorithmEnum;
	private final int pageSize;
	private final int malSize;
	private final long aggregationTime;
	private final long aggregationTimeWindow;
}
