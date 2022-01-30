package pl.polsl.km.mal.facade;

import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

@Configuration
public class ConfigLocalDataTimeBean
{
	private static final String dateTimeFormat = "yyyy-MM-dd HH:mm:ss";

	@Bean
	public Jackson2ObjectMapperBuilderCustomizer jsonCustomizer()
	{
		return builder -> {
			builder.simpleDateFormat(dateTimeFormat);
			builder.serializers(LocalDateTimeSerializer.INSTANCE);
			builder.deserializers(LocalDateTimeDeserializer.INSTANCE);
		};
	}
}

