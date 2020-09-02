package site.fifa.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import site.fifa.entity.Player;

import java.util.ArrayList;

@Data
@NoArgsConstructor
public class MatchStepDto {

    private MatchDto matchDto;
    private int step = -1;
    private int position = 2;
    private Player firstPlayer;
    private Player secondPlayer;
    private int firstTeamChance = 50;
    private int secondTeamChance = 50;
    private boolean firstTeamBall = true;
    private int firstTeamAction;
    private int secondTeamAction;
    private int goalFirstTeam;
    private int goalSecondTeam;
    private String lastStepLog = step + " m: Матч начался";
    private ArrayList<String> log = new ArrayList<>();

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

    public void increaseStep() {
        step++;
    }

    public void increaseGoal(int team) {
        if (team == 1) {
            goalFirstTeam++;
        } else {
            goalSecondTeam++;
        }
    }

    public String showGoals() {
        return matchDto.getFirstTeam().getTeam().getName() + " \"" + goalFirstTeam + ":" + goalSecondTeam +"\" " + matchDto.getSecondTeam().getTeam().getName();
    }

}
