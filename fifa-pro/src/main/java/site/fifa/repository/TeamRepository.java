package site.fifa.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import site.fifa.entity.Team;

@Repository
public interface TeamRepository extends CrudRepository<Team, Long> {
}
