package site.fifa.controller;

import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import site.fifa.entity.Player;
import site.fifa.service.PlayerService;

import java.util.List;

@RestController
@RequestMapping("player")
public class PlayerController {

    @Autowired
    private PlayerService playerService;

    @ApiResponses({@ApiResponse(code = 200, message = "Return all players in the team")})
    @GetMapping("get-team/{teamId}")
    public List<Player> getByTeamId(@PathVariable Long teamId) {
        return playerService.getByTeamId(teamId);
    }

}
