package site.fifa.controller;

import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import site.fifa.entity.Habit;
import site.fifa.service.HabitService;

import java.time.LocalDateTime;
import java.util.List;

@RequestMapping("habit")
@RestController
public class HabitController {

    @Autowired
    private HabitService habitService;

    @ApiResponses({@ApiResponse(code = 200, message = "Create new habit")})
    @PostMapping("create")
    public Habit createHabit(@RequestParam Long userId, @RequestParam String name) {

        Habit habit = new Habit();
        habit.setUserId(userId);
        habit.setName(name);
        habit.setLastUsage(LocalDateTime.now());

        return habitService.createHabit(habit);
    }

    @ApiResponses({@ApiResponse(code = 200, message = "Show users habits")})
    @GetMapping("info/{userId}")
    public String showHabitsInfo(@PathVariable  Long userId) {
        return habitService.showUserHabits(userId);
    }

    @ApiResponses({@ApiResponse(code = 200, message = "Reset habit time to now")})
    @PostMapping("reset-time")
    public Habit resetHabit(@RequestParam Long habitId) {
        return habitService.resetHabitTime(habitId);
    }

    @ApiResponses({@ApiResponse(code = 200, message = "Back previous time to habit")})
    @PostMapping("back-time")
    public Habit revertHabitTime(@RequestParam Long habitId) {
        return habitService.revertHabitTime(habitId);
    }

    @ApiResponses({@ApiResponse(code = 200, message = "Delete an habit")})
    @DeleteMapping("delete")
    public Habit deleteHabit(@RequestParam Long habitId, @RequestParam String password) {
        return habitService.deleteHabit(habitId, password);
    }

    @ApiResponses({@ApiResponse(code = 200, message = "Get habits list")})
    @GetMapping("get-all/{userId}")
    public List<Habit> getHabits(@PathVariable  Long userId) {
        return habitService.getHabits(userId);
    }

}
