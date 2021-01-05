package site.fifa.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
public class Habit {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private Long userId;
    private String name;
    private String description;
    private LocalDateTime lastUsage;
    private LocalDateTime preLastUsage;
    private long hiSeconds;
    @Transient
    @JsonIgnore
    private double allPercent;
}
