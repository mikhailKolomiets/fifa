package site.fifa.dto.mappers;

import org.springframework.jdbc.core.RowMapper;
import site.fifa.dto.StatisticDto;

import java.awt.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

public class StatisticDtoRowMapper implements RowMapper<StatisticDto> {
    @Override
    public StatisticDto mapRow(ResultSet resultSet, int i) throws SQLException {
        StatisticDto result = new StatisticDto();
        result.setMatchId(resultSet.getLong("match_id"));
        result.setFirstTeamName(resultSet.getString("ftm"));
        result.setSecondTeamName(resultSet.getString("stm"));
        result.setGoals(new Point(resultSet.getInt("ft_goals"),resultSet.getInt("st_goals")));
        result.setGoalKick(new Point(resultSet.getInt("ft_goal_kick"), resultSet.getInt("st_goal_kick")));
        result.setPercentageHoldBall(new Point(resultSet.getInt("ft_percentage_hold_ball"), resultSet.getInt("st_percentage_hold_ball")));
        result.setDate(LocalDate.parse(resultSet.getString("started")));
        return result;
    }
}
