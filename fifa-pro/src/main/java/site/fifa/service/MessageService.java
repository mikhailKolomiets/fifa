package site.fifa.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.fifa.entity.message.Message;
import site.fifa.entity.message.MessageTypeEnum;
import site.fifa.repository.MessageRepository;

import java.util.Collections;
import java.util.List;

@Service
public class MessageService {

    @Autowired
    private MessageRepository messageRepository;

    public List<Message> getTeamMessages(Long teamId) {
        List<Message> result = messageRepository.findByToIdAndType(teamId, MessageTypeEnum.TEAM_ACTION);
        Collections.reverse(result);
        return result;
    }

    public Integer getAmountNewTeamMessage(Long teamId) {
        return messageRepository.findByReadedFalseAndToId(teamId).size();
    }

    @Transactional
    public void updateAllReadForTeam(Long toId) {
        messageRepository.makeAllReadForTeamId(toId);
    }

}
