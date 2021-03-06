package site.fifa.repository;

import org.springframework.data.repository.CrudRepository;
import site.fifa.entity.match.GoalsInMatch;

import java.util.List;

public interface GoalsInMatchRepository extends CrudRepository<GoalsInMatch, Long> {

    List<GoalsInMatch> getByMatchId(Long matchId);
    List<GoalsInMatch> getByLeagueId(Long leagueId);

}
