package site.fifa.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import site.fifa.entity.Player;
import site.fifa.entity.Team;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TeamDTO {

    private int leaguePosition;
    private Team team;
    private List<Player> players = new ArrayList<>();
    private List<Player> reserve = new ArrayList<>();

    public TeamDTO buildLeaguePosition(int leaguePosition) {
        this.leaguePosition = leaguePosition;
        return this;
    }

    public TeamDTO buildTeam(Team team) {
        this.team = team;
        return this;
    }

    public TeamDTO buildPlayers(List<Player> players) {
        this.players = players;
        return this;
    }

    public TeamDTO buildReserve(List<Player> reserve) {
        this.reserve = reserve;
        return this;
    }

}
