package site.fifa.repository;

import org.springframework.data.repository.CrudRepository;
import site.fifa.entity.League;

public interface LeagueRepository extends CrudRepository<League, Long> {
}
