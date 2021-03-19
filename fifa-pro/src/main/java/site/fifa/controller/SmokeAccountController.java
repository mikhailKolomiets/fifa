package site.fifa.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import site.fifa.dto.SmokeAccountDto;
import site.fifa.dto.UserDTO;
import site.fifa.entity.SmokeAccount;
import site.fifa.service.SmokeAccountService;
import site.fifa.service.UserService;

import java.time.LocalDateTime;

@RestController
@RequestMapping("smoke")
public class SmokeAccountController {

    @Autowired
    private SmokeAccountService smokeAccountService;
    @Autowired
    private UserService userService;

    @PostMapping("create/{key}")
    public SmokeAccountDto create(@RequestBody SmokeAccount smokeAccount, @PathVariable String key) {
        UserDTO userDTO = userService.findUserInSessionByKey(key);
        if (userDTO == null) {
            return null;
        }
        smokeAccount.setUserId(userDTO.getUser().getId());
        smokeAccount.setLastSmoke(LocalDateTime.now());
        smokeAccount.setPreLastSmoke(LocalDateTime.now());
        return smokeAccountService.createAccount(smokeAccount);
    }

    @GetMapping("get/{key}")
    public SmokeAccountDto getSmokeAccount(@PathVariable String key) {
        UserDTO userDTO = userService.findUserInSessionByKey(key);
        if (userDTO == null) {
            return null;
        }
        return smokeAccountService.getAccountDtoByUserId(userDTO.getUser().getId());
    }

    @PutMapping("update-time/{key}")
    public SmokeAccountDto updateTime(@PathVariable String key) {
        UserDTO userDTO = userService.findUserInSessionByKey(key);
        if (userDTO == null) {
            return null;
        }
        return smokeAccountService.resetTime(userDTO.getUser().getId());
    }

    @PutMapping("reverse-time/{key}")
    public SmokeAccountDto reverseTimeToLast(@PathVariable String key) {
        UserDTO userDTO = userService.findUserInSessionByKey(key);
        if (userDTO == null) {
            return null;
        }
        return smokeAccountService.reverseTime(userDTO.getUser().getId());
    }

    @PutMapping("after-sleep/{key}")
    public SmokeAccountDto updateAfterSleep(@PathVariable String key) {
        UserDTO userDTO = userService.findUserInSessionByKey(key);
        if (userDTO == null) {
            return null;
        }
        return smokeAccountService.sleepMode(userDTO.getUser().getId());
    }

}
