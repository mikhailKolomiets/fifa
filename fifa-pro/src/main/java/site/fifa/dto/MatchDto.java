package site.fifa.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import site.fifa.entity.match.MatchType;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class MatchDto {

    private Long matchId;
    private PlaySide playSide;
    private LocalDate date;
    private TeamDTO firstTeam;
    private TeamDTO secondTeam;
    private MatchType matchType;

}
