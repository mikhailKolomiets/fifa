package site.fifa.controller;

import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import site.fifa.dto.LeagueTableItemDto;
import site.fifa.entity.League;
import site.fifa.entity.match.MatchPlay;
import site.fifa.service.LeagueService;

import java.util.List;

@RestController
@RequestMapping("league")
public class LeagueController {

    @Autowired
    private LeagueService leagueService;

    // todo remove after test
    @ApiResponses({@ApiResponse(code = 200, message = "Create league with 5 - 25 teams in country(test endpoint)")})
    @GetMapping("generate-league/{countryId}")
    public List<MatchPlay> generateLeagueByCountryId(@PathVariable Long countryId) {

        return leagueService.scheduledLeagueGames(countryId);
    }

    @ApiResponses({@ApiResponse(code = 200, message = "Return all leagues in the country")})
    @GetMapping("leagues-by-country/{countryId}")
    public List<League> getLeagueByCountryId(@PathVariable Long countryId) {
        return leagueService.getLeaguesByCountryId(countryId);
    }

    // todo remove after test
    @ApiResponses({@ApiResponse(code = 200, message = "Try to start all leagues")})
    @GetMapping("start")
    public void startLeague() {
        leagueService.startLeagues();
    }

    @ApiResponses({@ApiResponse(code = 200, message = "Return league table")})
    @GetMapping("get-table/{leagueId}")
    public List<LeagueTableItemDto> getLeagueTableByLeagueId(@PathVariable Long leagueId) {
        return leagueService.getLeagueTableById(leagueId);
    }

    @ApiResponses({@ApiResponse(code = 200, message = "Return first league table from country")})
    @GetMapping("table-first/{countryId}")
    public List<LeagueTableItemDto> getLeagueTableByCountryId(@PathVariable Long countryId) {
        return leagueService.getLeagueTableByCountryId(countryId);
    }

}
