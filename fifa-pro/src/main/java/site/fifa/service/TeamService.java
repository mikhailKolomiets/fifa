package site.fifa.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import site.fifa.entity.Player;
import site.fifa.entity.PlayerType;
import site.fifa.entity.Team;
import site.fifa.repository.TeamRepository;

@Service
public class TeamService {

    @Autowired
    private TeamRepository teamRepository;
    @Autowired
    private PlayerService playerService;

    public Team createNewTeam() {
        Team team = new Team();

        team.setCountry("Ukraine");
        team.setName("" + (int)(Math.random() * 1000));

        team = teamRepository.save(team);
        makeTeamPlayers(team);

        return team;
    }

    private void makeTeamPlayers(Team team) {
        Player player = playerService.generateRandomPlayerByType(PlayerType.GK);
        player.setTeamId(team.getId());
        playerService.savePlayer(player);

        for (int i = 0; i < 4; i++) {
            player = playerService.generateRandomPlayerByType(PlayerType.CD);
            player.setTeamId(team.getId());
            playerService.savePlayer(player);
        }

        for (int i = 0; i < 3; i++) {
            player = playerService.generateRandomPlayerByType(PlayerType.MD);
            player.setTeamId(team.getId());
            playerService.savePlayer(player);
            player = playerService.generateRandomPlayerByType(PlayerType.ST);
            player.setTeamId(team.getId());
            playerService.savePlayer(player);
        }
    }

}
