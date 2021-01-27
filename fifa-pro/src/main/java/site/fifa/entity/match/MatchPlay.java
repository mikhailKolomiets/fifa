package site.fifa.entity.match;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
public class MatchPlay {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private MatchStatus status;
    private MatchType type;
    private LocalDate started;
    private Long firstTeamId;
    private Long secondTeamId;
    private int funs;

    public MatchPlay(MatchStatus status, MatchType type, LocalDate started, Long firstTeamId, Long secondTeamId) {
        this.status = status;
        this.type = type;
        this.started = started;
        this.firstTeamId = firstTeamId;
        this.secondTeamId = secondTeamId;
    }

}
