package site.fifa.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import site.fifa.entity.Habit;
import site.fifa.entity.User;
import site.fifa.repository.HabitRepository;
import site.fifa.repository.UserRepository;

import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class HabitService {

    @Autowired
    private HabitRepository habitRepository;
    @Autowired
    private UserRepository userRepository;

    public Habit createHabit(Habit habit) {
        return habitRepository.save(habit);
    }

    public List<Habit> getHabits(Long userId) {
        return habitRepository.getByUserId(userId);
    }

    public String showUserHabits(Long userId) {
        User user = userRepository.findById(userId).orElse(null);

        if (user == null) {
            return "No user";
        }
        List<Habit> userHabits = habitRepository.getByUserId(userId);
        int amountOfHabits = userHabits.size();
        if (amountOfHabits == 0) {
            return user.getName() + " haven't habits";
        }
        StringBuilder result = new StringBuilder(user.getName() + "'s habits: ");
        double allPercent = 0;
        double percent;

        for (Habit h : userHabits) {
            percent = getPercent(h.getLastUsage(), amountOfHabits);
            allPercent += percent;
            result.append(h.getName())
                    .append(" (")
                    .append(getHabitInfo(h))
                    .append(" its ")
                    .append(new DecimalFormat("#0.000").format(percent))
                    .append("%) ");
        }
        result.append(". Summary: ").append(new DecimalFormat("#0.000").format(allPercent)).append("%.");

        return result.toString();
    }

    public Habit resetHabitTime(Long habitId) {
        Habit habit = habitRepository.findById(habitId).orElse(null);
        if (habit == null) {
            return null;
        }
        habit.setLastUsage(LocalDateTime.now());
        return habitRepository.save(habit);
    }

    private String getHabitInfo(Habit habit) {
        long seconds = ChronoUnit.SECONDS.between(habit.getLastUsage(), LocalDateTime.now());
        String result = seconds % 60 + "s";
        if (result.length() == 2) {
            result = "0" + result;
        }
        long timeUnit = seconds / 60;
        if (timeUnit != 0) {
            result = timeUnit % 60 + "m " + result;
            timeUnit /= 60;
            if (result.length() == 6) {
                result = "0" + result;
            }
            if (timeUnit != 0) {
                result = timeUnit % 24 + "h " + result;
                timeUnit /= 24;
                if (timeUnit != 0) {
                    result = timeUnit + "d " + result;
                }
            }
        }
        return result;
    }

    private double getPercent(LocalDateTime habitTime, int allHabits) {
        int daysToFinal = 100 / allHabits;
        long seconds = ChronoUnit.SECONDS.between(LocalDateTime.now(), habitTime);
        long finSeconds = ChronoUnit.SECONDS.between(habitTime.plusDays(daysToFinal), habitTime);

        return (double) seconds / finSeconds * 100;
    }


}
