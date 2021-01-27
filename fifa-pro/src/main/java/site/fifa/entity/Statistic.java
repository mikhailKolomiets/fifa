package site.fifa.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import site.fifa.dto.StatisticDto;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
@Data
@NoArgsConstructor
public class Statistic {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private Long matchId;
    private int ftGoals;
    private int stGoals;
    private int ftGoalKick;
    private int stGoalKick;
    private int ftPercentageHoldBall;
    private int stPercentageHoldBall;
    private int funs;

    // TODO: 9/12/20  add cards anf foul fields

    public Statistic(Long matchId, StatisticDto statisticDto, int funs) {
        this.matchId = matchId;
        this.ftGoals = statisticDto.getGoals().x;
        this.stGoals = statisticDto.getGoals().y;
        this.ftGoalKick = statisticDto.getGoalKick().x;
        this.stGoalKick = statisticDto.getGoalKick().y;
        this.ftPercentageHoldBall = statisticDto.getPercentageHoldBall().x;
        this.stPercentageHoldBall = statisticDto.getPercentageHoldBall().y;
        this.funs = funs;
    }

}
