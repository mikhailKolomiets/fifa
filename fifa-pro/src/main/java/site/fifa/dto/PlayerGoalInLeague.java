package site.fifa.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import site.fifa.entity.Player;

@Data
@AllArgsConstructor
public class PlayerGoalInLeague {
    private Player player;
    private int goalsInLeague;
    private String teamName;
}
