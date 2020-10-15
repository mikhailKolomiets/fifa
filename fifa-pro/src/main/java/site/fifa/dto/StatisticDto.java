package site.fifa.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import site.fifa.entity.match.GoalsInMatch;

import java.awt.*;
import java.util.ArrayList;

/**
 * Represent the statistic between 2 teams in the match.
 * Can use as some point by action with associate on x as first team and y as second team.
 */
@Data
@NoArgsConstructor
public class StatisticDto {

    private String firstTeamName;
    private String secondTeamName;
    private Point goals = new Point();
    private Point goalKick = new Point();
    private Point percentageHoldBall= new Point();
    private Point yellowCard = new Point();
    private Point redCard = new Point();
    private Point foul = new Point();
    private Point stepHold = new Point();
    private ArrayList<GoalsInMatch> goalsList = new ArrayList<>();

    /**
     * calculate the percentage ball hold to this according the step ball hold team
     * @param firstTeam - true is a ball in this step has a first team and another false
     */
    public void countPercentageByTeamHold(boolean firstTeam) {
            if (firstTeam) {
                stepHold.x++;
            } else {
                stepHold.y++;
            }

            percentageHoldBall.x =  (int) ((double) stepHold.x / (stepHold.x + stepHold.y) * 100);
            percentageHoldBall.y = 100 - percentageHoldBall.x;
    }

}
