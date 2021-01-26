package site.fifa.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import site.fifa.dto.CounterDto;
import site.fifa.service.UserService;

@RestController
@RequestMapping("counter")
public class CounterController {

    @Autowired
    private UserService userService;

    @GetMapping("online")
    public CounterDto getOnline() {
        return userService.getCounter();
    }

}
