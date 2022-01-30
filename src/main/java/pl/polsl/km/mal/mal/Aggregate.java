package pl.polsl.km.mal.mal;

import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@Builder
@ToString
public class Aggregate implements Serializable
{
    private Integer sumOfWaterLevelReadings;
    private LocalDateTime startTimestamp;
    private LocalDateTime endTimestamp;
}
