package site.fifa.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import site.fifa.entity.Statistic;

import java.util.List;

@Repository
public interface StatisticRepository extends CrudRepository<Statistic, Long> {

    List<Statistic> getByMatchId(Long matchId);

}
