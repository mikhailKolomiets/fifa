package site.fifa.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import site.fifa.entity.Player;
import site.fifa.entity.Team;

import java.util.List;

@Data
@AllArgsConstructor
public class TeamDTO {

    private int leaguePosition;
    private Team team;
    private List<Player> players;

}
