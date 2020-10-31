package site.fifa.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import site.fifa.entity.Country;
import site.fifa.entity.Team;

import java.util.List;

@Repository
public interface TeamRepository extends CrudRepository<Team, Long> {

    Team findByName(String name);

    List<Team> getByCountryAndLeagueIdIsNull(Country country);

    @Transactional
    @Modifying
    @Query("update Team t set t.leagueId = :lid where t.id = :id")
    void changeLeagueIdById(@Param(value = "lid") Long leagueId, @Param(value = "id") Long teamId);

    List<Team> getByLeagueId(Long leagueId);

    @Transactional
    @Modifying
    @Query("update Team t set t.leagueId = null where t.leagueId = :league")
    void resetAllTeamsInLeague(@Param("league") Long leagueId);

}
