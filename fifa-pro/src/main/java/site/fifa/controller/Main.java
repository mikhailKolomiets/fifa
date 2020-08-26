package site.fifa.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.Mapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import site.fifa.service.TeamService;

@RestController
@RequestMapping("main")
public class Main {

    @Autowired
    private TeamService teamService;

    @GetMapping("team-create")
    public String someMethod() {
        teamService.createNewTeam();
        return "Hi in the fifa game";
    }

}
