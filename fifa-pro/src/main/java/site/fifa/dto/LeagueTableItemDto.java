package site.fifa.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import site.fifa.entity.LeagueTableItem;
import site.fifa.entity.Team;

@Data
@NoArgsConstructor
public class LeagueTableItemDto {

    private String leagueName;
    private int playGames;
    private int position;
    private int points;
    private int wins;
    private int loses;
    private int draw;
    private int goals;
    private int goalsLose;
    private Team team;

    public LeagueTableItemDto(String leagueName, LeagueTableItem l, Team team) {
        this.leagueName = leagueName;
        this.position = l.getPosition();
        this.points = l.getPoint();
        this.wins = l.getWin();
        this.loses = l.getLose();
        this.draw = l.getDraw();
        this.goals = l.getGoals();
        this.goalsLose = l.getGoalLose();
        this.team = team;
        this.playGames = l.getGamePlays();
    }
}
