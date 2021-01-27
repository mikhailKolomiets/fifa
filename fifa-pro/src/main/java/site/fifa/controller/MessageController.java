package site.fifa.controller;

import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import site.fifa.entity.message.Message;
import site.fifa.service.MessageService;

import java.util.List;

@RestController
@RequestMapping("message")
public class MessageController {

    @Autowired
    private MessageService messageService;

    @ApiResponses({@ApiResponse(code = 200, message = "Get all messages of team")})
    @GetMapping("team/{teamId}")
    public List<Message> getByTeamId(@PathVariable Long teamId) {
        return messageService.getTeamMessages(teamId);
    }

    @ApiResponses({@ApiResponse(code = 200, message = "Get amount of new message for team")})
    @GetMapping("team-new-amount/{teamId}")
    public String getAmountMewMessageForTeam(@PathVariable Long teamId) {
        return messageService.getAmountNewTeamMessage(teamId) + "";
    }

    @ApiResponses({@ApiResponse(code = 200, message = "Make all team messages as readed")})
    @PutMapping("team/make-all-read")
    public void makeReadedMessageForTeam(@RequestParam("teamId") Long teamId) {
        messageService.updateAllReadForTeam(teamId);
    }

}
