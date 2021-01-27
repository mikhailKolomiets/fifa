package site.fifa.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import site.fifa.entity.message.Message;
import site.fifa.entity.message.MessageTypeEnum;

import java.util.List;

public interface MessageRepository extends CrudRepository<Message, Long> {

    List<Message> findByToIdAndType(Long toId, MessageTypeEnum type);

    List<Message> findByReadedFalseAndToId(Long toId);

    @Modifying
    @Query(nativeQuery = true, value = "update message set readed=true where to_id= :id and type = 0")
    void makeAllReadForTeamId(@Param("id") Long teamId);

}
