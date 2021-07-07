package site.fifa.controller;

import org.springframework.web.bind.annotation.*;
import site.fifa.dto.SmsCallbackUnit;
import site.fifa.dto.pojo.MPdata;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("test")
public class TestController {

    private static Set<SmsCallbackUnit> callbackList = new HashSet<>();

    private final String MSG_ID_KEY = "MsgId";
    private final String STATUS_KEY = "status_name";
    private final String POINTS_KEY = "points";
    private final String SENT_KEY = "sent_at";

    @GetMapping("mpd")
    public String multipleDate(@RequestParam(name = MSG_ID_KEY) List<String> msgIdList,
                               @RequestParam(name = STATUS_KEY) List<String> statusList,
                               @RequestParam(name = POINTS_KEY) List<String> pointList,
                               @RequestParam(name = SENT_KEY) List<String> sentList) {


        for (int i = 0; i < msgIdList.size(); i++) {
            SmsCallbackUnit unit = new SmsCallbackUnit(
                    msgIdList.get(i),
                    statusList.get(i),
                    Double.parseDouble(pointList.get(i)),
                    LocalDateTime.ofInstant(Instant.ofEpochSecond(Long.parseLong(sentList.get(i))), TimeZone.getDefault().toZoneId())
            );

            if (callbackList.add(unit)) {
                System.out.println("Parse callback unit: \n" + unit);
            }
        }
        System.out.println("local unit list size : " + callbackList.size());
        //System.out.println("Sms send result: \n" + mPdata.entrySet());


        return "OK";
    }

}
