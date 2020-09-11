package site.fifa.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import site.fifa.entity.League;
import site.fifa.entity.Team;
import site.fifa.service.LeagueService;
import site.fifa.service.TeamService;

import java.util.List;

@RestController
@RequestMapping("league")
public class LeagueController {

    @Autowired
    private LeagueService leagueService;

    // todo remove from test
    @RequestMapping("generate-league/{countryId}")
    public List<Team> generateLeagueByCountryId(@PathVariable Long countryId) {

        return null;

    }

}
