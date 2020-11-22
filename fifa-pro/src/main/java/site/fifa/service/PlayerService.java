package site.fifa.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import site.fifa.entity.Player;
import site.fifa.entity.PlayerType;
import site.fifa.repository.PlayerRepository;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class PlayerService {

    @Autowired
    private PlayerRepository playerRepository;

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

    @PostConstruct
    @Scheduled(cron = "5 5 12 * * *")
    public Player createPlayerForTransfer() {
        Player player = generateRandomPlayer();
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
        List<Player> data = playerRepository.getAllFree();
        data.sort(Comparator.comparingInt(Player::getSkill).reversed());
        int amountPlayers = data.size();
        amountPlayers = Math.min(amountPlayers, 5);
        List<Player> result = new ArrayList<>(data.subList(0, amountPlayers));
        data.sort(Comparator.comparingInt(Player::getSpeed).reversed());
        result.addAll(data.subList(0, amountPlayers));
        data.sort(Comparator.comparingInt(Player::getPrice));
        result.addAll(data.subList(0, amountPlayers));
        return result;
    }

    public List<Player> getByTeamId(Long teamId) {
        return playerRepository.getByTeamId(teamId);
    }

    public Player buyPlayer(Player player, Long teamId) {
        return player;
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
