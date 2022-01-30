package pl.polsl.km.mal.statistics.data;

import java.util.UUID;

import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Builder;
import lombok.Getter;

@Document
@Builder
@Getter
public class ListIteratorData
{
	private final UUID uuid;
	private final Type type;
	private final long aggregationTime;
	private final long aggregationTimeWindow;
	private final boolean endWithError;
	private final int numberOfProcessedElements;
	private final int aggregateSizeInBytes;
}
