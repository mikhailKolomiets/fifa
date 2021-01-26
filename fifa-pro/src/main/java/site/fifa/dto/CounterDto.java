package site.fifa.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CounterDto {
    private Long onlineToday;
    private Long onlineAllTime;
    private List<String> usersOnline;
    private Long guestsOnline;

}
