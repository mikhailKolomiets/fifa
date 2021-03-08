package site.fifa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import site.fifa.entity.ChatMessage;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    @Query(nativeQuery = true, value = "select * from chat_message cm order by cm.id desc limit :amount")
    List<ChatMessage> getLastMessages(@Param("amount") Integer amount);

}
