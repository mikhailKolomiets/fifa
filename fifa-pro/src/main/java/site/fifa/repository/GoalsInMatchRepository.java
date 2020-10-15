package site.fifa.repository;

import org.springframework.data.repository.CrudRepository;
import site.fifa.entity.match.GoalsInMatch;

public interface GoalsInMatchRepository extends CrudRepository<GoalsInMatch, Long> {
}
