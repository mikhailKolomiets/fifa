package site.fifa.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import site.fifa.dto.MatchStepDto;
import site.fifa.dto.TeamDTO;
import site.fifa.entity.Player;
import site.fifa.entity.PlayerType;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MatchService {

    @Autowired
    private TeamService teamService;

    public String playAutoMatch(Long team1, Long team2) {
        TeamDTO firstTeam = teamService.getTeamById(team1);
        TeamDTO secondTeam = teamService.getTeamById(team2);

        StringBuilder matchLog = new StringBuilder("Матч начался!<br>");

        int position = 2;
        MatchStepDto step = new MatchStepDto();

        for(int i = 0; i <= 90; i++) {
            matchLog.append(i).append(" m: ").append(position).append(step.isFirstTeamBall()).append(" ");
            step.setFirstTeamAction((int)(Math.random() * 3) + 1);
            step.setSecondTeamAction((int)(Math.random() * 3) + 1);
            if(position == 1) {
                step.setFirstPlayer(getRandomPlayerByType(firstTeam.getPlayers(), PlayerType.CD));
                step.setSecondPlayer(getRandomPlayerByType(secondTeam.getPlayers(), PlayerType.ST));
                if (!step.isFirstTeamBall()) {
                    if (step.getSecondTeamAction() == 1) {
                        step.setFirstTeamBall(true);
                        step.setFirstPlayer(getRandomPlayerByType(firstTeam.getPlayers(), PlayerType.GK));
                        if (Math.random() * step.getSecondPlayer().getSkill() * step.getSecondTeamChance() / 100 > Math.random() * step.getFirstPlayer().getSkill()) {
                            step.setGoalSecondTeam(step.getGoalSecondTeam() + 1);
                            matchLog.append(step.getSecondPlayer().getName()).append(" забил гол!!! ").append(firstTeam.getTeam().getName())
                                    .append(" ").append(step.showGoals()).append(" ").append(secondTeam.getTeam().getName()).append("<br>");
                            position = 2;
                        } else {
                            matchLog.append(step.getSecondPlayer().getName()).append(" бьет, но ").append(step.getFirstPlayer().getName()).append(" спасает ворота!<br>");
                        }
                    } else if (step.getSecondTeamAction() == 2 || step.getSecondTeamAction() == 3) {
                        if (Math.random() * step.getSecondPlayer().getSpeed() * step.getSecondTeamChance() / 100 > Math.random() * step.getFirstPlayer().getSkill() * step.getFirstTeamChance() / 100) {
                            matchLog.append(step.getSecondPlayer().getName()).append(" обводит ").append(step.getFirstPlayer().getName()).append("<br>");
                            step.plusChance(2);
                        } else {
                            matchLog.append(step.getSecondPlayer().getName()).append(" потерял мяч<br>");
                            step.setFirstTeamBall(true);
                            step.minusChance(2);
                        }
                    }
                } else {
                    if (step.getFirstTeamAction() == 1 || step.getFirstTeamAction() == 2) {
                        matchLog.append(step.getFirstPlayer().getName()).append(" выбивает мяч <br>");
                        position = 2;
                    } else {
                        matchLog.append(step.getFirstPlayer().getName()).append(" делает пас <br>");
                        position = 1;
                        step.plusChance(2);
                    }
                }
            } else if (position == 2) {
                step.setFirstPlayer(getRandomPlayerByType(firstTeam.getPlayers(), PlayerType.MD));
                step.setSecondPlayer(getRandomPlayerByType(secondTeam.getPlayers(), PlayerType.MD));
                if (step.isFirstTeamBall()) {
                    if (step.getFirstTeamAction() == 1) {
                        if (Math.random()*step.getFirstPlayer().getSpeed() * step.getFirstTeamChance() / 100
                                > Math.random()*step.getSecondPlayer().getSpeed()) {
                            matchLog.append(step.getFirstPlayer().getName()).append(" успешно прошол вперед<br>");
                            position = 3;
                        } else {
                            matchLog.append(step.getFirstPlayer().getName()).append(" потерял мяч<br>");
                            step.setFirstTeamBall(false);
                        }
                    } else if (step.getFirstTeamAction() == 2) {
                        if (Math.random() * step.getFirstPlayer().getSkill() * step.getFirstTeamChance() / 100
                                > Math.random() * step.getSecondPlayer().getSkill() * step.getSecondTeamChance() / 100 ) {
                            step.plusChance(1);
                        } else {
                            step.setFirstTeamBall(false);
                            step.minusChance(1);
                        }
                    } else if (step.getFirstTeamAction() == 3) {
                        if (Math.random() * step.getFirstPlayer().getSkill()
                                > Math.random() * step.getSecondPlayer().getSpeed() * step.getSecondTeamChance() / 100) {
                            position = 1;
                            step.plusChance(1);
                            matchLog.append(step.getFirstPlayer().getName()).append(" передача назад<br>");
                        } else {
                            matchLog.append(step.getFirstPlayer().getName()).append(" потерял мяч<br>");
                            step.setFirstTeamBall(false);
                        }
                    }
                } else {
                    if (step.getSecondTeamAction() == 1) {
                        if (Math.random() * step.getSecondPlayer().getSpeed() * step.getSecondTeamChance() / 100
                                > Math.random()*step.getFirstPlayer().getSpeed()) {
                            matchLog.append(step.getSecondPlayer().getName()).append(" успешно прошол вперед<br>");
                            position = 1;
                        } else {
                            matchLog.append(step.getSecondPlayer().getName()).append(" потерял мяч<br>");
                            step.setFirstTeamBall(true);
                        }
                    } else if (step.getSecondTeamAction() == 2) {
                        if (Math.random() * step.getSecondPlayer().getSkill() * step.getSecondTeamChance() / 100
                                > Math.random() * step.getFirstPlayer().getSkill() * step.getFirstTeamChance() / 100 ) {
                            step.plusChance(2);
                        } else {
                            step.setFirstTeamBall(true);
                            step.minusChance(2);
                        }
                    } else if (step.getSecondTeamAction() == 3) {
                        if (Math.random() * step.getSecondPlayer().getSkill()
                                > Math.random() * step.getFirstPlayer().getSpeed() * step.getFirstTeamChance() / 100) {
                            position = 3;
                            step.plusChance(2);
                            matchLog.append(step.getSecondPlayer().getName()).append(" передача назад<br>");
                        } else {
                            matchLog.append(step.getSecondPlayer().getName()).append(" потерял мяч<br>");
                            step.setFirstTeamBall(true);
                        }
                    }
                }
            } else if (position == 3) {
                step.setFirstPlayer(getRandomPlayerByType(firstTeam.getPlayers(), PlayerType.ST));
                step.setSecondPlayer(getRandomPlayerByType(secondTeam.getPlayers(), PlayerType.CD));
                if (step.isFirstTeamBall()) {
                    if (step.getFirstTeamAction() == 1) {
                        step.setFirstTeamBall(false);
                        step.setSecondPlayer(getRandomPlayerByType(secondTeam.getPlayers(), PlayerType.GK));
                        if (Math.random() * step.getFirstPlayer().getSkill() * step.getFirstTeamChance() / 100 > Math.random() * step.getSecondPlayer().getSkill()) {
                            step.setGoalFirstTeam(step.getGoalFirstTeam() + 1);
                            matchLog.append(step.getFirstPlayer().getName()).append(" забил гол!!! ").append(firstTeam.getTeam().getName())
                                    .append(" ").append(step.showGoals()).append(" ").append(secondTeam.getTeam().getName()).append("<br>");
                            position = 2;
                        } else {
                            matchLog.append(step.getFirstPlayer().getName()).append(" бьет, но ").append(step.getSecondPlayer().getName()).append(" спасает ворота!<br>");
                        }
                    } else if (step.getFirstTeamAction() == 2 || step.getFirstTeamAction() == 3) {
                        if (Math.random() * step.getFirstPlayer().getSpeed() * step.getFirstTeamChance() / 100 > Math.random() * step.getSecondPlayer().getSkill() * step.getSecondTeamChance() / 100) {
                            matchLog.append(step.getFirstPlayer().getName()).append(" обводит ").append(step.getSecondPlayer().getName()).append("<br>");
                            step.plusChance(1);
                        } else {
                            matchLog.append(step.getFirstPlayer().getName()).append(" потерял мяч<br>");
                            step.setFirstTeamBall(false);
                            step.minusChance(1);
                        }
                    }
                } else {
                    if (step.getSecondTeamAction() == 1 || step.getSecondTeamAction() == 2) {
                        matchLog.append(step.getSecondPlayer().getName()).append(" выбивает мяч <br>");
                            position = 2;
                        } else {
                        matchLog.append(step.getSecondPlayer().getName()).append(" делает пас <br>");
                        position = 3;
                        step.plusChance(2);
                    }
                    }
                }
        }
        matchLog.append("Матч закончился! ").append(firstTeam.getTeam().getName())
                .append(" ").append(step.showGoals()).append(" ").append(secondTeam.getTeam().getName()).append("<br>");
        return matchLog.toString();
    }

    private Player getRandomPlayerByType(List<Player> players, PlayerType type) {
        List<Player> filtered = players.stream().filter(player -> player.getType() == type).collect(Collectors.toList());
        return filtered.get((int)(Math.random() * filtered.size()));
    }
}
