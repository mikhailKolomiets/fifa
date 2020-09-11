package site.fifa.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import site.fifa.entity.Country;
import site.fifa.entity.Team;

import java.util.List;

@Repository
public interface TeamRepository extends CrudRepository<Team, Long> {

    Team findByName(String name);

    List<Team> getByCountryAndLeagueIdIsNull(Country country);

    @Modifying
    @Query("update Team t set t.leagueId = :lid where t.id = :id")
    void changeLeagueIdById(@Param(value = "lid") Long leagueId, @Param(value = "id") Long teamId);

}
