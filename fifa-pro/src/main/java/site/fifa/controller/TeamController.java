package site.fifa.controller;

import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import site.fifa.dto.NewTeamCreateRequest;
import site.fifa.dto.TeamDTO;
import site.fifa.entity.Team;
import site.fifa.service.MatchService;
import site.fifa.service.TeamService;

import java.util.List;

@RestController
@RequestMapping("team")
public class TeamController {

    @Autowired
    private TeamService teamService;

    @Autowired
    private MatchService matchService;

    @ApiResponses({@ApiResponse(code = 200, message = "Return all teams for all countries")})
    @GetMapping("get-team")
    public List<Team> getTeamForPlay() {
        return teamService.getTeams();
    }


    @ApiResponses({@ApiResponse(code = 200, message = "Return created team or null if name is busy")})
    @PostMapping(value = "create", headers = "Accept=*/*", produces = "application/json", consumes="application/json")
    public TeamDTO createTeam(@RequestBody NewTeamCreateRequest request) {

        return teamService.createNewTeam(request);
    }

}
