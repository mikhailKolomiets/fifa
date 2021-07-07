package site.fifa.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import site.fifa.dto.pojo.MPdata;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("test")
public class TestController {

    @GetMapping("mpd")
    public String multipleDate(@RequestParam Map<String, List<String>> mPdata) {

        System.out.println(mPdata.get("data"));



        return "Sms send result: " + mPdata.entrySet();
    }

}
