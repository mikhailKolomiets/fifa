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
        List<Habit> habits = habitRepository.getByUserId(userId);
        //todo dirty for LAST TIME USE
        long lastTime;
        for (Habit h : habits) {
            lastTime = ChronoUnit.SECONDS.between(h.getLastUsage(), LocalDateTime.now());
            h.setHiSeconds(lastTime);
        }
        return habits;
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
        double maxForHabit = userHabits.size() != 0 ? 100d / userHabits.size() : 0;
        boolean isMax;
        int maxHabit = 0;

        for (Habit h : userHabits) {
            isMax = false;
            percent = getPercent(h.getLastUsage());
            if (percent > maxForHabit) {
                percent = maxForHabit;
                isMax = true;
                maxHabit += 1;
            }
            allPercent += percent;
            result.append(h.getName())
                    .append(" (HI: ")
                    .append(getHabitHigth(h))
                    .append(" its ")
                    .append(new DecimalFormat("#0.000").format(percent))
                    .append("%");
            if (isMax) {
                result.append(" is max");
            }
            result.append(") ");

        }
        if(maxHabit != userHabits.size()) {
            result.append(". Summary: ").append(new DecimalFormat("#0.000").format(allPercent)).append("%.");
        } else {
            result.append(" All habits is done. Congratulations!!!");
        }

        return result.toString();
    }

    public Habit resetHabitTime(Long habitId) {
        Habit habit = habitRepository.findById(habitId).orElse(null);
        if (habit == null) {
            return null;
        }
        habit.setPreLastUsage(habit.getLastUsage());
        habit.setLastUsage(LocalDateTime.now());
        long hiSec = ChronoUnit.SECONDS.between(habit.getPreLastUsage(), LocalDateTime.now());
        if (habit.getHiSeconds() < hiSec) {
            habit.setHiSeconds(hiSec);
        }
        return habitRepository.save(habit);
    }

    public Habit revertHabitTime(Long habitId) {
        Habit habit = habitRepository.findById(habitId).orElse(null);
        if (habit == null || habit.getPreLastUsage() == null) {
            return null;
        }

        habit.setLastUsage(habit.getPreLastUsage());

        return habitRepository.save(habit);
    }

    public Habit deleteHabit(Long id, String userPassword) {
        Habit habit = habitRepository.findById(id).orElse(null);
        System.out.println(userPassword);
        User user = userRepository.findByPassword(userPassword);
        if (habit == null || user == null || !user.getId().equals(habit.getUserId())) {
            return null;
        }
        habitRepository.delete(habit);
        return habit;
    }

    private String getHabitHigth(Habit habit) {
        long seconds = habit.getHiSeconds();
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

    private double getPercent(LocalDateTime habitTime) {
        long seconds = ChronoUnit.SECONDS.between(LocalDateTime.now(), habitTime);
        long finSeconds = ChronoUnit.SECONDS.between(habitTime.plusDays(100), habitTime);

        return (double) seconds / finSeconds * 100;
    }


}
