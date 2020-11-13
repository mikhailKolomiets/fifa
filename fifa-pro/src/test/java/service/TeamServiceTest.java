package service;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import site.fifa.FIFApp;
import site.fifa.dto.NewTeamCreateRequest;
import site.fifa.dto.TeamDTO;
import site.fifa.repository.TeamRepository;
import site.fifa.service.TeamService;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = FIFApp.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TeamServiceTest {

    @Autowired
    private TeamService teamService;
    @Autowired
    private TeamRepository teamRepository;

    @Test
    public void test1BeforeCreation() {
        assertEquals(teamService.getTeams().size(), 0);
    }

    @Test
    public void test2CreateNewTeamTest() {
        NewTeamCreateRequest newTeamCreateRequest = new NewTeamCreateRequest();
        newTeamCreateRequest.setCountryName("Ukraine");
        newTeamCreateRequest.setTeamName("Test team");
        TeamDTO teamDTO = teamService.createNewTeam(newTeamCreateRequest);

        // try create same team
        assertNull(teamService.createNewTeam(newTeamCreateRequest));

        assertEquals(teamDTO, teamService.getTeamById(teamDTO.getTeam().getId()));
    }

    @Test
    public void test3AfterCreation() {
        assertEquals(teamService.getTeams().size(), 1);
    }

}
