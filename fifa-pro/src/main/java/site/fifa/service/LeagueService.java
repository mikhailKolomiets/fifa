package site.fifa.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import site.fifa.dto.LeagueTableItemDto;
import site.fifa.entity.*;
import site.fifa.entity.match.MatchPlay;
import site.fifa.entity.match.MatchStatus;
import site.fifa.entity.match.MatchType;
import site.fifa.repository.*;

import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class LeagueService {

    @Autowired
    private CountryRepository countryRepository;
    @Autowired
    private TeamRepository teamRepository;
    @Autowired
    private LeagueRepository leagueRepository;
    @Autowired
    private MatchRepository matchRepository;
    @Autowired
    private StatisticRepository statisticRepository;
    @Autowired
    private LeagueTableItemRepository leagueTableItemRepository;

    /**
     * Every day check start leagues in the all countries.
     * The league contain 8 team
     */
    @PostConstruct
    @Scheduled(cron = "5 13 21 * * *")
    public void startLeagues() {
        if (matchRepository.findFirstByTypeAndStatusOrderByIdDesc(MatchType.LEAGUE, MatchStatus.CREATED) == null
                && matchRepository.findFirstByTypeAndStatusOrderByIdDesc(MatchType.LEAGUE, MatchStatus.STARTED) == null
                && matchRepository.findFirstByTypeAndStatusOrderByIdDesc(MatchType.EURO, MatchStatus.CREATED) == null
                && matchRepository.findFirstByTypeAndStatusOrderByIdDesc(MatchType.EURO, MatchStatus.STARTED) == null
                && (!checkEuroLeagueConditionForCreate() || !createEuroLeague())) {

            countryRepository.findAll().forEach(this::startLeagueByCountry);

        }
    }

    private void startLeagueByCountry(Country country) {
        //check if the league is finished
        resetLeaguesForTeamsIfLeaguesIsEnd(country);

        List<Team> countryTeams = teamRepository.getByCountryAndLeagueIdIsNull(new Country(country.getCountryId()));

        countryTeams = countryTeams.stream().filter(team -> team.getLeagueId() == null).collect(Collectors.toList());
        if (countryTeams.size() > 4 && countryTeams.size() < 26) {
            League league = leagueRepository.save(new League(null, country.getCountryName() + " league", country));

            System.out.println("Create " + country.getCountryName() + " league with id " + league.getId());

            countryTeams.forEach(team -> {
                teamRepository.changeLeagueIdById(league.getId(), team.getId());
                leagueTableItemRepository.save(new LeagueTableItem(league.getId(), team.getId()));
            });
            matchRepository.saveAll(scheduledLeagueGames(league.getId()));
        }
    }

    public List<MatchPlay> scheduledLeagueGames(Long leagueId) {
        List<Team> teams = teamRepository.getByLeagueId(leagueId);

        List<MatchPlay> matchPlays = new ArrayList<>();
        for (int i = 0; i < teams.size(); i++) {
            for (int j = i + 1; j < teams.size(); j++) {
                matchPlays.add(new MatchPlay(MatchStatus.CREATED, MatchType.LEAGUE, null, teams.get(i).getId(), teams.get(j).getId()));
            }
        }

        return schedulingGame(matchPlays, teams.size());
    }

    private boolean createEuroLeague() {
        List<Team> groupA = new ArrayList<>();
        List<Team> groupB = new ArrayList<>();
        for (Country c : countryRepository.findAll()) {
            List<League> leagues = leagueRepository.getByCountryIfPresent(c.getCountryId());
            if (leagues == null || leagues.size() == 0) {
                System.out.println("cant create euro league couse " + c.getCountryName() + " d't play leagues");
                return false;
            }
            List<LeagueTableItem> leagueTableItems = leagueTableItemRepository.getByLeagueId(leagues.get(0).getId());
            leagueTableItems.sort(Comparator.comparingInt(LeagueTableItem::getPosition).reversed());

            groupA.add(teamRepository.findById(leagueTableItems.get(c.getCountryId() > 2 ? 0 : 1).getTeamId()).orElse(null));
            groupB.add(teamRepository.findById(leagueTableItems.get(c.getCountryId() > 2 ? 1 : 0).getTeamId()).orElse(null));
        }
        if (groupA.size() == 4 && groupB.size() == 4) {
            League euroA = leagueRepository.save(new League(null, "EURO", null));
            League euroB = leagueRepository.save(new League(null, "EURO", null));
            groupA.forEach(team -> leagueTableItemRepository.save(new LeagueTableItem(euroA.getId(), team.getId())));
            groupB.forEach(team -> leagueTableItemRepository.save(new LeagueTableItem(euroB.getId(), team.getId())));

            List<MatchPlay> euroGames = scheduledLeagueGames(euroA.getId());
            euroGames.forEach(g -> g.setType(MatchType.EURO));
            matchRepository.saveAll(euroGames);
            euroGames = scheduledLeagueGames(euroB.getId());
            euroGames.forEach(g -> g.setType(MatchType.EURO));
            matchRepository.saveAll(euroGames);

            System.out.println("euro leagues created");
        }
        return false;
    }

    private boolean checkEuroLeagueConditionForCreate() {
        MatchPlay lastEuroGame = matchRepository.findFirstByTypeAndStatusOrderByIdDesc(MatchType.EURO, MatchStatus.FINISHED);
        MatchPlay lastLeagueGame = matchRepository.findFirstByTypeAndStatusOrderByIdDesc(MatchType.LEAGUE, MatchStatus.FINISHED);
        return lastEuroGame == null || lastLeagueGame == null || !lastEuroGame.getStarted().isAfter(lastLeagueGame.getStarted());
    }

    public List<League> getLeaguesByCountryId(Long countryId) {
        return leagueRepository.getByCountryIfPresent(countryId);
    }

    /**
     * Scheduling game for country league
     *
     * @param matchPlays - all possible games between all teams in league
     * @param teams      - amount teams in league
     * @return
     */
    private List<MatchPlay> schedulingGame(List<MatchPlay> matchPlays, int teams) {
        List<MatchPlay> result = new ArrayList<>();
        List<Long> bufferList = new ArrayList<>();
        MatchPlay nextMatch = null;
        List<Long> correction = null;
        LocalDate date = LocalDate.now();

        long playDaysHalfLeague = 0;

        if (teams % 2 != 0) {
            correction = new ArrayList<>();
            for (MatchPlay matchPlay : matchPlays) {
                if (!correction.contains(matchPlay.getFirstTeamId())) {
                    correction.add(matchPlay.getFirstTeamId());
                } else if (!correction.contains(matchPlay.getSecondTeamId())) {
                    correction.add(matchPlay.getSecondTeamId());
                }
            }
        }

        while (matchPlays.size() > 0) {
            date = date.plusDays(1);
            playDaysHalfLeague++;

            for (int j = 0; j < teams / 2; j++) {
                for (MatchPlay match : matchPlays) {
                    if (correction != null && bufferList.size() != 0 && j == 0) {
                        if (bufferList.contains(match.getFirstTeamId())) {
                            nextMatch = match;
                            break;
                        }
                    } else if (!bufferList.contains(match.getFirstTeamId()) && !bufferList.contains(match.getSecondTeamId())) {
                        nextMatch = match;
                        break;
                    }
                }
                if (nextMatch != null) {
                    bufferList.add(nextMatch.getFirstTeamId());
                    bufferList.add(nextMatch.getSecondTeamId());
                    result.add(new MatchPlay(MatchStatus.CREATED, MatchType.LEAGUE, date, nextMatch.getFirstTeamId(), nextMatch.getSecondTeamId()));
                    matchPlays.remove(nextMatch);
                    nextMatch = null;
                }
            }

            if (correction != null) {
                for (Long l : correction)
                    if (!bufferList.contains(l)) {
                        bufferList.clear();
                        bufferList.add(l);
                        correction.remove(l);
                        break;
                    }
                if (bufferList.size() > 1)
                    bufferList.clear();
            } else
                bufferList.clear();

        }

        for (MatchPlay matchPlay : result) {
            // change stadium games
            matchPlays.add(new MatchPlay(MatchStatus.CREATED, MatchType.LEAGUE, matchPlay.getStarted().plusDays(playDaysHalfLeague),
                    matchPlay.getSecondTeamId(), matchPlay.getFirstTeamId()));
        }

        result.addAll(matchPlays);

        return result;
    }

    /**
     * Get List of league items that represent the teams of league
     *
     * @param leagueId
     * @return
     */
    public List<LeagueTableItemDto> getLeagueTableById(Long leagueId) {
        League league = leagueRepository.findById(leagueId).orElse(null);
        if (league == null) {
            return null;
        }
        List<LeagueTableItem> leagueTableItems = leagueTableItemRepository.getByLeagueId(leagueId);
        List<LeagueTableItemDto> result = new ArrayList<>();

        for (LeagueTableItem l : leagueTableItems) {
            result.add(new LeagueTableItemDto(league.getName(), l, teamRepository.findById(l.getTeamId()).orElse(null)));
        }

        result.sort(Comparator.comparingInt(LeagueTableItemDto::getPosition));

        return result;
    }

    /**
     * @param countryId
     * @return first league table in the country
     */
    public List<LeagueTableItemDto> getLeagueTableByCountryId(Long countryId) {
        List<League> countryLeagues = getLeaguesByCountryId(countryId);
        if (countryLeagues.size() == 0) {
            return null;
        }
        return getLeagueTableById(countryLeagues.get(0).getId());
    }

    private void resetLeaguesForTeamsIfLeaguesIsEnd(Country country) {

        List<League> countryLeagues = leagueRepository.getByCountryIfPresent(country.getCountryId());

        for (League l : countryLeagues) {
            if (matchRepository.getNextMatchesInLeague(l.getId()).size() == 0) {
                teamRepository.resetAllTeamsInLeague(l.getId());
            }
        }
    }

}
