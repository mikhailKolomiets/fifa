package site.fifa.entity.match;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import site.fifa.entity.Player;
import site.fifa.entity.Team;

import javax.persistence.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GoalsInMatch {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private Long matchId;
    @OneToOne
    @JoinColumn(name = "team")
    private Team team;
    @OneToOne
    @JoinColumn(name = "player")
    private Player player;
    private int gameTime;
    private Long leagueId;
}
