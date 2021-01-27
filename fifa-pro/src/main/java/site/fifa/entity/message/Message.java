package site.fifa.entity.message;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@Data
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private MessageTypeEnum type;
    private Long fromId;
    private Long toId;

    private String body;
    private LocalDateTime createTime;
    private boolean readed;

}
