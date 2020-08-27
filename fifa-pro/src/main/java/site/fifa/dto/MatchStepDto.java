package site.fifa.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import site.fifa.entity.Player;

@Data
@NoArgsConstructor
public class MatchStepDto {

    private Player firstPlayer;
    private Player secondPlayer;
    private int firstTeamChance = 50;
    private int secondTeamChance = 50;
    private boolean firstTeamBall = true;
    private int firstTeamAction;
    private int secondTeamAction;
    private int goalFirstTeam;
    private int goalSecondTeam;

    public void plusChance(int team) {
        if (team == 1 && firstTeamChance <= 100) {
            firstTeamChance += 5;
        } else if (secondTeamChance <= 100) {
            secondTeamChance += 5;
        }
    }

    public void minusChance(int team) {
        if (team == 1 && firstTeamChance > 20) {
            firstTeamChance -= 5;
        } else if (secondTeamChance > 20) {
            secondTeamChance -= 5;
        }
    }

    public String showGoals() {
        return goalFirstTeam + ":" + goalSecondTeam;
    }

    private int generateValueBetween (int first, int second) {
        return (int) (Math.random() * (second - first) + first);
    }

}
