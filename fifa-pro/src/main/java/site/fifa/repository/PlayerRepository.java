package site.fifa.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import site.fifa.entity.Player;

import java.util.List;

@Repository
public interface PlayerRepository extends CrudRepository<Player, Long> {

    List<Player> getByTeamId(Long teamId);

    List<Player> getByTeamIdIsNull();

    @Query(nativeQuery = true, value = "select * from player p where p.price > 0 and p.reserve = 1")
    List<Player> getAllFree();

}
