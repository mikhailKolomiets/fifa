package site.fifa.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import site.fifa.entity.Statistic;
import site.fifa.repository.StatisticRepository;

@Service
public class StatisticService {

    @Autowired
    private StatisticRepository statisticRepository;

    public void saveStatistic(Statistic statistic) {
        statisticRepository.save(statistic);
    }

    public boolean isExistByMatchId(Long matchId) {
        return statisticRepository.getByMatchId(matchId).size() > 0;
    }

}
