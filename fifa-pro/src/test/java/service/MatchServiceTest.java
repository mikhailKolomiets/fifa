package service;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import site.fifa.FIFApp;
import site.fifa.dto.MatchDto;
import site.fifa.dto.MatchStepDto;
import site.fifa.dto.NewTeamCreateRequest;
import site.fifa.dto.TeamDTO;
import site.fifa.entity.Team;
import site.fifa.entity.match.MatchPlay;
import site.fifa.entity.match.MatchStatus;
import site.fifa.repository.GoalsInMatchRepository;
import site.fifa.repository.LeagueRepository;
import site.fifa.repository.MatchRepository;
import site.fifa.repository.TeamRepository;
import site.fifa.service.LeagueService;
import site.fifa.service.MatchService;
import site.fifa.service.TeamService;

import java.time.LocalDate;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = FIFApp.class)
public class MatchServiceTest {

    @Autowired
    private MatchService matchService;
    @Autowired
    private TeamService teamService;
    @Autowired
    private TeamRepository teamRepository;
    @Autowired
    private GoalsInMatchRepository goalsInMatchRepository;
    @Autowired
    private MatchRepository matchRepository;
    @Autowired
    private LeagueService leagueService;
    @Autowired
    private LeagueRepository leagueRepository;

    private NewTeamCreateRequest team = new NewTeamCreateRequest();

    @After
    public void cleanData() {
        goalsInMatchRepository.deleteAll();
        teamRepository.deleteAll();
    }

    @Test
    public void startAndPlaySampleMatch() {
        team.setCountryName("Germany");
        team.setTeamName("Borusia");
        TeamDTO firstTeam = teamService.createNewTeam(team);
        team.setTeamName("Bavaria");
        TeamDTO secondTeam = teamService.createNewTeam(team);

        // play match with PC
        MatchDto testedMatch = matchService.startMatchWithPC(firstTeam.getTeam().getId(), secondTeam.getTeam().getId(), true);
        assertNotNull(testedMatch);
        MatchStepDto matchStepDto = matchService.makeStepWithCPU(testedMatch.getMatchId(), 1);
        while (!matchStepDto.getLastStepLog().equals(matchStepDto.showGoals())) {
            matchStepDto = matchService.makeStepWithCPU(testedMatch.getMatchId(), 1);
        }
        assertEquals(MatchStatus.FINISHED, matchRepository.findById(testedMatch.getMatchId()).get().getStatus());

        // play match with 2 players
        testedMatch = matchService.startMatchWithPC(firstTeam.getTeam().getId(), secondTeam.getTeam().getId(), false);
        assertNotNull(testedMatch);
        matchStepDto = matchService.makeStepWithCPU(testedMatch.getMatchId(), 1);
        matchService.makeStepWithCPU(testedMatch.getMatchId(), 10);
        while (!matchStepDto.getLastStepLog().equals(matchStepDto.showGoals())) {
            matchService.makeStepWithCPU(testedMatch.getMatchId(), 10);
            matchStepDto = matchService.makeStepWithCPU(testedMatch.getMatchId(), 1);
        }
        assertEquals(MatchStatus.FINISHED, matchRepository.findById(testedMatch.getMatchId()).get().getStatus());

        testedMatch.hashCode();

    }

    @Test
    public void playLeagueMatchesTest() {
        team.setCountryName("Germany");

        for(int i = 0; i < 6; i++) {
            team.setTeamName("League team " + i);
            teamService.createNewTeam(team);
        }

        leagueService.startLeagues();
        List<MatchPlay> games = matchRepository.getAllFromPlayInLeague(LocalDate.now().plusMonths(1));
        for (MatchPlay game : games) {
            assertEquals(MatchStatus.CREATED, game.getStatus());
            game.setStarted(game.getStarted().minusMonths(1));
            matchRepository.save(game);
        }
        matchService.playAllCreatedMatchesAfterToday();

        games = matchRepository.getAllFromPlayInLeague(LocalDate.now().plusMonths(1));
        for (MatchPlay game : games) {
            assertEquals(MatchStatus.FINISHED, game.getStatus());
        }
    }

}
