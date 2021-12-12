package pl.polsl.km.mal.data;

import java.time.LocalDateTime;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "meter_readings")
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
