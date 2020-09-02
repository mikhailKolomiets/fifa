package site.fifa.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import site.fifa.dto.MatchDto;
import site.fifa.dto.MatchStepDto;
import site.fifa.service.MatchService;

@RestController
@RequestMapping("match")
public class MatchController {

    @Autowired
    private MatchService matchService;

    @PostMapping("start/{firstTeamId}/{secondTeamId}")
    public MatchDto startMatch(@PathVariable Long firstTeamId,@PathVariable Long secondTeamId) {
        return matchService.startMatchWithPC(firstTeamId, secondTeamId);
    }

    @PostMapping("step/{matchId}/{action}")
    public MatchStepDto play(@PathVariable Long matchId, @PathVariable String action) {

        return matchService.makeStepWithCPU(matchId, Integer.parseInt(action));

    }

}
