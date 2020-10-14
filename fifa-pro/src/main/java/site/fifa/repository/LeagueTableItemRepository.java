package site.fifa.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import site.fifa.entity.LeagueTableItem;
import springfox.documentation.annotations.Cacheable;

import java.util.List;

@Repository
public interface LeagueTableItemRepository extends CrudRepository<LeagueTableItem, Long> {

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("update LeagueTableItem l set l.gamePlays = l.gamePlays + 1, l.win = l.win + :win, l.lose = l.lose + :lose, l.draw = l.draw + :draw, " +
            "l.goals = l.goals + :goals, l.goalLose = l.goalLose + :goalslose, l.point = l.point + :point where l.teamId = :teamid and l.leagueId = :leagueid")
    void increaseDataForLeagueTable(@Param("win") int win, @Param("lose") int lose, @Param("draw") int draw, @Param("goals") int goals,
                                    @Param("goalslose") int goalsLose, @Param("point") int point, @Param("teamid") Long teamId, @Param("leagueid") Long leagueId);

    List<LeagueTableItem> getByLeagueId(Long leagueId);

    List<LeagueTableItem> getByLeagueIdAndTeamId(Long leagueId, Long teamId);

    @Transactional
    @Modifying
    @Query("update LeagueTableItem l set l.position = :place where l.id = :id")
    void updatePosition(@Param("place") int place, @Param("id") Long id);


}
