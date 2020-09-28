package site.fifa.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import site.fifa.entity.League;

import java.util.List;

public interface LeagueRepository extends CrudRepository<League, Long> {

    @Query(nativeQuery = true, value = "select distinct l.* from league l inner join team t on l.id =  t.league_id where t.country_id = :country")
    List<League> getByCountryIfPresent(@Param("country") Long countryId);

}
