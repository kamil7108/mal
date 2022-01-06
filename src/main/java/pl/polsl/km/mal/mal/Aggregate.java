package pl.polsl.km.mal.mal;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

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
    /**
     * Just for test
     */
    private Boolean isAllReadyRead;
}
