package site.fifa.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.fifa.dto.*;
import site.fifa.dto.mappers.StatisticDtoRowMapper;
import site.fifa.entity.LeagueTableItem;
import site.fifa.entity.Player;
import site.fifa.entity.PlayerType;
import site.fifa.entity.Statistic;
import site.fifa.entity.match.GoalsInMatch;
import site.fifa.entity.match.MatchPlay;
import site.fifa.entity.match.MatchStatus;
import site.fifa.entity.match.MatchType;
import site.fifa.repository.GoalsInMatchRepository;
import site.fifa.repository.LeagueTableItemRepository;
import site.fifa.repository.MatchRepository;

import javax.annotation.PostConstruct;
import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MatchService {

    @Autowired
    private TeamService teamService;
    @Autowired
    private MatchRepository matchRepository;
    @Autowired
    private StatisticService statisticService;
    @Autowired
    private LeagueTableItemRepository leagueTableItemRepository;
    @Autowired
    private GoalsInMatchRepository goalsInMatchRepository;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static ArrayList<MatchStepDto> matchStepDtos = new ArrayList<>();

    @Transactional
    public MatchDto startMatchWithPC(Long firstTeamId, Long secondTeamId) {
        MatchPlay match = matchRepository.getByFirstTeamIdAndSecondTeamIdAndStatus(firstTeamId, secondTeamId, MatchStatus.CREATED)
                .stream().findAny().orElse(null);
        if (match == null) {
            match = matchRepository.save(new MatchPlay(MatchStatus.STARTED, MatchType.FRIENDLY, LocalDate.now(), firstTeamId, secondTeamId));
        } else {
            matchRepository.updateMatchStatusById(MatchStatus.STARTED, match.getId());
        }

        MatchStepDto matchStepDto = new MatchStepDto();
        matchStepDto.setMatchDto(new MatchDto(match.getId(), PlaySide.CPU,  LocalDate.now(), teamService.getTeamById(firstTeamId), teamService.getTeamById(secondTeamId)));
        matchStepDto.setFirstPlayer(getRandomPlayerByType(matchStepDto.getMatchDto().getFirstTeam().getPlayers(), PlayerType.MD));
        matchStepDto.setSecondPlayer(getRandomPlayerByType(matchStepDto.getMatchDto().getSecondTeam().getPlayers(), PlayerType.MD));
        matchStepDtos.add(matchStepDto);
        return matchStepDto.getMatchDto();
    }

    @Transactional
    public MatchStepDto makeStepWithCPU(Long matchId, int action) {

        MatchStepDto matchStepDto = getMatchStepDtoById(matchId);
        matchStepDto.increaseStep();
        matchStepDto.getStatisticDto().countPercentageByTeamHold(matchStepDto.isFirstTeamBall());
        String stepLog = matchStepDto.getStep() + " м: ";
        double attackFactor;
        boolean startPointBall = false;

        if (matchStepDto.getStep() > 90 && matchStepDto.getAdditionTime() < 0) {
            if (!statisticService.isExistByMatchId(matchId)) {
                matchStepDto.setAdditionTime(-2);
                matchStepDto.setLastStepLog(matchStepDto.showGoals());
                matchStepDto.getLog().add("Матч окончен!" + matchStepDto.getLastStepLog());
                statisticService.saveStatistic(new Statistic(matchId, matchStepDto.getStatisticDto()));
                matchRepository.updateMatchStatusById(MatchStatus.FINISHED, matchId);
                updateLeagueTable(matchStepDto);
            }
            return matchStepDto;
        }

        if (matchStepDto.getAdditionTime() >= 0) {
            matchStepDto.decreaseAdditionTime();
            if (matchStepDto.getAdditionTime() < 0 && matchStepDto.getStep() < 90) {
                matchStepDto.setStep(45);
                matchStepDto.setAdditionTime(100);
            }
        }


        if (matchStepDto.getStep() == 45 || matchStepDto.getStep() == 90) {
            if (matchStepDto.getAdditionTime() < 0) {
                matchStepDto.generateAdditionTime();
                stepLog += "Добавлено " + matchStepDto.getAdditionTime() + " м ";
            } else if (matchStepDto.getAdditionTime() == 100) {
                matchStepDto.setAdditionTime(-1);
                matchStepDto.setFirstTeamBall(false);
                matchStepDto.setPosition(2);
                matchStepDto.setLastStepLog("Второй тайм!");
                matchStepDto.getLog().add(matchStepDto.getLastStepLog());
                updateBallCoordinate(matchStepDto, true);
                return matchStepDto;
            }
        }

        int addition;

        if (action < 1 || action > 3) {
            action = randomizeActionByTeamChance(matchStepDto.getFirstTeamChance());
        }
        matchStepDto.setSecondTeamAction(randomizeActionByTeamChance(matchStepDto.getFirstTeamChance()));

        if (matchStepDto.getPosition() == 1) {

            if (matchStepDto.isFirstTeamBall()) {
                addition = matchStepDto.getSecondTeamAction() == action ? 15 : 0;
                if (action == 1) {
                    matchStepDto.setPosition(2);
                    if (Math.random() * matchStepDto.getFirstPlayer().getSkill()
                            > Math.random() * matchStepDto.getSecondPlayer().getSkill() * (matchStepDto.getSecondTeamChance() + addition) / 100) {
                        stepLog += matchStepDto.getFirstPlayer().getName() + " выбивает мяч";
                        matchStepDto.setFirstPlayer(getRandomPlayerByType(matchStepDto.getMatchDto().getFirstTeam().getPlayers(), PlayerType.MD));
                        matchStepDto.setSecondPlayer(getRandomPlayerByType(matchStepDto.getMatchDto().getSecondTeam().getPlayers(), PlayerType.MD));
                    } else {
                        matchStepDto.setFirstTeamBall(false);
                        matchStepDto.setSecondPlayer(getRandomPlayerByType(matchStepDto.getMatchDto().getSecondTeam().getPlayers(), PlayerType.MD));
                        stepLog += matchStepDto.getSecondPlayer().getName() + " забирает мяч";
                    }
                } else if (action == 2 || action == 3) {
                    matchStepDto.plusChance(1);
                    if (Math.random() * 100 < Math.random() * matchStepDto.getFirstTeamChance()) {
                        matchStepDto.setPosition(2);
                        stepLog += matchStepDto.getFirstPlayer().getName() + " выбивает мяч";
                        matchStepDto.setFirstPlayer(getRandomPlayerByType(matchStepDto.getMatchDto().getFirstTeam().getPlayers(), PlayerType.MD));
                        matchStepDto.setSecondPlayer(getRandomPlayerByType(matchStepDto.getMatchDto().getSecondTeam().getPlayers(), PlayerType.MD));
                    } else {
                        stepLog += matchStepDto.getFirstPlayer().getName() + " держит мяч";
                        matchStepDto.setFirstPlayer(getRandomPlayerByType(matchStepDto.getMatchDto().getFirstTeam().getPlayers(), PlayerType.CD));
                        matchStepDto.setSecondPlayer(getRandomPlayerByType(matchStepDto.getMatchDto().getSecondTeam().getPlayers(), PlayerType.ST));
                    }
                }
            } else {
                addition = matchStepDto.getSecondTeamAction() == action ? 50 : 0;
                if (matchStepDto.getSecondTeamAction() == 1) {
                    matchStepDto.getStatisticDto().getGoalKick().y++;
                    matchStepDto.setFirstPlayer(getRandomPlayerByType(matchStepDto.getMatchDto().getFirstTeam().getPlayers(), PlayerType.GK));
                    attackFactor = Math.random() * matchStepDto.getSecondPlayer().getSkill() * matchStepDto.getSecondTeamChance() / 100
                            - Math.random() * matchStepDto.getFirstPlayer().getSkill() * (matchStepDto.getFirstTeamChance() + addition) / 100;
                    if (attackFactor > 0) {
                        matchStepDto.setPosition(2);
                        matchStepDto.setFirstTeamBall(true);
                        matchStepDto.increaseGoal(2);
                        stepLog += matchStepDto.getSecondPlayer().getName() + attackLog(attackFactor, addition) + matchStepDto.showGoals();
                        matchStepDto.getStatisticDto().getGoals().y++;
                        startPointBall = true;
                        // save goal result
                        GoalsInMatch goalsInMatch = goalsInMatchRepository.save(new GoalsInMatch(
                                null, matchStepDto.getMatchDto().getMatchId(), matchStepDto.getMatchDto().getSecondTeam().getTeam(), matchStepDto.getSecondPlayer(),
                                matchStepDto.getStep()
                        ));
                        matchStepDto.getStatisticDto().getGoalsList().add(goalsInMatch);

                        matchStepDto.setFirstPlayer(getRandomPlayerByType(matchStepDto.getMatchDto().getFirstTeam().getPlayers(), PlayerType.MD));
                        matchStepDto.setSecondPlayer(getRandomPlayerByType(matchStepDto.getMatchDto().getSecondTeam().getPlayers(), PlayerType.MD));
                    } else {
                        matchStepDto.setFirstTeamBall(true);
                        stepLog += matchStepDto.getFirstPlayer().getName() + attackLog(attackFactor, addition);
                    }
                } else if (matchStepDto.getSecondTeamAction() == 2) {
                    if (Math.random() * matchStepDto.getSecondPlayer().getSkill() * matchStepDto.getSecondTeamChance() / 100
                            > Math.random() * matchStepDto.getFirstPlayer().getSkill() * matchStepDto.getFirstTeamChance() / 100) {
                        matchStepDto.plusChance(2);
                        stepLog += matchStepDto.getSecondPlayer().getName() + " обводит " + matchStepDto.getFirstPlayer().getName();
                        matchStepDto.setFirstPlayer(getRandomPlayerByType(matchStepDto.getMatchDto().getFirstTeam().getPlayers(), PlayerType.CD));
                    } else {
                        matchStepDto.setFirstTeamBall(true);
                        stepLog += matchStepDto.getSecondPlayer().getName() + " теряет мяч";
                        matchStepDto.setSecondPlayer(getRandomPlayerByType(matchStepDto.getMatchDto().getSecondTeam().getPlayers(), PlayerType.ST));
                    }
                } else if (matchStepDto.getSecondTeamAction() == 3) {
                    matchStepDto.setPosition(2);
                    matchStepDto.plusChance(2);
                    matchStepDto.minusChance(1);
                    stepLog += matchStepDto.getSecondPlayer().getName() + " делает пас назад";
                    matchStepDto.setFirstPlayer(getRandomPlayerByType(matchStepDto.getMatchDto().getFirstTeam().getPlayers(), PlayerType.MD));
                    matchStepDto.setSecondPlayer(getRandomPlayerByType(matchStepDto.getMatchDto().getSecondTeam().getPlayers(), PlayerType.MD));
                }
            }
        } else if (matchStepDto.getPosition() == 2) {

            addition = matchStepDto.getSecondTeamAction() == action ? 25 : 0;
            if (matchStepDto.isFirstTeamBall()) {
                if (action == 1) {
                    if (Math.random() * matchStepDto.getFirstPlayer().getSpeed()
                            > Math.random() * matchStepDto.getSecondPlayer().getSpeed() * (matchStepDto.getSecondTeamChance() + addition) / 100) {
                        matchStepDto.setPosition(3);
                        matchStepDto.minusChance(1);
                        stepLog += matchStepDto.getFirstPlayer().getName() + " передает пас вперед вперед";
                        matchStepDto.setFirstPlayer(getRandomPlayerByType(matchStepDto.getMatchDto().getFirstTeam().getPlayers(), PlayerType.ST));
                        matchStepDto.setSecondPlayer(getRandomPlayerByType(matchStepDto.getMatchDto().getSecondTeam().getPlayers(), PlayerType.CD));
                    } else {
                        matchStepDto.setFirstTeamBall(false);
                        stepLog += matchStepDto.getFirstPlayer().getName() + " теряет мяч";
                    }
                } else if (action == 2) {
                    if (Math.random() * matchStepDto.getFirstPlayer().getSkill()
                            > Math.random() * matchStepDto.getSecondPlayer().getSkill() * matchStepDto.getSecondTeamChance() / 100) {
                        matchStepDto.plusChance(1);
                        stepLog += matchStepDto.getFirstPlayer().getName() + " укрепляет позицию";
                        matchStepDto.setFirstPlayer(getRandomPlayerByType(matchStepDto.getMatchDto().getFirstTeam().getPlayers(), PlayerType.MD));
                        matchStepDto.setSecondPlayer(getRandomPlayerByType(matchStepDto.getMatchDto().getSecondTeam().getPlayers(), PlayerType.MD));
                    } else {
                        matchStepDto.setFirstTeamBall(false);
                        stepLog += matchStepDto.getFirstPlayer().getName() + " теряет мяч";
                        matchStepDto.setFirstPlayer(getRandomPlayerByType(matchStepDto.getMatchDto().getFirstTeam().getPlayers(), PlayerType.MD));
                    }
                } else if (action == 3) {
                    matchStepDto.setPosition(1);
                    matchStepDto.plusChance(1);
                    matchStepDto.minusChance(2);
                    stepLog += matchStepDto.getFirstPlayer().getName() + " делает пас назад";
                    matchStepDto.setFirstPlayer(getRandomPlayerByType(matchStepDto.getMatchDto().getFirstTeam().getPlayers(), PlayerType.CD));
                    matchStepDto.setSecondPlayer(getRandomPlayerByType(matchStepDto.getMatchDto().getSecondTeam().getPlayers(), PlayerType.ST));
                }
            } else {
                if (matchStepDto.getSecondTeamAction() == 1) {
                    if (Math.random() * matchStepDto.getSecondPlayer().getSpeed()
                            > Math.random() * matchStepDto.getFirstPlayer().getSpeed() * (matchStepDto.getFirstTeamChance() + addition) / 100) {
                        matchStepDto.setPosition(1);
                        matchStepDto.minusChance(2);
                        stepLog += matchStepDto.getSecondPlayer().getName() + " передает мяч вперед";
                        matchStepDto.setFirstPlayer(getRandomPlayerByType(matchStepDto.getMatchDto().getFirstTeam().getPlayers(), PlayerType.CD));
                        matchStepDto.setSecondPlayer(getRandomPlayerByType(matchStepDto.getMatchDto().getSecondTeam().getPlayers(), PlayerType.ST));
                    } else {
                        matchStepDto.setFirstTeamBall(true);
                        stepLog += matchStepDto.getSecondPlayer().getName() + " теряет мяч";
                        matchStepDto.setSecondPlayer(getRandomPlayerByType(matchStepDto.getMatchDto().getSecondTeam().getPlayers(), PlayerType.MD));
                    }
                } else if (matchStepDto.getSecondTeamAction() == 2) {
                    if (Math.random() * matchStepDto.getSecondPlayer().getSkill()
                            > Math.random() * matchStepDto.getFirstPlayer().getSkill() * matchStepDto.getFirstTeamChance() / 100) {
                        matchStepDto.plusChance(2);
                        stepLog += matchStepDto.getSecondPlayer().getName() + " укрепляет позицию";
                        matchStepDto.setFirstPlayer(getRandomPlayerByType(matchStepDto.getMatchDto().getFirstTeam().getPlayers(), PlayerType.MD));
                        matchStepDto.setSecondPlayer(getRandomPlayerByType(matchStepDto.getMatchDto().getSecondTeam().getPlayers(), PlayerType.MD));
                    } else {
                        matchStepDto.setFirstTeamBall(true);
                        stepLog += matchStepDto.getSecondPlayer().getName() + " теряет мяч";
                        matchStepDto.setFirstPlayer(getRandomPlayerByType(matchStepDto.getMatchDto().getFirstTeam().getPlayers(), PlayerType.MD));
                    }
                } else if (matchStepDto.getSecondTeamAction() == 3) {
                    matchStepDto.setPosition(3);
                    matchStepDto.plusChance(2);
                    matchStepDto.minusChance(1);
                    stepLog += matchStepDto.getSecondPlayer().getName() + " делает пас назад";
                    matchStepDto.setFirstPlayer(getRandomPlayerByType(matchStepDto.getMatchDto().getFirstTeam().getPlayers(), PlayerType.ST));
                    matchStepDto.setSecondPlayer(getRandomPlayerByType(matchStepDto.getMatchDto().getSecondTeam().getPlayers(), PlayerType.CD));
                }
            }
        } else if (matchStepDto.getPosition() == 3) {

            if (!matchStepDto.isFirstTeamBall()) {
                addition = matchStepDto.getSecondTeamAction() == action ? 15 : 0;
                if (matchStepDto.getSecondTeamAction() == 1) {
                    matchStepDto.setPosition(2);
                    if (Math.random() * matchStepDto.getSecondPlayer().getSkill()
                            > Math.random() * matchStepDto.getFirstPlayer().getSkill() * (matchStepDto.getFirstTeamChance() + addition) / 100) {
                        stepLog += matchStepDto.getSecondPlayer().getName() + " выбивает мяч";
                        matchStepDto.setFirstPlayer(getRandomPlayerByType(matchStepDto.getMatchDto().getFirstTeam().getPlayers(), PlayerType.MD));
                        matchStepDto.setSecondPlayer(getRandomPlayerByType(matchStepDto.getMatchDto().getSecondTeam().getPlayers(), PlayerType.MD));
                    } else {
                        matchStepDto.setFirstTeamBall(true);
                        stepLog += matchStepDto.getFirstPlayer().getName() + " забирает мяч";
                        matchStepDto.setSecondPlayer(getRandomPlayerByType(matchStepDto.getMatchDto().getSecondTeam().getPlayers(), PlayerType.CD));
                    }
                } else if (matchStepDto.getSecondTeamAction() == 2 || matchStepDto.getSecondTeamAction() == 3) {
                    matchStepDto.plusChance(2);
                    if (Math.random() * 100 < Math.random() * matchStepDto.getSecondTeamChance()) {
                        matchStepDto.setPosition(2);
                        stepLog += matchStepDto.getSecondPlayer().getName() + " выбивает мяч";
                        matchStepDto.setFirstPlayer(getRandomPlayerByType(matchStepDto.getMatchDto().getFirstTeam().getPlayers(), PlayerType.MD));
                        matchStepDto.setSecondPlayer(getRandomPlayerByType(matchStepDto.getMatchDto().getSecondTeam().getPlayers(), PlayerType.MD));
                    } else {
                        stepLog += matchStepDto.getSecondPlayer().getName() + " держит мяч";
                        matchStepDto.setFirstPlayer(getRandomPlayerByType(matchStepDto.getMatchDto().getFirstTeam().getPlayers(), PlayerType.ST));
                        matchStepDto.setSecondPlayer(getRandomPlayerByType(matchStepDto.getMatchDto().getSecondTeam().getPlayers(), PlayerType.CD));
                    }
                }
            } else {
                addition = matchStepDto.getSecondTeamAction() == action ? 50 : 0;
                if (action == 1) {
                    matchStepDto.getStatisticDto().getGoalKick().x++;
                    matchStepDto.setSecondPlayer(getRandomPlayerByType(matchStepDto.getMatchDto().getSecondTeam().getPlayers(), PlayerType.GK));
                    attackFactor = Math.random() * matchStepDto.getFirstPlayer().getSkill() * matchStepDto.getFirstTeamChance() / 100
                            - Math.random() * matchStepDto.getSecondPlayer().getSkill() * (matchStepDto.getSecondTeamChance() + addition) / 100;
                    if (attackFactor > 0) {
                        matchStepDto.setPosition(2);
                        matchStepDto.setFirstTeamBall(false);
                        matchStepDto.increaseGoal(1);
                        stepLog += matchStepDto.getFirstPlayer().getName() + attackLog(attackFactor, addition) + matchStepDto.showGoals();
                        matchStepDto.getStatisticDto().getGoals().x++;
                        startPointBall = true;
                        // save goal result
                        GoalsInMatch goalsInMatch = goalsInMatchRepository.save(new GoalsInMatch(
                                null, matchStepDto.getMatchDto().getMatchId(), matchStepDto.getMatchDto().getFirstTeam().getTeam(), matchStepDto.getFirstPlayer(),
                                matchStepDto.getStep()
                        ));
                        matchStepDto.getStatisticDto().getGoalsList().add(goalsInMatch);
                        matchStepDto.setFirstPlayer(getRandomPlayerByType(matchStepDto.getMatchDto().getFirstTeam().getPlayers(), PlayerType.MD));
                        matchStepDto.setSecondPlayer(getRandomPlayerByType(matchStepDto.getMatchDto().getSecondTeam().getPlayers(), PlayerType.MD));
                    } else {
                        matchStepDto.setFirstTeamBall(false);
                        stepLog += matchStepDto.getSecondPlayer().getName() + attackLog(attackFactor, addition);
                    }
                } else if (action == 2) {
                    if (Math.random() * matchStepDto.getFirstPlayer().getSkill() * matchStepDto.getFirstTeamChance() / 100
                            > Math.random() * matchStepDto.getSecondPlayer().getSkill() * matchStepDto.getSecondTeamChance() / 100) {
                        matchStepDto.plusChance(1);
                        stepLog += matchStepDto.getFirstPlayer().getName() + " обводит " + matchStepDto.getSecondPlayer().getName();
                        matchStepDto.setSecondPlayer(getRandomPlayerByType(matchStepDto.getMatchDto().getSecondTeam().getPlayers(), PlayerType.CD));
                    } else {
                        matchStepDto.setFirstTeamBall(false);
                        stepLog += matchStepDto.getFirstPlayer().getName() + " теряет мяч";
                        matchStepDto.setFirstPlayer(getRandomPlayerByType(matchStepDto.getMatchDto().getFirstTeam().getPlayers(), PlayerType.ST));
                    }
                } else if (action == 3) {
                    matchStepDto.setPosition(2);
                    matchStepDto.plusChance(1);
                    matchStepDto.minusChance(2);
                    stepLog += matchStepDto.getFirstPlayer().getName() + " делает пас назад";
                    matchStepDto.setFirstPlayer(getRandomPlayerByType(matchStepDto.getMatchDto().getFirstTeam().getPlayers(), PlayerType.MD));
                    matchStepDto.setSecondPlayer(getRandomPlayerByType(matchStepDto.getMatchDto().getSecondTeam().getPlayers(), PlayerType.MD));
                }
            }
        }

        updateBallCoordinate(matchStepDto, startPointBall);
        matchStepDto.setLastStepLog(stepLog);
        matchStepDto.getLog().add(matchStepDto.getLastStepLog());
        return matchStepDto;
    }

    /**
     * play soft match between two teams
     *
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

        for (int i = 0; i <= 90; i++) {
            matchLog.append(i).append(" m: ").append(position).append(step.isFirstTeamBall()).append(" ");
            step.setFirstTeamAction((int) (Math.random() * 3) + 1);
            step.setSecondTeamAction((int) (Math.random() * 3) + 1);
            if (position == 1) {
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
                        if (Math.random() * step.getFirstPlayer().getSpeed() * step.getFirstTeamChance() / 100
                                > Math.random() * step.getSecondPlayer().getSpeed()) {
                            matchLog.append(step.getFirstPlayer().getName()).append(" успешно прошол вперед<br>");
                            position = 3;
                        } else {
                            matchLog.append(step.getFirstPlayer().getName()).append(" потерял мяч<br>");
                            step.setFirstTeamBall(false);
                        }
                    } else if (step.getFirstTeamAction() == 2) {
                        if (Math.random() * step.getFirstPlayer().getSkill() * step.getFirstTeamChance() / 100
                                > Math.random() * step.getSecondPlayer().getSkill() * step.getSecondTeamChance() / 100) {
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
                                > Math.random() * step.getFirstPlayer().getSpeed()) {
                            matchLog.append(step.getSecondPlayer().getName()).append(" успешно прошол вперед<br>");
                            position = 1;
                        } else {
                            matchLog.append(step.getSecondPlayer().getName()).append(" потерял мяч<br>");
                            step.setFirstTeamBall(true);
                        }
                    } else if (step.getSecondTeamAction() == 2) {
                        if (Math.random() * step.getSecondPlayer().getSkill() * step.getSecondTeamChance() / 100
                                > Math.random() * step.getFirstPlayer().getSkill() * step.getFirstTeamChance() / 100) {
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

    @PostConstruct
    @Scheduled(cron = "5 12 15 * * *")
    public void playAllCreatedMatchesAfterToday() {
        List<MatchPlay> matchPlayList = matchRepository.getAllFromPlayInLeague(LocalDate.now());
        MatchDto matchDto;
        MatchStepDto matchStepDto;
        // clear all matches in server
        matchStepDtos.clear();
        matchRepository.resetAllMatches();

        for (MatchPlay matchPlay : matchPlayList) {
            matchDto = startMatchWithPC(matchPlay.getFirstTeamId(), matchPlay.getSecondTeamId());
            matchStepDto = makeStepWithCPU(matchDto.getMatchId(), randomizeActionByTeamChance(50));
            while (!matchStepDto.getLastStepLog().equals(matchStepDto.showGoals())) {
                matchStepDto = makeStepWithCPU(matchDto.getMatchId(), randomizeActionByTeamChance(matchStepDto.getFirstTeamChance()));
            }
            System.out.println(matchStepDto.showGoals());
        }
    }

    public List<MatchDto> getMatchesForPlayLeaguesGame() {
        List<MatchDto> result = new ArrayList<>();
        List<MatchPlay> matchPlays = matchRepository.getForLeaguePlay();
        for (MatchPlay m : matchPlays) {
            result.add(new MatchDto(m.getId(), PlaySide.CPU, m.getStarted(), teamService.getTeamById(m.getFirstTeamId()), teamService.getTeamById(m.getSecondTeamId())));
        }
        return result;
    }

    public List<StatisticDto> getLastLeagueMatches(Long amount) {
        List<StatisticDto> result = new ArrayList<>();

        String sql = "select s.*, ft.name as 'ft', st.name as 'st'  from match_play mp " +
                "inner join team ft on ft.id=mp.first_team_id " +
                "inner join team st on st.id=mp.second_team_id " +
                "inner join statistic s on s.match_id = mp.id " +
                "where mp.type = 1 and mp.status = 2 order by mp.started desc limit ?";

        result = jdbcTemplate.query(sql, new Object[]{amount}, new StatisticDtoRowMapper());


        return result;
    }

    private Player getRandomPlayerByType(List<Player> players, PlayerType type) {
        List<Player> filtered = players.stream().filter(player -> player.getType() == type).collect(Collectors.toList());
        return filtered.get((int) (Math.random() * filtered.size()));
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

    private int randomizeActionByTeamChance(int teamChance) {
        double teamActionRandom = Math.random() * 100;

        // magic cpu randomise algorithm allowed by chance
        if (teamChance > 75)
            return teamActionRandom > 20 ? 1 : teamActionRandom < 10 ? 2 : 3;
        else if (teamChance < 40)
            return teamActionRandom > 80 ? 1 : teamActionRandom < 35 ? 2 : 3;
        else
            return teamActionRandom > 65 ? 1 : teamActionRandom < 30 ? 2 : 3;
    }

    private void updateLeagueTable(MatchStepDto matchStepDto) {
        MatchPlay matchPlay = matchRepository.findById(matchStepDto.getMatchDto().getMatchId()).orElse(null);
        if (matchPlay == null || matchPlay.getType() != MatchType.LEAGUE)
            return;
        int f = matchStepDto.getGoalFirstTeam();
        int s = matchStepDto.getGoalSecondTeam();
        leagueTableItemRepository.increaseDataForLeagueTable(f > s ? 1 : 0, f < s ? 1 : 0, f == s ? 1 : 0, f, s, f > s ? 3 : f == s ? 1 : 0,
                matchStepDto.getMatchDto().getFirstTeam().getTeam().getId(), matchStepDto.getMatchDto().getFirstTeam().getTeam().getLeagueId()
                );
        leagueTableItemRepository.increaseDataForLeagueTable(f < s ? 1 : 0, f > s ? 1 : 0, f == s ? 1 : 0, s, f, f > s ? 0 : f == s ? 1 : 3,
                matchStepDto.getMatchDto().getSecondTeam().getTeam().getId(), matchStepDto.getMatchDto().getFirstTeam().getTeam().getLeagueId()
        );

        List<LeagueTableItem> leagueTableItems = leagueTableItemRepository.getByLeagueId(matchStepDto.getMatchDto().getFirstTeam().getTeam().getLeagueId());
        leagueTableItems.sort(Comparator.comparingInt(LeagueTableItem::getPoint).reversed().thenComparing(l -> -l.getGoals()).thenComparing(LeagueTableItem::getGoalLose));
        int i = 0;
        for (LeagueTableItem l : leagueTableItems) {
            leagueTableItemRepository.updatePosition(++i, l.getId());
        }
    }

    private void updateBallCoordinate(MatchStepDto matchStepDto, boolean startPoint) {
        Point point = matchStepDto.getBallPosition();

        if (startPoint) {
            point.x = matchStepDto.isFirstTeamBall() ? 500 : 550;
            point.y = 270;
            return;
        }

        if (matchStepDto.isFirstTeamBall()) {

            switch (matchStepDto.getMatchDto().getFirstTeam().getPlayers().indexOf(matchStepDto.getFirstPlayer())) {
                case 0: // GK
                    point.x = 100;
                    point.y = 270;
                    break;
                case 1: // CD
                    point.x = 200;
                    point.y = 330;
                    break;
                case 2:
                    point.x = 200;
                    point.y = 450;
                    break;
                case 3:
                    point.x = 200;
                    point.y = 200;
                    break;
                case 4:
                    point.x = 200;
                    point.y = 60;
                    break;
                case 5: //MD
                    point.x = 500;
                    point.y = 270;
                    break;
                case 6: //st
                    point.x = 950;
                    point.y = 270;
                    break;
                case 7:
                    point.x = 500;
                    point.y = 120;
                    break;
                case 8:
                    point.x = 950;
                    point.y = 120;
                    break;
                case 9:
                    point.x = 500;
                    point.y = 420;
                    break;
                case 10:
                    point.x = 950;
                    point.y = 420;
                    break;
            }
        } else {
            switch (matchStepDto.getMatchDto().getSecondTeam().getPlayers().indexOf(matchStepDto.getSecondPlayer())) {
                case 0: // GK
                    point.x = 1050;
                    point.y = 270;
                    break;
                case 1: // CD
                    point.x = 950;
                    point.y = 330;
                    break;
                case 2:
                    point.x = 950;
                    point.y = 450;
                    break;
                case 3:
                    point.x = 950;
                    point.y = 200;
                    break;
                case 4:
                    point.x = 950;
                    point.y = 60;
                    break;
                case 5: //MD
                    point.x = 550;
                    point.y = 270;
                    break;
                case 6: //st
                    point.x = 200;
                    point.y = 270;
                    break;
                case 7:
                    point.x = 550;
                    point.y = 120;
                    break;
                case 8:
                    point.x = 200;
                    point.y = 120;
                    break;
                case 9:
                    point.x = 550;
                    point.y = 420;
                    break;
                case 10:
                    point.x = 200;
                    point.y = 420;
                    break;
            }
        }
    }
}
