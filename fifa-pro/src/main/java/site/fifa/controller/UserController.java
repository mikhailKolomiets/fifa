package site.fifa.controller;

import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import site.fifa.dto.UserDTO;
import site.fifa.entity.User;
import site.fifa.service.UserService;

@RestController
@RequestMapping("user")
public class UserController {

    @Autowired
    private UserService userService;

    @ApiResponses({@ApiResponse(code = 200, message = "Created new user or info why not")})
    @PostMapping("registration")
    public UserDTO registration(@RequestParam String userName, @RequestParam String email, @RequestParam String password) {
        User user = new User();
        user.setEmail(email);
        user.setName(userName);
        user.setPassword(password);
        return userService.createUser(user);
    }

    @ApiResponses({@ApiResponse(code = 200, message = "User login or tell in message why not")})
    @PostMapping("login")
    public UserDTO login(@RequestParam String username, @RequestParam String password) {
        User user = new User();
        user.setName(username);
        user.setPassword(password);
        return  userService.login(user);
    }

    @ApiResponses({@ApiResponse(code = 200, message = "Return present user by key")})
    @GetMapping("check")
    public UserDTO checkUserByKey(@RequestParam String key) {
        return userService.findUserInSessionByKey(key);
    }

    @ApiResponses({@ApiResponse(code = 200, message = "logout user")})
    @GetMapping("logout")
    public void logout(@RequestParam String key) {
        userService.logout(key);
    }

}
