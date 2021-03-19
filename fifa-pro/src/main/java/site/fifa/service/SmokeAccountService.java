package site.fifa.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import site.fifa.dto.SmokeAccountDto;
import site.fifa.entity.SmokeAccount;
import site.fifa.repository.SmokeAccountRepository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
public class SmokeAccountService {

    @Autowired
    private SmokeAccountRepository smokeAccountRepository;
    private final List<SmokeAccount> accounts = new ArrayList<>();

    public SmokeAccountDto createAccount(SmokeAccount s) {
        SmokeAccountDto smokeAccountDto = getAccountDtoByUserId(s.getUserId());
        if (smokeAccountDto != null) {
            return smokeAccountDto;
        }

        return new SmokeAccountDto(smokeAccountRepository.save(s));
    }

    public SmokeAccountDto getAccountDtoByUserId(Long userId) {
        SmokeAccount s = getByUserId(userId);
        if (s == null) {
            s = smokeAccountRepository.findFirstByUserId(userId);
            if (s == null) {
                return null;
            }
            accounts.add(s);
        }
        return new SmokeAccountDto(s);
    }

    public SmokeAccountDto resetTime(Long userId) {
        SmokeAccount s = getByUserId(userId);
        if (s == null) {
            return null;
        }
        s.setMoneyCount(
                s.getMoneyCount() + (int) (s.getPriceForOne() * (ChronoUnit.SECONDS.between(s.getLastSmoke(), LocalDateTime.now()) - s.getCommonTime())) / s.getCommonTime()
        );
        s.setPreLastSmoke(s.getLastSmoke());
        s.setLastSmoke(LocalDateTime.now());
        s.setCigarettes(s.getCigarettes() + 1);
        return new SmokeAccountDto(smokeAccountRepository.save(s));
    }

    public SmokeAccountDto reverseTime(Long userId) {
        SmokeAccount s = getByUserId(userId);
        if (s == null) {
            return null;
        }
        if (s.getLastSmoke() != s.getPreLastSmoke()) {
            s.setMoneyCount(
                    s.getMoneyCount() - (int) (s.getPriceForOne() * (ChronoUnit.SECONDS.between(s.getPreLastSmoke(), s.getLastSmoke()) - s.getCommonTime())) / s.getCommonTime()
            );
            s.setLastSmoke(s.getPreLastSmoke());
            s.setCigarettes(s.getCigarettes() - 1);
        }
        return new SmokeAccountDto(smokeAccountRepository.save(s));
    }

    public SmokeAccountDto sleepMode(Long userId) {
        SmokeAccount s = getByUserId(userId);
        if (s == null) {
            return null;
        }
        LocalDateTime timeAfterSleep = LocalDateTime.now().minusSeconds(s.getCommonTime() + s.getCommonTime() * s.getCigarettes() * s.getType().getPercent() / 1000);
        s.setLastSmoke(timeAfterSleep);
        return new SmokeAccountDto(smokeAccountRepository.save(s));
    }

    public void delete(Long userId) {
        SmokeAccount s = smokeAccountRepository.findFirstByUserId(userId);
        if (s != null) {
            accounts.remove(getByUserId(userId));
            smokeAccountRepository.delete(s);
        }
    }

    private SmokeAccount getByUserId(Long userId) {
        for (SmokeAccount s : accounts) {
            if (s.getUserId().equals(userId)) {
                return s;
            }
        }
        return null;
    }

}
