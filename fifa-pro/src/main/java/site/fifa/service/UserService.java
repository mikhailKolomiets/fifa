package site.fifa.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import site.fifa.dto.UserDTO;
import site.fifa.entity.User;
import site.fifa.repository.UserRepository;

import javax.servlet.ServletRequest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ServletRequest servletRequest;

    private List<UserDTO> userOnline = new ArrayList<>();

    public UserDTO createUser(User user) {

        System.out.println(servletRequest.getRemoteAddr());
        System.out.println(servletRequest.getLocalAddr());

        User savesUser = userRepository.findByName(user.getName());

        //todo any else validation with refactor
        if (savesUser != null) {
            return UserDTO.builder().user(user).message("User with name " + user.getName() + " is present").build();
        }

        user.setLastEnter(LocalDateTime.now());
        savesUser = userRepository.save(user);
        String key = UUID.randomUUID().toString();

        UserDTO result = UserDTO.builder().user(user).sessionKey(key).build();
        userOnline.add(result);

        return result;
    }

    public UserDTO login(User user) {

        UserDTO sessionUser = findUserInSession(user.getName());
        if (sessionUser != null)
            return sessionUser;

        User logUser = userRepository.findByName(user.getName());
        if (logUser == null) {
            return UserDTO.builder().user(user).message("User with name " + user.getName() + " is absent").build();
        } else if (!user.getPassword().equals(logUser.getPassword())) {
            return UserDTO.builder().user(user).message("Password incorrect").build();
        }
        logUser.setLastEnter(LocalDateTime.now());

        sessionUser = UserDTO.builder().user(logUser).sessionKey(UUID.randomUUID().toString()).build();
        userOnline.add(sessionUser);

        System.out.println(user.getName() + " is login in");
        return sessionUser;
    }

    public UserDTO findUserInSessionByKey(String key) {
        for (UserDTO u : userOnline) {
            if(u.getSessionKey().equals(key)) {
                u.getUser().setLastEnter(LocalDateTime.now());
                return u;
            }
        }
        return null;
    }

    public void logout(String key) {
        UserDTO userDTO = findUserInSessionByKey(key);
        if (userDTO != null)
            userDTO.getUser().setLastEnter(LocalDateTime.now().minusMinutes(4));
        deleteByTimeOut();
    }

    public UserDTO findUserInSession(String name) {
        for (UserDTO u : userOnline) {
            if(u.getUser().getName().equals(name)) {
                return u;
            }
        }
        return null;
    }

    @Scheduled(cron = "10 * * * * *")
    public void deleteByTimeOut() {
        // todo add to constants
        System.out.println("check session expired users. users online: " + userOnline.size());
        for (UserDTO u : userOnline) {
            if (u.getUser().getLastEnter().isBefore(LocalDateTime.now().minusMinutes(4))) {
                u.getUser().setLastEnter(LocalDateTime.now());
                userRepository.save(u.getUser());
                u.getUser().setLastEnter(LocalDateTime.now().minusMinutes(4+1));
            }
        }
        userOnline = userOnline.stream().filter(u -> u.getUser().getLastEnter().isAfter(LocalDateTime.now().minusMinutes(4))).collect(Collectors.toList());

    }

}
