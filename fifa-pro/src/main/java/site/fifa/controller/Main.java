package site.fifa.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Main {

    @GetMapping("secury")
    public String someMethod() {
        return "Hi in the fifa game";
    }

}
