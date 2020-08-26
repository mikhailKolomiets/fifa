package site.fifa.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import site.fifa.entity.Player;

@Repository
public interface PlayerRepository extends CrudRepository<Player, Long> {



}
