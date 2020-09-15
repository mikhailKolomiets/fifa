package site.fifa.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
@Data
@NoArgsConstructor
public class LeagueTableItem {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private Long leagueId;
    private Long teamId;

    private int position;
    private int gamePlays;
    private int win;
    private int lose;
    private int draw;
    private int goals;
    private int goalLose;
    private int point;

    public LeagueTableItem(Long leagueId, Long teamId) {
        this.leagueId = leagueId;
        this.teamId = teamId;
    }
}
