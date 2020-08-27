package site.fifa.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import site.fifa.dto.TeamDTO;
import site.fifa.entity.Player;
import site.fifa.entity.PlayerType;
import site.fifa.entity.Team;
import site.fifa.repository.TeamRepository;

import java.util.ArrayList;
import java.util.List;

@Service
public class TeamService {

    @Autowired
    private TeamRepository teamRepository;
    @Autowired
    private PlayerService playerService;

    public TeamDTO createNewTeam(String name) {

        if(teamRepository.findByName(name) != null)
            return null;

        Team team = new Team();

        team.setCountry("Ukraine");
        name = name == null ? "" + (int)(Math.random() * 1000) : name;
        team.setName(name);

        team = teamRepository.save(team);
        makeTeamPlayers(team);

        return new TeamDTO(team, playerService.getByTeamId(team.getId()));
    }

    public List<Team> getTeams() {
        List<Team> result = new ArrayList<>();
        teamRepository.findAll().forEach(result::add);
        return result;
    }

    public TeamDTO getTeamById(Long teamId) {
        return new TeamDTO(teamRepository.findById(teamId).orElse(null), playerService.getByTeamId(teamId));
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
