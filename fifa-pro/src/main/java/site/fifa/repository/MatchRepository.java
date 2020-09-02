package site.fifa.repository;

import org.springframework.data.repository.CrudRepository;
import site.fifa.entity.match.MatchPlay;
import site.fifa.entity.match.MatchStatus;

import java.util.List;

public interface MatchRepository extends CrudRepository<MatchPlay, Long> {

    List<MatchPlay> getByFirstTeamIdAndSecondTeamIdAndStatus(Long firstTeamId, Long secondTeamId, MatchStatus status);

}
