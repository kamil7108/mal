package pl.polsl.km.mal.statistics.data;

import java.util.UUID;

import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Document
@AllArgsConstructor
@Getter
public class InitializationTime
{
	private final UUID iteratorIdentifier;
	private final Long initializationTime;
}
