package site.fifa.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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

    @RequestMapping("get-team")
    public List<Team> getTeamForPlay() {
        return teamService.getTeams();
    }

    @RequestMapping("play")
    public String playQuickMatch() {
        return matchService.playAutoMatch(4L, 5L);
    }

    /**
     *
     * @param
     * @return null if team with name is present
     */
    @PostMapping(value = "create", headers = "Accept=*/*", produces = "application/json", consumes="application/json")
    public TeamDTO createTeam(@RequestBody NewTeamCreateRequest request) {

        return teamService.createNewTeam(request);
    }

}
