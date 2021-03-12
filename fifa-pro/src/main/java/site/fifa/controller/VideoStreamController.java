package site.fifa.controller;

import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("video")
public class VideoStreamController {

    private List<String> tokens;

    @GetMapping("get-all")
    public List<String> getAllTokens() {
        return tokens;
    }

    @PostMapping("add")
    public void addToken(@RequestParam String token) {
        if (tokens == null) {
            tokens = new ArrayList<>();
        }
        tokens.add(token);
        System.out.println("token: " + token);
    }

}
