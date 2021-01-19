package site.fifa.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import site.fifa.constants.GameConstants;
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

        String userIp = servletRequest.getRemoteAddr();

        User savesUser = userRepository.findByName(user.getName());

        //todo any else validation with refactor
        if (savesUser != null) {
            return UserDTO.builder().user(user).message("User with name " + user.getName() + " is present").build();
        }
        savesUser = userRepository.findFirstByUserLastIp(userIp);
        if (savesUser != null && savesUser.getLastEnter().isAfter(LocalDateTime.now().minusDays(GameConstants.USER_IP_CHECK_DAYS))) {
            return UserDTO.builder().user(user).message("Cant create user. Ip is use another user's.").build();
        }

        user.setLastEnter(LocalDateTime.now());
        user.setUserLastIp(servletRequest.getRemoteAddr());
        String key = UUID.randomUUID().toString();

        userRepository.save(user);

        UserDTO result = UserDTO.builder().user(user).sessionKey(key).build();
        userOnline.add(result);

        return result;
    }

    public UserDTO login(User user) {

        UserDTO sessionUser = findUserInSession(user.getName());
        if (sessionUser != null && sessionUser.getUser().getUserLastIp().equals(servletRequest.getRemoteAddr())) {
                return sessionUser;
        }

        User logUser = userRepository.findByName(user.getName());
        if (logUser == null) {
            return UserDTO.builder().user(user).message("User with name " + user.getName() + " is absent").build();
        } else if (!user.getPassword().equals(logUser.getPassword())) {
            return UserDTO.builder().user(user).message("Password incorrect").build();
        }

        logUser.setUserLastIp(servletRequest.getRemoteAddr());
        logUser.setSessionKey(UUID.randomUUID().toString());
        userRepository.save(logUser);
        System.out.println(user.getName() + " is login in");
        return putUserInSession(logUser);
    }

    public UserDTO findUserInSessionByKey(String key) {
        for (UserDTO u : userOnline) {
            if(u.getSessionKey().equals(key) && servletRequest.getRemoteAddr().equals(u.getUser().getUserLastIp())) {
                u.getUser().setLastEnter(LocalDateTime.now());
                return u;
            }
        }
        User user = userRepository.findFirstByUserLastIp(servletRequest.getRemoteAddr());
        if (user != null && key.equals(user.getSessionKey())) {
            System.out.println("got " + user.getName() + " from old session");
            return putUserInSession(user);
        }
        return null;
    }

    public void logout(String key) {
        UserDTO userDTO = findUserInSessionByKey(key);
        if (userDTO != null) {
            userDTO.getUser().setLastEnter(LocalDateTime.now().minusMinutes(4));
            User user = userRepository.findFirstByUserLastIp(servletRequest.getRemoteAddr());
            if (user != null) {
                user.setLastEnter(LocalDateTime.now());
                user.setSessionKey(null);
                System.out.println(user.getName() + " is login out");
                userRepository.save(user);
            }
        }
        userOnline.remove(userDTO);
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
        System.out.println("check session expired users. Online: " + userOnline.size());
        for (UserDTO u : userOnline) {
            if (u.getUser().getLastEnter().isBefore(LocalDateTime.now().minusSeconds(GameConstants.USER_LOGOUT_TIMEOUT))) {
                u.getUser().setLastEnter(LocalDateTime.now());
                System.out.println("try save with key " + u.getUser().getSessionKey());
                userRepository.save(u.getUser());
                u.getUser().setLastEnter(LocalDateTime.now().minusSeconds(GameConstants.USER_LOGOUT_TIMEOUT));
            }
        }
        userOnline = userOnline.stream().filter(u -> u.getUser().getLastEnter().isAfter(LocalDateTime.now().minusSeconds(GameConstants.USER_LOGOUT_TIMEOUT))).collect(Collectors.toList());

    }

    private UserDTO putUserInSession(User user) {
        userOnline = userOnline.stream().filter(u -> !u.getUser().getName().equals(user.getName())).collect(Collectors.toList());

        user.setLastEnter(LocalDateTime.now());

        UserDTO result = UserDTO.builder().user(user).sessionKey(user.getSessionKey()).build();

        userOnline.add(result);
        return result;
    }

}
