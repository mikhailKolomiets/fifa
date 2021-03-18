package site.fifa.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import site.fifa.entity.SmokeAccount;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SmokeAccountDto {

    private int cigarettes;
    private long secondsLast;
    private long secondsNext;
    private int moneySaves;
    private int moneyLose;
    private int moneyByLast;
    private String message;

    public SmokeAccountDto(SmokeAccount s) {
        this.cigarettes = s.getCigarettes();
        this.secondsLast = ChronoUnit.SECONDS.between(s.getLastSmoke(), LocalDateTime.now());
        if (cigarettes <= 0) {
            this.secondsNext = s.getCommonTime() - secondsLast;
        } else {
            this.secondsNext = (s.getCommonTime() + s.getCommonTime() * cigarettes * s.getType().getPercent() / 1000) - secondsLast;
        }
        this.moneyLose = cigarettes * s.getPriceForOne();
        this.moneySaves = s.getMoneyCount();
        this.moneyByLast = (int) (s.getPriceForOne() * (secondsLast - s.getCommonTime())) / s.getCommonTime();
    }

}
