package site.fifa.controller;

import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import site.fifa.dto.NewTeamCreateRequest;
import site.fifa.dto.TeamDTO;
import site.fifa.dto.UserDTO;
import site.fifa.entity.Team;
import site.fifa.entity.User;
import site.fifa.service.MatchService;
import site.fifa.service.TeamService;
import site.fifa.service.UserService;

import java.util.List;

@RestController
@RequestMapping("team")
public class TeamController {

    @Autowired
    private TeamService teamService;
    @Autowired
    private MatchService matchService;
    @Autowired
    private UserService userService;

    @ApiResponses({@ApiResponse(code = 200, message = "Return all teams for all countries")})
    @GetMapping("get-team")
    public List<Team> getTeamForPlay() {
        return teamService.getTeams();
    }

    @ApiResponses({@ApiResponse(code = 200, message = "Assign team to user with ip by session")})
    @PostMapping("assign")
    public User assignTeam(@RequestParam Long teamId) {
        User user = teamService.assignTeamForUser(teamId);
        if (user == null) {
            return null;
        }
        UserDTO userInSession = userService.findUserInSession(user.getName());
        if (userInSession != null) {
            userInSession.getUser().setTeamId(teamId);
        }
        return user;
    }


    @ApiResponses({@ApiResponse(code = 200, message = "Return created team or null if name is busy")})
    @PostMapping(value = "create", headers = "Accept=*/*", produces = "application/json", consumes="application/json")
    public TeamDTO createTeam(@RequestBody NewTeamCreateRequest request) {

        return teamService.createNewTeam(request);
    }

    @ApiResponses({@ApiResponse(code = 200, message = "Return team by his id")})
    @GetMapping("get/{teamId}")
    public TeamDTO getById(@PathVariable Long teamId) {
        return teamService.getTeamByIdAndUserIp(teamId);
    }

    @ApiResponses({@ApiResponse(code = 200, message = "Return teams of country")})
    @GetMapping("get-by-country/{countryId}")
    public List<Team> getByCountry(@PathVariable Long countryId) {
        return teamService.getByCountryId(countryId);
    }

    @ApiResponses({@ApiResponse(code = 200, message = "Return all free teams of country")})
    @GetMapping("get-free-by-country/{countryId}")
    public List<Team> getFreeByCountry(@PathVariable Long countryId) {
        return teamService.getFreeByCountry(countryId);
    }

}
