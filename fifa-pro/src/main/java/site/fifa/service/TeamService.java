package site.fifa.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.fifa.constants.GameConstants;
import site.fifa.dto.NewTeamCreateRequest;
import site.fifa.dto.TeamDTO;
import site.fifa.entity.*;
import site.fifa.entity.message.Message;
import site.fifa.entity.message.MessageTypeEnum;
import site.fifa.repository.*;

import javax.servlet.ServletRequest;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
    private final ServletRequest servletRequest;
    private final UserRepository userRepository;
    private final StadiumService stadiumService;
    private final MessageRepository messageRepository;
    private final GoalsInMatchRepository goalsInMatchRepository;

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

    public User assignTeamForUser(Long teamId) {
        User user = userRepository.findFirstByUserLastIp(servletRequest.getRemoteAddr());
        Team team = teamRepository.findById(teamId).orElse(null);
        if (user != null && team != null) {
            user.setTeamId(teamId);
            userRepository.save(user);
            team.setCoachStarted(LocalDateTime.now());
            teamRepository.save(team);
        }
        return user;
    }

    public TeamDTO getTeamByIdAndUserIp(Long teamId) {
        User user = userRepository.findFirstByUserLastIp(servletRequest.getRemoteAddr());
        if (user == null || user.getTeamId() == null) {
            return null;
        }
        TeamDTO result = getTeamById(teamId);
        return result.getTeam().getId().equals(user.getTeamId()) ? result : null;
    }

    public TeamDTO getTeamById(Long teamId) {
        Team team = teamRepository.findById(teamId).orElse(null);
        if (team == null) {
            return null;
        }
        if (team.getStadium() == null) {
            team.setStadium(stadiumService.createDefaultStadium());
            teamRepository.save(team);
        }
        LeagueTableItem leagueTableItem = leagueTableItemRepository.getByLeagueIdAndTeamId(team.getLeagueId(), teamId).stream().findAny().orElse(null);

        List<Player> teamStuff = playerService.getByTeamId(teamId);

        return new TeamDTO(leagueTableItem == null ? 0 : leagueTableItem.getPosition(),
                teamRepository.findById(teamId).orElse(null), sortPlayersForGame(teamStuff.stream().filter(p -> !p.isReserve()).collect(Collectors.toList())),
                teamStuff.stream().filter(Player::isReserve).collect(Collectors.toList()));
    }

    public Team resetCoach(Long teamId) {
        Team team = teamRepository.findById(teamId).orElse(null);
        if (team != null && (team.getCoachStarted() == null || team.getCoachStarted().isBefore(LocalDateTime.now().minusDays(GameConstants.COACH_DAYS_FOR_TEAM_CHANGE)))) {
            User user = userRepository.findByTeamId(teamId);
            if (user != null) {
                user.setTeamId(null);
                userRepository.save(user);
                messageRepository.save(Message.builder().
                        type(MessageTypeEnum.TEAM_ACTION).createTime(LocalDateTime.now()).
                        body(LocalDate.now() + " " + user.getName() + " покинул пост тренера").toId(teamId).
                        build());
                team.setCoachStarted(null);
            }
        }

        return team;
    }

    public Team updateOrSave(Team team) {
        return teamRepository.save(team);
    }

    public List<Team> getByCountryId(Long countryId) {
        return teamRepository.getByCountryId(countryId);
    }

    public List<Team> getFreeByCountry(Long countryId) {
        return teamRepository.getFreeByCountryId(countryId);
    }

    @Transactional
    public void buyPlayer(Long playerId, Long teamId) {
        User user = userRepository.findFirstByUserLastIp(servletRequest.getRemoteAddr());
        if (user == null || !user.getTeamId().equals(teamId)) {
            return;
        }
        Team team = teamRepository.findById(teamId).orElse(null);
        Player player = playerRepository.findById(playerId).orElse(null);
        if (player != null && team != null) {
            player.setReserve(true); // todo delete after all refactoring
            int price = player.getPrice();
            team.setMoney(team.getMoney() - price);

            if (player.getTeamId() != null) {
                Team teamFrom = teamRepository.findById(player.getTeamId()).orElse(null);
                if (teamFrom != null) {
                    price -= price/10 + 1;
                    price = Math.max(price, 0);
                    teamFrom.setMoney(teamFrom.getMoney() + price);
                    updateOrSave(teamFrom);
                }
            }
            Message message = new Message();
            message.setToId(teamId);
            message.setBody(LocalDate.now() + " " + user.getName() + " купил " + player.getName() + " за " + price);
            message.setType(MessageTypeEnum.TEAM_ACTION);
            message.setCreateTime(LocalDateTime.now());

            messageRepository.save(message);

            player.setPrice(0);
            player.setTeamId(teamId);

            updateOrSave(team);
            playerRepository.save(player);
        }
    }

    @Transactional
    public void sellPlayerByDelete(Long playerId) {
        Player player = playerRepository.findById(playerId).orElse(null);
        if (player == null || !player.isReserve()) {
            return;
        }
        if (player.getTeamId() != null) {
            Team team = teamRepository.findById(player.getTeamId()).orElse(null);
            if (team != null) {
                int playerSellingPrice = (player.getSkill() + player.getSpeed()) / GameConstants.PLAYER_SELL_DELETE_CONSTANT;
                team.setMoney(team.getMoney() + playerSellingPrice);
                teamRepository.save(team);
                Message message = Message.builder()
                        .body(LocalDate.now() + " " + player.getName() + " продан и удален из проекта за " + playerSellingPrice)
                        .toId(team.getId())
                        .type(MessageTypeEnum.TEAM_ACTION)
                        .createTime(LocalDateTime.now()).build();
                messageRepository.save(message);
            }
            player.setTeamId(null);
        }
        player.setReserve(false);
        playerRepository.save(player);
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
