package site.fifa.dto;

import lombok.Builder;
import lombok.Data;
import site.fifa.entity.User;

@Data
@Builder
public class UserDTO {

    private User user;
    private String sessionKey;
    private String message;

}
