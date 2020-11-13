package service;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import site.fifa.FIFApp;
import site.fifa.dto.NewTeamCreateRequest;
import site.fifa.dto.TeamDTO;
import site.fifa.entity.Player;
import site.fifa.repository.PlayerRepository;
import site.fifa.repository.TeamRepository;
import site.fifa.service.PlayerService;
import site.fifa.service.TeamService;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = FIFApp.class)
public class PlayerServiceTest {

    @Autowired
    private PlayerRepository playerRepository;
    @Autowired
    private PlayerService playerService;
    @Autowired
    private TeamService teamService;
    @Autowired
    private TeamRepository teamRepository;

    @Test
    public void saveAndCheckIsPresentTest() {
        Player player = playerRepository.save(playerService.generateRandomPlayer());
        assertNotNull(player.getId());

        NewTeamCreateRequest team = new NewTeamCreateRequest();
        team.setTeamName("germany team");
        team.setCountryName("Germany");

        TeamDTO teamDTO = teamService.createNewTeam(team);

        assertEquals(playerService.getByTeamId(teamDTO.getTeam().getId()).size(), 11);

    }

    @After
    public void cleanDataAfterTest() {
        playerRepository.deleteAll();
        teamRepository.deleteAll();
    }

}
