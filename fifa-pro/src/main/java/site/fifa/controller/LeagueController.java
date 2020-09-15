package site.fifa.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import site.fifa.dto.LeagueTableItemDto;
import site.fifa.entity.match.MatchPlay;
import site.fifa.service.LeagueService;

import java.util.List;

@RestController
@RequestMapping("league")
public class LeagueController {

    @Autowired
    private LeagueService leagueService;

    // todo remove after test
    @RequestMapping("generate-league/{countryId}")
    public List<MatchPlay> generateLeagueByCountryId(@PathVariable Long countryId) {

        return leagueService.scheduledLeagueGames(countryId);
    }

    // todo remove after test
    @RequestMapping("start")
    public void startLeague() {
        leagueService.startLeagues();
    }

    @RequestMapping("get-table/{leagueId}")
    public List<LeagueTableItemDto> getLeagueTableByLeagueId(@PathVariable Long leagueId) {
        return leagueService.getLeagueTableById(leagueId);
    }

}
