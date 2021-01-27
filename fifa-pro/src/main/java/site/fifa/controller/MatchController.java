package site.fifa.controller;

import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import site.fifa.dto.MatchDto;
import site.fifa.dto.MatchStepDto;
import site.fifa.dto.PlaySide;
import site.fifa.dto.StatisticDto;
import site.fifa.entity.match.MatchPlay;
import site.fifa.service.MatchService;
import site.fifa.service.TeamService;

import java.awt.*;
import java.util.List;

@RestController
@RequestMapping("match")
public class MatchController {

    @Autowired
    private MatchService matchService;
    @Autowired
    private TeamService teamService;

    @ApiResponses({@ApiResponse(code = 200, message = "Started game between two teams")})
    @PostMapping("start/{firstTeamId}/{secondTeamId}/{matchType}")
    public MatchDto startMatch(@PathVariable Long firstTeamId, @PathVariable Long secondTeamId, @PathVariable Long matchType) {
        if (matchType == 1 || (teamService.getTeamByIdAndUserIp(firstTeamId) == null && teamService.getTeamByIdAndUserIp(secondTeamId) == null)) {
            return matchService.startQuickGame(firstTeamId, secondTeamId);
        }
        return matchService.startGame(firstTeamId, secondTeamId);
    }

    @ApiResponses({@ApiResponse(code = 200, message = "calculate match step by action")})
    @PostMapping("step/{matchId}/{action}")
    public MatchStepDto play(@PathVariable Long matchId, @RequestParam Long teamSide, @PathVariable String action) {
        PlaySide playSide = teamSide == 2 ? PlaySide.SECOND_TEAM : PlaySide.FiRST_TEAM;
        return matchService.makeGameStep(matchId, playSide, Integer.parseInt(action));
    }

    @ApiResponses({@ApiResponse(code = 200, message = "Return next three games in all leagues")})
    @GetMapping("get-for-league-games")
    public List<MatchDto> getThreeFirstMatchesInLeagueOrToday() {
        return matchService.getMatchesForPlayLeaguesGame();
    }

    @ApiResponses({@ApiResponse(code = 200, message = "Play all league games before today for all countries")})
    @GetMapping("get-for-league")
    public void startLeague() {
        matchService.playAllCreatedMatchesAfterToday();
    }

    @ApiResponses({@ApiResponse(code = 200, message = "Return last some matches in the leagues")})
    @GetMapping("last-league-matches")
    public List<StatisticDto> lastPlayedMatches() {
        return matchService.getLastMatches();
    }

    @ApiResponses({@ApiResponse(code = 200, message = "Return all players point")})
    @GetMapping("all-point")
    public List<Point> getAllPointsForPlayers() {
        return matchService.returnAllPosition();
    }

    @ApiResponses({@ApiResponse(code = 200, message = "Return yesterday, today and tomorrow's league games")})
    @GetMapping("league-games/{leagueId}")
    public List<StatisticDto> getLeagueGames(@PathVariable Long leagueId) {
        return matchService.getLeagueStatisticOfMatches(leagueId);
    }

    @ApiResponses({@ApiResponse(code = 200, message = "Return last home league game")})
    @GetMapping("last-home/{teamId}")
    public MatchPlay getLastHomeGame(@PathVariable Long teamId) {
        return matchService.getLastLeagueGame(teamId);
    }

}
