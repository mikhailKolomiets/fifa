package site.fifa.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;

import org.springframework.stereotype.Controller;
import site.fifa.dto.MatchStepDto;
import site.fifa.dto.PlaySide;
import site.fifa.service.MatchService;

@Controller
public class WebSocketController {

    @Autowired
    private MatchService matchService;

    @MessageMapping("p2p/{matchId}")
    public MatchStepDto play2player(@DestinationVariable String matchId, String message) {
        // todo update play side according team id in session
        return matchService.makeGameStep(Long.parseLong(matchId), message.substring(1).equals("1") ? PlaySide.FiRST_TEAM : PlaySide.SECOND_TEAM, Integer.parseInt(message.substring(0,1)));
    }

}
