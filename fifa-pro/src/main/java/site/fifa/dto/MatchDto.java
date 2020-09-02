package site.fifa.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MatchDto {

    private Long matchId;
    private PlaySide playSide;

    private TeamDTO firstTeam;
    private TeamDTO secondTeam;

}
