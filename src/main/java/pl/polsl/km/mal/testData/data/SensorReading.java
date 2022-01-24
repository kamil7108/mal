package pl.polsl.km.mal.testData.data;

import java.time.LocalDateTime;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.MappedSuperclass;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Table(name = "meter_readings",indexes = {
        @Index(name = "timeStampIndex", columnList = "time_stamp ASC",unique = true)
})
@Entity
public class SensorReading {
    @Id
    private UUID uuid;
    @Column(name="guage_id")
    private int guageId;
    @Column(name="water_level")
    private int waterLevel;
    @Column(name="time_stamp")
    private LocalDateTime timestamp;
}
