package site.fifa.controller;

import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import site.fifa.entity.Player;
import site.fifa.service.PlayerService;
import site.fifa.service.TeamService;

import java.util.List;

@RestController
@RequestMapping("player")
public class PlayerController {

    @Autowired
    private PlayerService playerService;
    @Autowired
    private TeamService teamService;

    @ApiResponses({@ApiResponse(code = 200, message = "Return all players in the team")})
    @GetMapping("get-team/{teamId}")
    public List<Player> getByTeamId(@PathVariable Long teamId) {
        return playerService.getByTeamId(teamId);
    }

    @ApiResponses({@ApiResponse(code = 200, message = "Update player")})
    @PostMapping("update")
    public Player editPlayer(@RequestParam String playerName, @RequestParam Long playerId, @RequestParam int price) {

        System.out.println("try to change player with id=" + playerId + " to name " + playerName + " and price " + price);

        Player player = new Player();
        player.setId(playerId);
        player.setName(playerName);
        player.setPrice(price);
        return playerService.updatePlayer(player);
    }

    @ApiResponses({@ApiResponse(code = 200, message = "Update team stuff")})
    @PutMapping("update-stuff/{stuffId}/{reserveId}")
    public void updateStuff(@PathVariable Long stuffId, @PathVariable Long reserveId) {
        System.out.println("try change stuff " + stuffId + " to " + reserveId);
        if (playerService.updateStuff(stuffId, reserveId)) {
            System.out.println("Stuff changed successful");
        }
    }

    @GetMapping("offers")
    public List<Player> bestOffers() {
        return playerService.getBestOffers();
    }

    @ApiResponses({@ApiResponse(code = 200, message = "Buy player")})
    @PostMapping("buy")
    public void buyPlayer(@RequestParam Long teamId, @RequestParam Long playerId) {

        System.out.println("try to buy player with id=" + playerId + " to team with id " + teamId);

        teamService.buyPlayer(playerId, teamId);
    }

}
