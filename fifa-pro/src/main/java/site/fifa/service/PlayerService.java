package site.fifa.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import site.fifa.entity.Player;
import site.fifa.entity.PlayerType;
import site.fifa.repository.PlayerRepository;

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

        return player;
    }

    public Player generateRandomPlayerByType(PlayerType type) {
        Player player = generateRandomPlayer();
        player.setType(type);
        return player;
    }

    public void savePlayer(Player player) {
        playerRepository.save(player);
    }

    private int generateValueBetween (int first, int second) {
        return (int) (Math.random() * (second - first) + first);
    }

    private String generatePlayerName () {
        // part of players name
        String[] words = {"bu", "zic", "ron", "ald", "vir", "ets", "ku", "kol", "bic", "boc", "cou","omi", "por"};
        String name = "";
        for (int i = 0; i < generateValueBetween(2, 4); i ++) {
            name += words[generateValueBetween(0, words.length)];
        }
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    /**
     *  for test generated value
     * @param args
     */
    public static void main(String[] args) {
        PlayerService playerService = new PlayerService();
        Player player;
        for (int i = 0; i < 10; i++) {
            player = playerService.generateRandomPlayer();
            System.out.println(player.getName() + ", " + player.getAge() + " years, " + player.getType().name() +
                    " with speed " + player.getSpeed() + " and skill " + player.getSkill());
        }
    }

}
