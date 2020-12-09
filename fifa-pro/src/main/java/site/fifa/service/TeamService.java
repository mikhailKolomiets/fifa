package site.fifa.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.fifa.dto.NewTeamCreateRequest;
import site.fifa.dto.TeamDTO;
import site.fifa.entity.*;
import site.fifa.repository.CountryRepository;
import site.fifa.repository.LeagueTableItemRepository;
import site.fifa.repository.PlayerRepository;
import site.fifa.repository.TeamRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeamService {


    private final TeamRepository teamRepository;
    private final CountryRepository countryRepository;
    private final PlayerService playerService;
    private final LeagueTableItemRepository leagueTableItemRepository;
    private final PlayerRepository playerRepository;

    public TeamDTO createNewTeam(NewTeamCreateRequest request) {

        if (teamRepository.findByName(request.getTeamName()) != null)
            return null;

        Country country = countryRepository.findByCountryName(request.getCountryName());
        Team team = new Team();

        team.setCountry(country);
        String name = request.getTeamName() == null ? "" + (int) (Math.random() * 1000) : request.getTeamName();
        team.setName(name);

        team = teamRepository.save(team);

        return new TeamDTO().buildTeam(team).buildPlayers(makeTeamPlayers(team));
    }

    public List<Team> getTeams() {
        List<Team> result = new ArrayList<>();
        teamRepository.findAll().forEach(result::add);
        return result;
    }

    public TeamDTO getTeamById(Long teamId) {
        Team team = teamRepository.findById(teamId).orElse(null);
        if (team == null) {
            return null;
        }
        LeagueTableItem leagueTableItem = leagueTableItemRepository.getByLeagueIdAndTeamId(team.getLeagueId(), teamId).stream().findAny().orElse(null);

        List<Player> teamStuff = playerService.getByTeamId(teamId);

        return new TeamDTO(leagueTableItem == null ? 0 : leagueTableItem.getPosition(),
                teamRepository.findById(teamId).orElse(null), sortPlayersForGame(teamStuff.stream().filter(p -> !p.isReserve()).collect(Collectors.toList())),
                teamStuff.stream().filter(Player::isReserve).collect(Collectors.toList()));
    }

    public Team updateOrSave(Team team) {
        return teamRepository.save(team);
    }

    public List<Team> getByCountryId(Long countryId) {
        return teamRepository.getByCountryId(countryId);
    }

    @Transactional
    public void buyPlayer(Long playerId, Long teamId) {
        Team team = teamRepository.findById(teamId).orElse(null);
        Player player = playerRepository.findById(playerId).orElse(null);
        if (player != null && team != null) {
            player.setReserve(true);
            int price = player.getPrice();
            player.setPrice(0);
            player.setTeamId(teamId);
            team.setMoney(team.getMoney() - price);

            updateOrSave(team);
            playerRepository.save(player);
        }
    }

    public List<Player> sortPlayersForGame(List<Player> players) {
        return playerService.sortPlayersForGame(players);
    }

    private List<Player> makeTeamPlayers(Team team) {
        List<Player> result = new ArrayList<>();
        Player player = playerService.generateRandomPlayerByType(PlayerType.GK);
        player.setTeamId(team.getId());
        result.add(playerService.savePlayer(player));

        for (int i = 0; i < 4; i++) {
            player = playerService.generateRandomPlayerByType(PlayerType.CD);
            player.setTeamId(team.getId());
            result.add(playerService.savePlayer(player));
        }

        for (int i = 0; i < 3; i++) {
            player = playerService.generateRandomPlayerByType(PlayerType.MD);
            player.setTeamId(team.getId());
            result.add(playerService.savePlayer(player));
            player = playerService.generateRandomPlayerByType(PlayerType.ST);
            player.setTeamId(team.getId());
            result.add(playerService.savePlayer(player));
        }
        return result;
    }

}
