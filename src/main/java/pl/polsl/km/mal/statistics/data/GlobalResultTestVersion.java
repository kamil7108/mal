package pl.polsl.km.mal.statistics.data;

import javax.persistence.Id;

import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Document
@AllArgsConstructor
@Setter
@Getter
public class GlobalResultTestVersion
{
	@Id
	private final Integer version;
	private final String testResultName;
}
