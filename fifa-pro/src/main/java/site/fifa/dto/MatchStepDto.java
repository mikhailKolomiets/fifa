package site.fifa.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import site.fifa.entity.Player;
import site.fifa.entity.Stadium;

import java.awt.*;
import java.time.LocalDateTime;
import java.util.ArrayList;

@Data
@NoArgsConstructor
public class MatchStepDto {

    private MatchDto matchDto;
    private int step = -1;
    private int additionTime = -1;
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
    private StatisticDto statisticDto = new StatisticDto();
    private ArrayList<String> log = new ArrayList<>();
    private Point ballPosition = new Point();
    private Stadium stadium;
    private int additionHomeMaxChance;
    private int funs;
    private LocalDateTime timeoutTime;

    public void plusChance(int team, int amount) {
        if (team == 1 && firstTeamChance <= 100 + additionHomeMaxChance) {
            firstTeamChance += amount;
        } else if (secondTeamChance <= 100) {
            secondTeamChance += amount;
        }
    }

    public void minusChance(int team, int amount) {
        if (team == 1 && firstTeamChance > 20) {
            firstTeamChance -= amount;
        } else if (secondTeamChance > 20) {
            secondTeamChance -= amount;
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

    public void decreaseAdditionTime() {
        additionTime--;
    }

    public String showGoals() {
        return matchDto.getFirstTeam().getTeam().getName() + " \"" + goalFirstTeam + ":" + goalSecondTeam +"\" " + matchDto.getSecondTeam().getTeam().getName();
    }

    public void generateAdditionTime() {
        additionTime = (int) (Math.random() * 6);
    }

    public void nullableActionStep() {
        firstTeamAction = 0;
        secondTeamAction = 0;
    }

}
