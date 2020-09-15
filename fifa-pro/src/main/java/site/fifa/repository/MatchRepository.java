package site.fifa.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import site.fifa.entity.match.MatchPlay;
import site.fifa.entity.match.MatchStatus;

import java.time.LocalDate;
import java.util.List;

public interface MatchRepository extends CrudRepository<MatchPlay, Long> {

    List<MatchPlay> getByFirstTeamIdAndSecondTeamIdAndStatus(Long firstTeamId, Long secondTeamId, MatchStatus status);

    @Modifying
    @Query("update MatchPlay mp set mp.status = :status where mp.id = :id")
    void updateMatchStatusById(@Param("status") MatchStatus status, @Param("id") Long id);

    @Modifying
    @Query("update MatchPlay mp set mp.status = 0 where mp.status = 1")
    void resetAllMatches();

    @Query("select mp from MatchPlay mp where mp.type = 1 and mp.status <> 2 and mp.started < :startdate")
    List<MatchPlay> getAllFromPlayInLeague(@Param("startdate") LocalDate date);

}
