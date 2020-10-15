package site.fifa.controller;

import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import site.fifa.dto.MatchDto;
import site.fifa.dto.MatchStepDto;
import site.fifa.dto.StatisticDto;
import site.fifa.entity.match.MatchPlay;
import site.fifa.service.MatchService;

import java.util.List;

@RestController
@RequestMapping("match")
public class MatchController {

    @Autowired
    private MatchService matchService;

    @ApiResponses({@ApiResponse(code = 200, message = "Started game between ywo teams")})
    @PostMapping("start/{firstTeamId}/{secondTeamId}")
    public MatchDto startMatch(@PathVariable Long firstTeamId,@PathVariable Long secondTeamId) {
        return matchService.startMatchWithPC(firstTeamId, secondTeamId);
    }

    @ApiResponses({@ApiResponse(code = 200, message = "calculate match step by action")})
    @PostMapping("step/{matchId}/{action}")
    public MatchStepDto play(@PathVariable Long matchId, @PathVariable String action) {
        return matchService.makeStepWithCPU(matchId, Integer.parseInt(action));
    }

    @ApiResponses({@ApiResponse(code = 200, message = "Return next three games in all leagues")})
    @GetMapping("get-for-league-games")
    public List<MatchDto> getThreeFirstMatchesInLeague() {
        return matchService.getMatchesForPlayLeaguesGame();
    }

    @ApiResponses({@ApiResponse(code = 200, message = "Play all league games before today for all countries")})
    @GetMapping("get-for-league")
    public void startLeague() {
        matchService.playAllCreatedMatchesAfterToday();
    }

    @GetMapping("last-league-matches/{amount}")
    public List<StatisticDto> lastPlayedMatches(@PathVariable Long amount) {
        return matchService.getLastLeagueMatches(amount);
    }

}
