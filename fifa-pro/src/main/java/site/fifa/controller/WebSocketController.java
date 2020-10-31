package site.fifa.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;

import org.springframework.stereotype.Controller;
import site.fifa.dto.MatchStepDto;
import site.fifa.service.MatchService;

@Controller
public class WebSocketController {

    @Autowired
    private MatchService matchService;

    @MessageMapping("p2p/{matchId}")
    public MatchStepDto play2player(@DestinationVariable String matchId, String message) {
        System.out.println(matchId + " " + message);
        return matchService.makeStepWithCPU(Long.parseLong(matchId), Integer.parseInt(message));

    }

}
