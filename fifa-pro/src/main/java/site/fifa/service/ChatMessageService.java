package site.fifa.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import site.fifa.constants.GameConstants;
import site.fifa.entity.ChatMessage;
import site.fifa.repository.ChatMessageRepository;

import java.util.List;

@Service
public class ChatMessageService {

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    public List<ChatMessage> getAll() {
        return chatMessageRepository.getLastMessages(GameConstants.CHAT_MESSAGE_AMOUNT);
    }

    public ChatMessage saveMessage(ChatMessage chatMessage) {
        return chatMessageRepository.save(chatMessage);
    }

}
