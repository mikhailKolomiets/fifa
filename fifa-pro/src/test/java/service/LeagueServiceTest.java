package service;

import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import site.fifa.FIFApp;
import site.fifa.dto.NewTeamCreateRequest;
import site.fifa.service.LeagueService;
import site.fifa.service.TeamService;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = FIFApp.class)
public class LeagueServiceTest {

    @Autowired
    private LeagueService leagueService;
    @Autowired
    private TeamService teamService;

    @Test
    public void createLeagueTest() {
        assertEquals(0L, leagueService.getLeaguesByCountryId(2L).size());

        NewTeamCreateRequest team = new NewTeamCreateRequest();
        team.setCountryName("Germany");

        for(int i = 0; i < 6; i++) {
            team.setTeamName("League team " + i);
            teamService.createNewTeam(team);
        }

        leagueService.startLeagues();
        assertEquals(1, leagueService.getLeaguesByCountryId(2L).size());
    }

}
