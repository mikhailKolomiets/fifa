package site.fifa.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import site.fifa.dto.PlayerGoalInLeague;
import site.fifa.entity.Player;
import site.fifa.entity.PlayerType;
import site.fifa.entity.match.GoalsInMatch;
import site.fifa.repository.GoalsInMatchRepository;
import site.fifa.repository.PlayerRepository;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PlayerService {

    @Autowired
    private PlayerRepository playerRepository;
    @Autowired
    private GoalsInMatchRepository goalsInMatchRepository;

    public Player generateRandomPlayer() {
        Player player = new Player();

        player.setName(generatePlayerName());
        player.setSkill(generateValueBetween(1, 100));
        player.setAge(generateValueBetween(18, 40));
        player.setType(PlayerType.GK.random());
        player.setSpeed(generateValueBetween(1, 100));

        player.setPrice(generateValueBetween(1, 1000));

        return player;
    }

    // todo on production after update update fifa.player set reserve = 1 where team_id is null ;
    @PostConstruct
    @Scheduled(cron = "5 5 12 * * *")
    public Player createPlayerForTransfer() {
        Player player = generateRandomPlayer();
        player.setReserve(true);
        if (player.getPrice() < player.getSpeed() + player.getSkill()) {
            player.setPrice(player.getSpeed() + player.getSkill());
        }
        savePlayer(player);
        return player;
    }

    public List<Player> getFreePlayers() {
        return playerRepository.getByTeamIdIsNull();
    }

    public Player generateRandomPlayerByType(PlayerType type) {
        Player player = generateRandomPlayer();
        player.setType(type);
        return player;
    }

    public Player savePlayer(Player player) {
        return playerRepository.save(player);
    }

    public Player updatePlayer(Player player) {
        Player result = playerRepository.findById(player.getId()).orElse(null);
        if (result == null) {
            return null;
        }
        result.setPrice(player.getPrice());
        result.setName(player.getName());
        return savePlayer(result);
    }

    public boolean updateStuff(Long fp, Long sp) {
        Player firstPlayer = playerRepository.findById(fp).orElse(null);
        Player secondPlayer = playerRepository.findById(sp).orElse(null);
        if (firstPlayer == null || secondPlayer == null || firstPlayer.isReserve() == secondPlayer.isReserve()) {
            return false;
        }
        firstPlayer.setReserve(!firstPlayer.isReserve());
        secondPlayer.setReserve(!secondPlayer.isReserve());
        playerRepository.save(firstPlayer);
        playerRepository.save(secondPlayer);
        return true;
    }

    public List<Player> getBestOffers() {
        return playerRepository.getAllFree();
    }

    public List<Player> getByTeamId(Long teamId) {
        return playerRepository.getByTeamId(teamId);
    }

    public List<Player> sortPlayersForGame(List<Player> players) {
        List<Player> result = new ArrayList<>();
        result.add(players.stream().filter(player -> player.getType() == PlayerType.GK).findAny().orElse(null));
        result.addAll(players.stream().filter(player -> player.getType() == PlayerType.CD).collect(Collectors.toList()));
        result.addAll(players.stream().filter(player -> player.getType() == PlayerType.MD).collect(Collectors.toList()));
        result.addAll(players.stream().filter(player -> player.getType() == PlayerType.ST).collect(Collectors.toList()));

        if (result.size() != 11 || result.get(0) == null) {
            return players;
        }

        result.add(6, result.remove(10));
        result.add(8, result.remove(10));

        return result;
    }

    public Player buyPlayer(Player player, Long teamId) {
        return player;
    }

    public List<PlayerGoalInLeague> getSortPlayersByGoalsInLeague(Long leagueId) {
        List<PlayerGoalInLeague> result = new ArrayList<>();
        List<GoalsInMatch> goals = goalsInMatchRepository.getByLeagueId(leagueId);
        int playerGoals;
        String teamName;

        while (goals.size() > 0) {
            playerGoals = goals.size();
            Player player = goals.get(0).getPlayer();
            teamName = goals.get(0).getTeam().getName();
            goals = goals.stream().filter(g -> !g.getPlayer().equals(player)).collect(Collectors.toList());
            result.add(new PlayerGoalInLeague(player, playerGoals - goals.size(), teamName));
        }
        result.sort(Comparator.comparingInt(PlayerGoalInLeague::getGoalsInLeague).reversed());
        return result;
    }

    private int generateValueBetween(int first, int second) {
        return (int) (Math.random() * (second - first) + first);
    }

    private String generatePlayerName() {
        // part of players name
        String[] words = {"bu", "zic", "ron", "ald", "vir", "ets", "ku", "kol", "bic", "boc", "cou", "omi", "por"};
        String name = "";
        for (int i = 0; i < generateValueBetween(2, 4); i++) {
            name += words[generateValueBetween(0, words.length)];
        }
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }

}
