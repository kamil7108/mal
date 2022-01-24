package pl.polsl.km.mal;

import java.io.IOException;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.convert.threeten.Jsr310JpaConverters;

@SpringBootApplication
@EntityScan(basePackageClasses = { MalApplication.class, Jsr310JpaConverters.class })
public class MalApplication
{
    public static void main(String[] args)
    {
        SpringApplication.run(MalApplication.class, args);
    }
}
