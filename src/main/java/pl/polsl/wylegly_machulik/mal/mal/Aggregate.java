package pl.polsl.wylegly_machulik.mal.mal;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@Builder
@ToString
public class Aggregate {
    private List<Integer> waterLevelReadings;
    private LocalDateTime startTimestamp;
    private LocalDateTime endTimestamp;
    /**
     * Just for test
     */
    private Boolean isAllReadyRead;
}
