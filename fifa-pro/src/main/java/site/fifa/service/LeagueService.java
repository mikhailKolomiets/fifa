package site.fifa.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import site.fifa.entity.Country;
import site.fifa.entity.League;
import site.fifa.entity.Team;
import site.fifa.repository.CountryRepository;
import site.fifa.repository.LeagueRepository;
import site.fifa.repository.TeamRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class LeagueService {

    @Autowired
    private CountryRepository countryRepository;
    @Autowired
    private TeamRepository teamRepository;
    @Autowired
    private LeagueRepository leagueRepository;

    /**
     * Every friday check start leagues in the all countries.
     * The league contain from 5 to 25 team
     */
    @Scheduled(cron = "5 40 20 * * FRI")
    public void startLeagues() {
        //System.out.println("timer");
        countryRepository.findAll().forEach(this::startLeagueByCountry);

    }

    private void startLeagueByCountry(Country country) {
        System.out.println("attempt to create league for " + country.getCountryName());
        List<Team> countryTeams = teamRepository.getByCountryAndLeagueIdIsNull(new Country(country.getCountryId()));
        countryTeams = countryTeams.stream().filter(team -> team.getLeagueId() == null).collect(Collectors.toList());
        if (countryTeams.size() > 4 && countryTeams.size() < 26) {
            League league = leagueRepository.save(new League(null, country.getCountryName() + " league", country));

            //todo refactor by repository layer
            countryTeams.forEach(team -> teamRepository.changeLeagueIdById(league.getId(), team.getId()));
        }
    }

}
