package site.fifa.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import site.fifa.constants.GameConstants;
import site.fifa.dto.UserDTO;
import site.fifa.entity.ChatMessage;
import site.fifa.service.ChatMessageService;
import site.fifa.service.UserService;

import javax.servlet.ServletRequest;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("chat")
public class ChatController {

    private List<ChatMessage> messages;
    @Autowired
    private ChatMessageService chatMessageService;
    @Autowired
    private UserService userService;
    @Autowired
    private ServletRequest servletRequest;

    @MessageMapping("chat/general/{key}")
    public ChatMessage sendMessage(String body, @DestinationVariable String key) {
        UserDTO userDTO = userService.samplqFindUserByKey(key);
        String from = userDTO == null ? GameConstants.GUEST_NAME : userDTO.getUser().getName();
        ChatMessage message = ChatMessage.builder().fromName(from).messageBody(body).created(LocalDateTime.now()).build();
        message = chatMessageService.saveMessage(message);
        if (messages == null) {
            getChatMessages();
        }
        messages.add(0, message);
        return message;
    }

    @GetMapping("get-all")
    public List<ChatMessage> getChatMessages() {
        if (messages == null) {
            return messages = chatMessageService.getAll();
        }
        return messages;
    }

}
