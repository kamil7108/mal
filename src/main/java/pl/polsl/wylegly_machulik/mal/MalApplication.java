package pl.polsl.wylegly_machulik.mal;

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
