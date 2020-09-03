package site.fifa.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import site.fifa.dto.MatchDto;
import site.fifa.dto.MatchStepDto;
import site.fifa.dto.PlaySide;
import site.fifa.dto.TeamDTO;
import site.fifa.entity.Player;
import site.fifa.entity.PlayerType;
import site.fifa.entity.match.MatchPlay;
import site.fifa.entity.match.MatchStatus;
import site.fifa.entity.match.MatchType;
import site.fifa.repository.MatchRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MatchService {

    @Autowired
    private TeamService teamService;
    @Autowired
    private MatchRepository matchRepository;

    private static ArrayList<MatchStepDto> matchStepDtos = new ArrayList<>();

    public MatchDto startMatchWithPC(Long firstTeamId, Long secondTeamId) {
        MatchPlay match = matchRepository.getByFirstTeamIdAndSecondTeamIdAndStatus(firstTeamId, secondTeamId, MatchStatus.STARTED)
                .stream().findAny().orElse(null);
        if (match == null) {
            match = matchRepository.save( new MatchPlay(MatchStatus.STARTED, MatchType.FRIENDLY, LocalDate.now(), firstTeamId, secondTeamId));
        }

        MatchStepDto matchStepDto = new MatchStepDto();
        matchStepDto.setMatchDto(new MatchDto(match.getId(), PlaySide.CPU, teamService.getTeamById(firstTeamId), teamService.getTeamById(secondTeamId)));
        matchStepDtos.add(matchStepDto);
        return matchStepDto.getMatchDto();
    }

    public MatchStepDto makeStepWithCPU(Long matchId, int action) {

        MatchStepDto matchStepDto = getMatchStepDtoById(matchId);
        double attackFactor;

        if (matchStepDto.getStep() > 90) {
            matchStepDto.setLastStepLog(matchStepDto.showGoals());
            matchStepDto.getLog().add("Матч окончен!" + matchStepDto.getLastStepLog());
            return matchStepDto;//todo save result
        }

        int addition;
        double teamActionRandom = Math.random() * 100;
        if (matchStepDto.getSecondTeamChance() > 75)
            matchStepDto.setSecondTeamAction(teamActionRandom > 20 ? 1 : teamActionRandom < 10 ? 2 : 3);
        else if (matchStepDto.getSecondTeamChance() < 40)
            matchStepDto.setSecondTeamAction(teamActionRandom > 80 ? 1 : teamActionRandom < 35 ? 2 : 3);
        else
            matchStepDto.setSecondTeamAction(teamActionRandom > 65 ? 1 : teamActionRandom < 30 ? 2 : 3);

        matchStepDto.increaseStep();
        String stepLog = matchStepDto.getStep() +" м: ";

        if (matchStepDto.getPosition() == 1) {
            matchStepDto.setFirstPlayer(getRandomPlayerByType(matchStepDto.getMatchDto().getFirstTeam().getPlayers(), PlayerType.CD));
            matchStepDto.setSecondPlayer(getRandomPlayerByType(matchStepDto.getMatchDto().getSecondTeam().getPlayers(), PlayerType.ST));
            if (matchStepDto.isFirstTeamBall()) {
                addition = matchStepDto.getSecondTeamAction() == action ? 15 : 0;
                if (action == 1) {
                    matchStepDto.setPosition(2);
                    if (Math.random() * matchStepDto.getFirstPlayer().getSkill()
                            > Math.random() * matchStepDto.getSecondPlayer().getSkill() * (matchStepDto.getSecondTeamChance() + addition) / 100) {
                        stepLog += matchStepDto.getFirstPlayer().getName() + " выбивает мяч";
                    } else {
                        matchStepDto.setFirstTeamBall(false);
                        stepLog += matchStepDto.getSecondPlayer().getName() + " забирает мяч";
                    }
                } else if (action == 2 || action == 3) {
                    matchStepDto.plusChance(1);
                    if (Math.random() * 100 < Math.random() * matchStepDto.getFirstTeamChance()) {
                        matchStepDto.setPosition(2);
                        stepLog += matchStepDto.getFirstPlayer().getName() + " выбивает мяч";
                    } else {
                        stepLog += matchStepDto.getFirstPlayer().getName() + " держит мяч";
                    }
                }
            } else {
                addition = matchStepDto.getSecondTeamAction() == action ? 50 : 0;
                if (matchStepDto.getSecondTeamAction() == 1) {
                    matchStepDto.setFirstPlayer(getRandomPlayerByType(matchStepDto.getMatchDto().getFirstTeam().getPlayers(), PlayerType.GK));
                    attackFactor = Math.random() * matchStepDto.getSecondPlayer().getSkill() * matchStepDto.getSecondTeamChance() / 100
                            - Math.random() * matchStepDto.getFirstPlayer().getSkill() * (matchStepDto.getFirstTeamChance() + addition) / 100;
                    if (attackFactor > 0) {
                        matchStepDto.setPosition(2);
                        matchStepDto.setFirstTeamBall(true);
                        matchStepDto.increaseGoal(2);
                        stepLog += matchStepDto.getSecondPlayer().getName() + attackLog(attackFactor, addition) + matchStepDto.showGoals();
                    } else {
                        matchStepDto.setFirstTeamBall(true);
                        stepLog += matchStepDto.getFirstPlayer().getName() + attackLog(attackFactor, addition);
                    }
                } else if (matchStepDto.getSecondTeamAction() == 2) {
                    if (Math.random() * matchStepDto.getSecondPlayer().getSkill() * matchStepDto.getSecondTeamChance() / 100
                            > Math.random() * matchStepDto.getFirstPlayer().getSkill() * matchStepDto.getFirstTeamChance() / 100) {
                        matchStepDto.plusChance(2);
                        stepLog += matchStepDto.getSecondPlayer().getName() + " обводит " + matchStepDto.getFirstPlayer().getName();
                    } else {
                        matchStepDto.setFirstTeamBall(true);
                        stepLog += matchStepDto.getSecondPlayer().getName() + " теряет мяч";
                    }
                } else if (matchStepDto.getSecondTeamAction() == 3) {
                    matchStepDto.setPosition(2);
                    matchStepDto.plusChance(2);
                    matchStepDto.minusChance(1);
                    stepLog += matchStepDto.getSecondPlayer().getName() + " делает пас назад";
                }
            }
        } else if (matchStepDto.getPosition() == 2) {

            matchStepDto.setFirstPlayer(getRandomPlayerByType(matchStepDto.getMatchDto().getFirstTeam().getPlayers(), PlayerType.MD));
            matchStepDto.setSecondPlayer(getRandomPlayerByType(matchStepDto.getMatchDto().getSecondTeam().getPlayers(), PlayerType.MD));
            addition = matchStepDto.getSecondTeamAction() == action ? 25 : 0;
            if (matchStepDto.isFirstTeamBall()) {
                if (action == 1) {
                    if (Math.random() * matchStepDto.getFirstPlayer().getSpeed()
                            > Math.random() * matchStepDto.getSecondPlayer().getSpeed() * (matchStepDto.getSecondTeamChance() + addition) / 100) {
                        matchStepDto.setPosition(3);
                        matchStepDto.minusChance(1);
                        stepLog += matchStepDto.getFirstPlayer().getName() + " проходит вперед";
                    } else {
                        matchStepDto.setFirstTeamBall(false);
                        stepLog += matchStepDto.getFirstPlayer().getName() + " теряет мяч";
                    }
                } else if (action == 2) {
                    if (Math.random() * matchStepDto.getFirstPlayer().getSkill()
                            > Math.random() * matchStepDto.getSecondPlayer().getSkill() * matchStepDto.getSecondTeamChance() / 100) {
                        matchStepDto.plusChance(1);
                        stepLog += matchStepDto.getFirstPlayer().getName() + " укрепляет позицию";
                    } else {
                        matchStepDto.setFirstTeamBall(false);
                        stepLog += matchStepDto.getFirstPlayer().getName() + " теряет мяч";
                    }
                } else if (action == 3) {
                    matchStepDto.setPosition(1);
                    matchStepDto.plusChance(1);
                    matchStepDto.minusChance(2);
                    stepLog += matchStepDto.getFirstPlayer().getName() + " делает пас назад";
                }
            } else {
                if (matchStepDto.getSecondTeamAction() == 1) {
                    if (Math.random() * matchStepDto.getSecondPlayer().getSpeed()
                            > Math.random() * matchStepDto.getFirstPlayer().getSpeed() * (matchStepDto.getFirstTeamChance() + addition) / 100) {
                        matchStepDto.setPosition(1);
                        matchStepDto.minusChance(2);
                        stepLog += matchStepDto.getSecondPlayer().getName() + " проходит вперед";
                    } else {
                        matchStepDto.setFirstTeamBall(true);
                        stepLog += matchStepDto.getSecondPlayer().getName() + " теряет мяч";
                    }
                } else if (matchStepDto.getSecondTeamAction() == 2) {
                    if (Math.random() * matchStepDto.getSecondPlayer().getSkill()
                            > Math.random() * matchStepDto.getFirstPlayer().getSkill() * matchStepDto.getFirstTeamChance() / 100) {
                        matchStepDto.plusChance(2);
                        stepLog += matchStepDto.getSecondPlayer().getName() + " укрепляет позицию";
                    } else {
                        matchStepDto.setFirstTeamBall(true);
                        stepLog += matchStepDto.getSecondPlayer().getName() + " теряет мяч";
                    }
                } else if (matchStepDto.getSecondTeamAction() == 3) {
                    matchStepDto.setPosition(3);
                    matchStepDto.plusChance(2);
                    matchStepDto.minusChance(1);
                    stepLog += matchStepDto.getSecondPlayer().getName() + " делает пас назад";
                }
            }
        } else if (matchStepDto.getPosition() == 3) {

            matchStepDto.setFirstPlayer(getRandomPlayerByType(matchStepDto.getMatchDto().getFirstTeam().getPlayers(), PlayerType.ST));
            matchStepDto.setSecondPlayer(getRandomPlayerByType(matchStepDto.getMatchDto().getSecondTeam().getPlayers(), PlayerType.CD));
            if (!matchStepDto.isFirstTeamBall()) {
                addition = matchStepDto.getSecondTeamAction() == action ? 15 : 0;
                if (matchStepDto.getSecondTeamAction() == 1) {
                    matchStepDto.setPosition(2);
                    if (Math.random() * matchStepDto.getSecondPlayer().getSkill()
                            > Math.random() * matchStepDto.getFirstPlayer().getSkill() * (matchStepDto.getFirstTeamChance() + addition) / 100) {
                        stepLog += matchStepDto.getSecondPlayer().getName() + " выбивает мяч";
                    } else {
                        matchStepDto.setFirstTeamBall(true);
                        stepLog += matchStepDto.getFirstPlayer().getName() + " забирает мяч";
                    }
                } else if (matchStepDto.getSecondTeamAction() == 2 || matchStepDto.getSecondTeamAction() == 3) {
                    matchStepDto.plusChance(2);
                    if (Math.random() * 100 < Math.random() * matchStepDto.getSecondTeamChance()) {
                        matchStepDto.setPosition(2);
                        stepLog += matchStepDto.getSecondPlayer().getName() + " выбивает мяч";
                    } else {
                        stepLog += matchStepDto.getSecondPlayer().getName() + " держит мяч";
                    }
                }
            } else {
                addition = matchStepDto.getSecondTeamAction() == action ? 50 : 0;
                if (action == 1) {
                    matchStepDto.setSecondPlayer(getRandomPlayerByType(matchStepDto.getMatchDto().getSecondTeam().getPlayers(), PlayerType.GK));
                    attackFactor = Math.random() * matchStepDto.getFirstPlayer().getSkill() * matchStepDto.getFirstTeamChance() / 100
                            - Math.random() * matchStepDto.getSecondPlayer().getSkill() * (matchStepDto.getSecondTeamChance() + addition) / 100;
                    if (attackFactor > 0) {
                        matchStepDto.setPosition(2);
                        matchStepDto.setFirstTeamBall(false);
                        matchStepDto.increaseGoal(1);
                        stepLog += matchStepDto.getFirstPlayer().getName() + attackLog(attackFactor, addition) + matchStepDto.showGoals();
                    } else {
                        matchStepDto.setFirstTeamBall(false);
                        stepLog += matchStepDto.getSecondPlayer().getName() + attackLog(attackFactor, addition);
                    }
                } else if (action == 2) {
                    if (Math.random() * matchStepDto.getFirstPlayer().getSkill() * matchStepDto.getFirstTeamChance() / 100
                            > Math.random() * matchStepDto.getSecondPlayer().getSkill() * matchStepDto.getSecondTeamChance() / 100) {
                        matchStepDto.plusChance(1);
                        stepLog += matchStepDto.getFirstPlayer() + " обводит " + matchStepDto.getSecondPlayer().getName();
                    } else {
                        matchStepDto.setFirstTeamBall(false);
                        stepLog += matchStepDto.getFirstPlayer().getName() + " теряет мяч";
                    }
                } else if (action == 3) {
                    matchStepDto.setPosition(2);
                    matchStepDto.plusChance(1);
                    matchStepDto.minusChance(2);
                    stepLog += matchStepDto.getFirstPlayer().getName() + " делает пас назад";
                }
            }
        }

        matchStepDto.setLastStepLog(stepLog);
        matchStepDto.getLog().add(matchStepDto.getLastStepLog());
        return matchStepDto;
    }

    /**
     * play soft match between two teams
     * @param team1
     * @param team2
     * @return
     */
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

    private MatchStepDto getMatchStepDtoById(Long id) {
        return matchStepDtos.stream().filter(m -> m.getMatchDto().getMatchId().equals(id)).findAny().orElse(null);
    }

    private String attackLog(double factor, int addition) {
        if (addition > 0) {
            if (factor > 20) {
                return " невероятно поклал мяч в девятку из далека!!!!";
            } else if (factor > 10) {
                return " обыграл вратаря и забил гол!!!";
            } else if (factor > 0) {
                return " гол из далека!";
            } else if (factor < 20) {
                return " с легкостью берет мяч в руки";
            } else if (factor < 10) {
                return " смотрит как мяч пролетает далеко мимо ворот";
            } else if (factor <= 0) {
                return " реагирует и забирает мяч";
            }
        } else {
            if (factor > 40) {
                return " не оставил шанса вратврю!!!!";
            } else if (factor > 20) {
                return " запутал вратаря и точно пробил по воротам!!";
            } else if (factor > 0) {
                return " забивает гол!";
            } else if (factor < 40) {
                return " выбрал правильную позицию и забрал мяч из под ног";
            } else if (factor < 20) {
                return " не дотягивается до меча и ... штанга";
            } else if (factor <= 0) {
                return " делает невероятный сейв!";
            }
        }
        return "";
    }
}
