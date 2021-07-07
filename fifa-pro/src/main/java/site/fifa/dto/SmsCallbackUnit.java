package site.fifa.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@ToString
public class SmsCallbackUnit {

    //[MsgId=60E576ED3463355E5B8E386A, status=404, status_name=DELIVERED, idx=, points=0.086, to=380639482705,
    // sent_at=1625650925, donedate=1625650938, username=mihail.kolomiets@gmail.com, mcc=255, mnc=6, from=Test]

    private String msgId;
    private String status;
    private Double points;
    private LocalDateTime sent;


}
