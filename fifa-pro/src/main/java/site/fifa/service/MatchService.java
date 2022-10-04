package site.fifa.service;

import lombok.Synchronized;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.fifa.constants.GameConstants;
import site.fifa.dto.*;
import site.fifa.dto.mappers.StatisticDtoRowMapper;
import site.fifa.entity.*;
import site.fifa.entity.match.GoalsInMatch;
import site.fifa.entity.match.MatchPlay;
import site.fifa.entity.match.MatchStatus;
import site.fifa.entity.match.MatchType;
import site.fifa.entity.message.Message;
import site.fifa.entity.message.MessageTypeEnum;
import site.fifa.repository.*;

import javax.annotation.PostConstruct;
import javax.servlet.ServletRequest;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static site.fifa.constants.GameConstants.*;

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
    private PlayerRepository playerRepository;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private ServletRequest servletRequest;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private StadiumRepository stadiumRepository;
    @Autowired
    private MessageRepository messageRepository;

    private static ArrayList<MatchStepDto> matchStepDtos = new ArrayList<>();
    private static ArrayList<StatisticDto> lastMatches = new ArrayList<>();

    public MatchDto startQuickGame(Long firstTeamId, Long secondTeamId) {
        matchRepository.save(new MatchPlay(MatchStatus.STARTED, MatchType.FRIENDLY, LocalDate.now(), firstTeamId, secondTeamId));
        return startGame(firstTeamId, secondTeamId);
    }

    @Transactional
    public MatchDto startGame(Long firstTeamId, Long secondTeamId) {
        MatchPlay match = matchRepository.getLastByFirstTeamIdAndSecondTeamIdAndStatus(firstTeamId, secondTeamId, MatchStatus.STARTED);
        if (match != null) {
            MatchStepDto msd = getMatchStepDtoById(match.getId());
            if (msd != null) {
                return msd.getMatchDto();
            }
        }
        match = matchRepository.getLastByFirstTeamIdAndSecondTeamIdAndStatus(firstTeamId, secondTeamId, MatchStatus.CREATED);
        MatchStepDto matchStepDto = new MatchStepDto();
        if (match == null) {
            match = matchRepository.save(new MatchPlay(MatchStatus.STARTED, MatchType.FRIENDLY, LocalDate.now(), firstTeamId, secondTeamId));
        } else {
            matchRepository.updateMatchStatusById(MatchStatus.STARTED, match.getId());
            if (userRepository.findByTeamId(firstTeamId) != null && userRepository.findByTeamId(secondTeamId) != null) {
                matchStepDto.setTimeoutTime(LocalDateTime.now());
            }
        }

        matchStepDto.setMatchDto(new MatchDto(match.getId(), PlaySide.CPU, LocalDate.now(), teamService.getTeamById(firstTeamId), teamService.getTeamById(secondTeamId), match.getType()));
        matchStepDto.setFirstPlayer(getRandomPlayerByType(matchStepDto.getMatchDto().getFirstTeam().getPlayers(), PlayerType.MD));
        matchStepDto.setSecondPlayer(getRandomPlayerByType(matchStepDto.getMatchDto().getSecondTeam().getPlayers(), PlayerType.MD));
        matchStepDto.getStatisticDto().setFirstTeamName(matchStepDto.getMatchDto().getFirstTeam().getTeam().getName());
        matchStepDto.getStatisticDto().setSecondTeamName(matchStepDto.getMatchDto().getSecondTeam().getTeam().getName());
        matchStepDto.setStadium(matchStepDto.getMatchDto().getFirstTeam().getTeam().getStadium());
        calculatePopulation(matchStepDto);

        matchStepDtos.add(matchStepDto);
        return matchStepDto.getMatchDto();
    }

    @Synchronized
    @Transactional
    public MatchStepDto makeGameStep(Long matchId, PlaySide playSide, int action) {

        MatchStepDto matchStepDto = getMatchStepDtoById(matchId);
        // todo prevent incorrect db writing!!!
        if (matchStepDto.getStep() > 100) {
            System.out.println("system fail! check match play and statistic into db with mp.id=" + matchId);
            matchStepDto.setLastStepLog(matchStepDto.showGoals());
            return matchStepDto;
        }
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
                lastMatches.add(matchStepDto.getStatisticDto());
                statisticService.saveStatistic(new Statistic(matchId, matchStepDto.getStatisticDto(), matchStepDto.getFuns()));
                goalsInMatchRepository.saveAll(matchStepDto.getStatisticDto().getGoalsList());
                MatchPlay matchPlay = matchRepository.findById(matchId).orElse(new MatchPlay());
                matchPlay.setStatus(MatchStatus.FINISHED);
                matchPlay.setFuns(matchStepDto.getFuns());
                matchRepository.saveAndFlush(matchPlay);
                if (matchStepDto.getMatchDto().getMatchType() == MatchType.LEAGUE) {
                    updatePlayersExperience(matchStepDto);
                    updateLeagueTable(matchStepDto);
                }
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

        // calculate step action algorithm
        if (playSide == PlaySide.CPU) {
            matchStepDto.setSecondTeamAction(randomizeActionByTeamChance(matchStepDto.getSecondTeamChance()));
            action = randomizeActionByTeamChance(matchStepDto.getFirstTeamChance());
        } else if (matchStepDto.getTimeoutTime() == null || matchStepDto.getTimeoutTime().isBefore(LocalDateTime.now().minusMinutes(MATCH_WITH_PLAYER_TIMEOUT)))
        if (playSide == PlaySide.FiRST_TEAM) {
            matchStepDto.setSecondTeamAction(randomizeActionByTeamChance(matchStepDto.getSecondTeamChance()));
        } else {
            matchStepDto.setSecondTeamAction(action);
            action = randomizeActionByTeamChance(matchStepDto.getFirstTeamChance());
        } else {
            if (playSide == PlaySide.FiRST_TEAM) {
                if (matchStepDto.getSecondTeamAction() != 0) {
                    matchStepDto.setTimeoutTime(LocalDateTime.now());
                }
                matchStepDto.setFirstTeamAction(action);
            } else {
                if (matchStepDto.getFirstTeamAction() != 0) {
                    matchStepDto.setTimeoutTime(LocalDateTime.now());
                }
                matchStepDto.setSecondTeamAction(action);
            }
            if (matchStepDto.getFirstTeamAction() == 0 || matchStepDto.getSecondTeamAction() == 0) {
                stepLog += "Ожидаем противника. Осталось " + (MATCH_WITH_PLAYER_TIMEOUT * 60 - ChronoUnit.SECONDS.between(matchStepDto.getTimeoutTime(), LocalDateTime.now())) + "секунд";
                matchStepDto.setStep(matchStepDto.getStep() - 1);
                updateBallCoordinate(matchStepDto, startPointBall);
                matchStepDto.setLastStepLog(stepLog);
                matchStepDto.getLog().add(matchStepDto.getLastStepLog());
                return matchStepDto;
            }
            action = matchStepDto.getFirstTeamAction();
        }

        int addition = matchStepDto.getSecondTeamAction() == action ? EQUALS_ACTION_BONUS : 0;

        if (matchStepDto.getPosition() == 1) {

            if (matchStepDto.isFirstTeamBall()) {
                if (action == 1) {
                    matchStepDto.setPosition(2);
                    if (Math.random() * (matchStepDto.getFirstPlayer().getSkill() * DEFENSE_BALLOUT_SKILL_INDEX + matchStepDto.getFirstPlayer().getSpeed() * DEFENSE_BALLOUT_SPEED_INDEX) * matchStepDto.getFirstTeamChance() / 100
                            > Math.random() * (matchStepDto.getSecondPlayer().getSkill() * ATTACK_SKILL_INDEX + matchStepDto.getSecondPlayer().getSpeed() * ATTACK_SPEED_INDEX) * (matchStepDto.getSecondTeamChance() + addition) / 100) {
                        stepLog += matchStepDto.getFirstPlayer().getName() + " выбивает мяч";
                        matchStepDto.plusChance(1,4);
                        matchStepDto.setFirstPlayer(getRandomPlayerByType(matchStepDto.getMatchDto().getFirstTeam().getPlayers(), PlayerType.MD));
                        matchStepDto.setSecondPlayer(getRandomPlayerByType(matchStepDto.getMatchDto().getSecondTeam().getPlayers(), PlayerType.MD));
                    } else {
                        matchStepDto.setFirstTeamBall(false);
                        stepLog += matchStepDto.getSecondPlayer().getName() + " забирает мяч";
                        matchStepDto.plusChance(2,4);
                    }
                } else if (action == 2) {
                    matchStepDto.plusChance(1,3);
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
                } else if (action == 3) {
                    matchStepDto.setSecondPlayer(getRandomPlayerByType(matchStepDto.getMatchDto().getSecondTeam().getPlayers(), PlayerType.ST));
                    if (Math.random() * (matchStepDto.getFirstPlayer().getSkill() * DEFENSE_BALLOUT_SKILL_INDEX + matchStepDto.getFirstPlayer().getSpeed() * DEFENSE_BALLOUT_SPEED_INDEX) * matchStepDto.getFirstTeamChance() / 100
                            > Math.random() * (matchStepDto.getSecondPlayer().getSkill() + matchStepDto.getSecondPlayer().getSpeed() * ATTACK_SPEED_INDEX) * (matchStepDto.getSecondTeamChance() + addition) / 100) {
                        stepLog += matchStepDto.getFirstPlayer().getName() + " дает пас вратарю";
                        matchStepDto.plusChance(1, 7);
                    } else {
                        stepLog += matchStepDto.getSecondPlayer().getName() + " опасно перехватывает мяч";
                        matchStepDto.plusChance(2, 4);
                        matchStepDto.minusChance(1, 5);
                        matchStepDto.setFirstTeamBall(false);
                    }
                    matchStepDto.setFirstPlayer(getRandomPlayerByType(matchStepDto.getMatchDto().getFirstTeam().getPlayers(), PlayerType.GK));
                }
            } else {
                if (matchStepDto.getSecondTeamAction() == 1) {
                    matchStepDto.getStatisticDto().getGoalKick().y++;
                    matchStepDto.setFirstPlayer(getRandomPlayerByType(matchStepDto.getMatchDto().getFirstTeam().getPlayers(), PlayerType.GK));
                    attackFactor = Math.random() * (matchStepDto.getSecondPlayer().getSkill() * ATTACK_SKILL_INDEX + matchStepDto.getSecondPlayer().getSpeed() * ATTACK_SPEED_INDEX) * matchStepDto.getSecondTeamChance() / 100
                            - Math.random() * (matchStepDto.getFirstPlayer().getSkill() * KEEPER_SKILL_INDEX + matchStepDto.getFirstPlayer().getSpeed() * KEEPER_SPEED_INDEX) * (matchStepDto.getFirstTeamChance() + addition) / 100;

                    matchStepDto.setFirstTeamBall(true);
                    stepLog += matchStepDto.getFirstPlayer().getName() + attackLog(attackFactor, addition);
                    if (attackFactor > 0) {
                        matchStepDto.setPosition(2);
                        matchStepDto.increaseGoal(2);
                        stepLog += matchStepDto.getSecondPlayer().getName() + attackLog(attackFactor, addition) + matchStepDto.showGoals();
                        matchStepDto.getStatisticDto().getGoals().y++;
                        startPointBall = true;
                        matchStepDto.getStatisticDto().getGoalsList().add(new GoalsInMatch(
                                null, matchStepDto.getMatchDto().getMatchId(), matchStepDto.getMatchDto().getSecondTeam().getTeam(), matchStepDto.getSecondPlayer(),
                                matchStepDto.getStep(), matchStepDto.getMatchDto().getFirstTeam().getTeam().getLeagueId()
                        ));
                        matchStepDto.minusChance(1, 8);
                        matchStepDto.plusChance(2,5);
                        matchStepDto.setFirstPlayer(getRandomPlayerByType(matchStepDto.getMatchDto().getFirstTeam().getPlayers(), PlayerType.MD));
                        matchStepDto.setSecondPlayer(getRandomPlayerByType(matchStepDto.getMatchDto().getSecondTeam().getPlayers(), PlayerType.MD));
                    } else {
                        matchStepDto.plusChance(1,2);
                        matchStepDto.minusChance(2,3);
                    }
                } else if (matchStepDto.getSecondTeamAction() == 2) {
                    if (Math.random() * (matchStepDto.getSecondPlayer().getSkill() * ATTACK_SKILL_INDEX + matchStepDto.getSecondPlayer().getSpeed() * ATTACK_SPEED_INDEX) * matchStepDto.getSecondTeamChance() / 100
                            > Math.random() * (matchStepDto.getFirstPlayer().getSkill() * DEFENSE_SKILL_INDEX + matchStepDto.getFirstPlayer().getSpeed() * DEFENSE_SPEED_INDEX) * (matchStepDto.getFirstTeamChance() + addition) / 100) {
                        matchStepDto.plusChance(2,3);
                        matchStepDto.minusChance(1,1);
                        stepLog += matchStepDto.getSecondPlayer().getName() + " обводит " + matchStepDto.getFirstPlayer().getName();
                        matchStepDto.setFirstPlayer(getRandomPlayerByType(matchStepDto.getMatchDto().getFirstTeam().getPlayers(), PlayerType.CD));
                    } else {
                        matchStepDto.setFirstTeamBall(true);
                        stepLog += matchStepDto.getSecondPlayer().getName() + " теряет мяч";
                        matchStepDto.setSecondPlayer(getRandomPlayerByType(matchStepDto.getMatchDto().getSecondTeam().getPlayers(), PlayerType.ST));
                    }
                } else if (matchStepDto.getSecondTeamAction() == 3) {
                    matchStepDto.setPosition(2);
                    matchStepDto.setFirstPlayer(getRandomPlayerByType(matchStepDto.getMatchDto().getFirstTeam().getPlayers(), PlayerType.MD));
                    if (Math.random() * (matchStepDto.getSecondPlayer().getSkill() * ATTACK_BALLOUT_SKILL_INDEX + matchStepDto.getSecondPlayer().getSpeed() * ATTACK_BALLOUT_SPEED_INDEX) * matchStepDto.getSecondTeamChance() / 100
                            > Math.random() * (matchStepDto.getFirstPlayer().getSkill() * MIDDLE_DEFENSE_SKILL_INDEX + matchStepDto.getFirstPlayer().getSpeed() * MIDDLE_DEFENSE_SPEED_INDEX) * (matchStepDto.getFirstTeamChance()) / 130) {
                        matchStepDto.plusChance(2, 5);
                        matchStepDto.minusChance(1,5);
                        stepLog += matchStepDto.getSecondPlayer().getName() + " делает пас назад";
                    } else {
                        matchStepDto.plusChance(1, 5);
                        matchStepDto.minusChance(2,5);
                        matchStepDto.setFirstTeamBall(true);
                        stepLog += matchStepDto.getFirstPlayer().getName() + " перехватывает обратную передачу";
                    }

                    matchStepDto.setSecondPlayer(getRandomPlayerByType(matchStepDto.getMatchDto().getSecondTeam().getPlayers(), PlayerType.MD));
                }
            }
        } else if (matchStepDto.getPosition() == 2) {

            if (matchStepDto.isFirstTeamBall()) {
                if (action == 1) {
                    if (Math.random() * (matchStepDto.getFirstPlayer().getSkill() * MIDDLE_ATTACK_SKILL_INDEX + matchStepDto.getFirstPlayer().getSpeed() * MIDDLE_ATTACK_SPEED_INDEX) * matchStepDto.getFirstTeamChance() / 100
                            > Math.random() * (matchStepDto.getSecondPlayer().getSkill() * MIDDLE_DEFENSE_SKILL_INDEX + matchStepDto.getSecondPlayer().getSpeed() * MIDDLE_DEFENSE_SPEED_INDEX) * (matchStepDto.getSecondTeamChance() + addition) / 100) {
                        matchStepDto.setPosition(3);
                        matchStepDto.minusChance(2, 2);
                        stepLog += matchStepDto.getFirstPlayer().getName() + " передает пас вперед";
                        matchStepDto.setFirstPlayer(getRandomPlayerByType(matchStepDto.getMatchDto().getFirstTeam().getPlayers(), PlayerType.ST));
                        matchStepDto.setSecondPlayer(getRandomPlayerByType(matchStepDto.getMatchDto().getSecondTeam().getPlayers(), PlayerType.CD));
                    } else {
                        matchStepDto.setFirstTeamBall(false);
                        matchStepDto.minusChance(1,2);
                        stepLog += matchStepDto.getFirstPlayer().getName() + " теряет мяч";
                    }
                } else if (action == 2) {
                    if (Math.random() * (matchStepDto.getFirstPlayer().getSkill() * MIDDLE_ACTIVE_INDEX + matchStepDto.getFirstPlayer().getSpeed() * MIDDLE_ACTIVE_INDEX) * matchStepDto.getFirstTeamChance() / 100
                            > Math.random() * (matchStepDto.getSecondPlayer().getSkill() * MIDDLE_DEFENSE_SPEED_INDEX + matchStepDto.getSecondPlayer().getSpeed() * MIDDLE_DEFENSE_SPEED_INDEX) * (matchStepDto.getSecondTeamChance() + addition) / 100) {
                        matchStepDto.plusChance(1,3);
                        stepLog += matchStepDto.getFirstPlayer().getName() + " укрепляет позицию";
                        matchStepDto.setFirstPlayer(getRandomPlayerByType(matchStepDto.getMatchDto().getFirstTeam().getPlayers(), PlayerType.MD));
                        matchStepDto.setSecondPlayer(getRandomPlayerByType(matchStepDto.getMatchDto().getSecondTeam().getPlayers(), PlayerType.MD));
                    } else {
                        matchStepDto.setFirstTeamBall(false);
                        matchStepDto.minusChance(1, 3);
                        stepLog += matchStepDto.getFirstPlayer().getName() + " теряет мяч";
                        matchStepDto.setFirstPlayer(getRandomPlayerByType(matchStepDto.getMatchDto().getFirstTeam().getPlayers(), PlayerType.MD));
                    }
                } else if (action == 3) {
                    if (Math.random() * (matchStepDto.getFirstPlayer().getSkill() * MIDDLE_ACTIVE_INDEX + matchStepDto.getFirstPlayer().getSpeed() * MIDDLE_ACTIVE_INDEX) * matchStepDto.getFirstTeamChance() / 100
                            > Math.random() * (matchStepDto.getSecondPlayer().getSkill() + matchStepDto.getSecondPlayer().getSpeed() * MIDDLE_ATTACK_SPEED_INDEX) * (matchStepDto.getSecondTeamChance() + addition) / 100) {
                        matchStepDto.plusChance(1, 5);
                        matchStepDto.minusChance(2, 1);
                        matchStepDto.setPosition(1);
                        stepLog += matchStepDto.getFirstPlayer().getName() + " делает пас назад";
                        matchStepDto.setFirstPlayer(getRandomPlayerByType(matchStepDto.getMatchDto().getFirstTeam().getPlayers(), PlayerType.CD));
                        matchStepDto.setSecondPlayer(getRandomPlayerByType(matchStepDto.getMatchDto().getSecondTeam().getPlayers(), PlayerType.ST));
                    } else {
                        stepLog += matchStepDto.getSecondPlayer().getName() + " перехватывает мяч";
                        matchStepDto.setFirstPlayer(getRandomPlayerByType(matchStepDto.getMatchDto().getFirstTeam().getPlayers(), PlayerType.MD));
                        matchStepDto.setSecondPlayer(getRandomPlayerByType(matchStepDto.getMatchDto().getSecondTeam().getPlayers(), PlayerType.MD));
                    }
                }
            } else {
                if (matchStepDto.getSecondTeamAction() == 1) {
                    if (Math.random() * (matchStepDto.getSecondPlayer().getSkill() * MIDDLE_ATTACK_SKILL_INDEX + matchStepDto.getSecondPlayer().getSpeed() * MIDDLE_ATTACK_SPEED_INDEX) * matchStepDto.getSecondTeamChance() / 100
                            > Math.random() * (matchStepDto.getFirstPlayer().getSkill() * MIDDLE_DEFENSE_SKILL_INDEX + matchStepDto.getFirstPlayer().getSpeed() * MIDDLE_DEFENSE_SPEED_INDEX) * (matchStepDto.getFirstTeamChance() + addition) / 100) {
                        matchStepDto.setPosition(1);
                        matchStepDto.minusChance(1, 2);
                        stepLog += matchStepDto.getSecondPlayer().getName() + " передает пас вперед";
                        matchStepDto.setFirstPlayer(getRandomPlayerByType(matchStepDto.getMatchDto().getFirstTeam().getPlayers(), PlayerType.CD));
                        matchStepDto.setSecondPlayer(getRandomPlayerByType(matchStepDto.getMatchDto().getSecondTeam().getPlayers(), PlayerType.ST));
                    } else {
                        matchStepDto.setFirstTeamBall(true);
                        matchStepDto.minusChance(2,2);
                        stepLog += matchStepDto.getSecondPlayer().getName() + " теряет мяч";
                    }
                } else if (matchStepDto.getSecondTeamAction() == 2) {
                    if (Math.random() * (matchStepDto.getSecondPlayer().getSkill() * MIDDLE_ACTIVE_INDEX + matchStepDto.getSecondPlayer().getSpeed() * MIDDLE_ACTIVE_INDEX) * matchStepDto.getSecondTeamChance() / 100
                            > Math.random() * (matchStepDto.getFirstPlayer().getSkill() * MIDDLE_DEFENSE_SPEED_INDEX + matchStepDto.getFirstPlayer().getSpeed() * MIDDLE_DEFENSE_SPEED_INDEX) * (matchStepDto.getFirstTeamChance() + addition) / 100) {
                        matchStepDto.plusChance(2,3);
                        stepLog += matchStepDto.getSecondPlayer().getName() + " укрепляет позицию";
                        matchStepDto.setFirstPlayer(getRandomPlayerByType(matchStepDto.getMatchDto().getFirstTeam().getPlayers(), PlayerType.MD));
                        matchStepDto.setSecondPlayer(getRandomPlayerByType(matchStepDto.getMatchDto().getSecondTeam().getPlayers(), PlayerType.MD));
                    } else {
                        matchStepDto.setFirstTeamBall(true);
                        matchStepDto.minusChance(2, 3);
                        stepLog += matchStepDto.getSecondPlayer().getName() + " теряет мяч";
                        matchStepDto.setSecondPlayer(getRandomPlayerByType(matchStepDto.getMatchDto().getSecondTeam().getPlayers(), PlayerType.MD));
                    }
                } else if (action == 3) {
                    if (Math.random() * (matchStepDto.getSecondPlayer().getSkill() * MIDDLE_ACTIVE_INDEX + matchStepDto.getSecondPlayer().getSpeed() * MIDDLE_ACTIVE_INDEX) * matchStepDto.getSecondTeamChance() / 100
                            > Math.random() * (matchStepDto.getFirstPlayer().getSkill() + matchStepDto.getFirstPlayer().getSpeed() * MIDDLE_ATTACK_SPEED_INDEX) * (matchStepDto.getFirstTeamChance() + addition) / 100) {
                        matchStepDto.plusChance(2, 5);
                        matchStepDto.minusChance(1, 1);
                        matchStepDto.setPosition(3);
                        stepLog += matchStepDto.getFirstPlayer().getName() + " делает пас назад";
                        matchStepDto.setFirstPlayer(getRandomPlayerByType(matchStepDto.getMatchDto().getFirstTeam().getPlayers(), PlayerType.ST));
                        matchStepDto.setSecondPlayer(getRandomPlayerByType(matchStepDto.getMatchDto().getSecondTeam().getPlayers(), PlayerType.CD));
                    } else {
                        stepLog += matchStepDto.getSecondPlayer().getName() + " перехватывает мяч";
                        matchStepDto.setFirstPlayer(getRandomPlayerByType(matchStepDto.getMatchDto().getFirstTeam().getPlayers(), PlayerType.MD));
                        matchStepDto.setSecondPlayer(getRandomPlayerByType(matchStepDto.getMatchDto().getSecondTeam().getPlayers(), PlayerType.MD));
                    }
                }
            }
        } else if (matchStepDto.getPosition() == 3) {

            if (!matchStepDto.isFirstTeamBall()) {
                if (matchStepDto.getSecondTeamAction() == 1) {
                    matchStepDto.setPosition(2);
                    if (Math.random() * (matchStepDto.getSecondPlayer().getSkill() * DEFENSE_BALLOUT_SKILL_INDEX + matchStepDto.getSecondPlayer().getSpeed() * DEFENSE_BALLOUT_SPEED_INDEX) * matchStepDto.getSecondTeamChance() / 100
                            > Math.random() * (matchStepDto.getFirstPlayer().getSkill() * ATTACK_SKILL_INDEX + matchStepDto.getFirstPlayer().getSpeed() * ATTACK_SPEED_INDEX) * (matchStepDto.getFirstTeamChance() + addition) / 100) {
                        stepLog += matchStepDto.getSecondPlayer().getName() + " выбивает мяч";
                        matchStepDto.plusChance(2,4);
                        matchStepDto.setFirstPlayer(getRandomPlayerByType(matchStepDto.getMatchDto().getFirstTeam().getPlayers(), PlayerType.MD));
                        matchStepDto.setSecondPlayer(getRandomPlayerByType(matchStepDto.getMatchDto().getSecondTeam().getPlayers(), PlayerType.MD));
                    } else {
                        matchStepDto.setFirstTeamBall(true);
                        stepLog += matchStepDto.getFirstPlayer().getName() + " забирает мяч";
                        matchStepDto.plusChance(1,4);
                    }
                } else if (matchStepDto.getSecondTeamAction() == 2) {
                    matchStepDto.plusChance(2,3);
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
                } else if (matchStepDto.getSecondTeamAction() == 3) {
                    matchStepDto.setFirstPlayer(getRandomPlayerByType(matchStepDto.getMatchDto().getFirstTeam().getPlayers(), PlayerType.ST));
                    if (Math.random() * (matchStepDto.getSecondPlayer().getSkill() * DEFENSE_BALLOUT_SKILL_INDEX + matchStepDto.getSecondPlayer().getSpeed() * DEFENSE_BALLOUT_SPEED_INDEX) * matchStepDto.getSecondTeamChance() / 100
                            > Math.random() * (matchStepDto.getFirstPlayer().getSkill() + matchStepDto.getFirstPlayer().getSpeed() * ATTACK_SPEED_INDEX) * (matchStepDto.getFirstTeamChance() + addition) / 100) {
                        stepLog += matchStepDto.getSecondPlayer().getName() + " дает пас вратарю";
                        matchStepDto.plusChance(2, 7);
                    } else {
                        stepLog += matchStepDto.getFirstPlayer().getName() + " опасно перехватывает мяч";
                        matchStepDto.plusChance(1, 4);
                        matchStepDto.minusChance(2, 5);
                        matchStepDto.setFirstTeamBall(true);
                    }
                    matchStepDto.setSecondPlayer(getRandomPlayerByType(matchStepDto.getMatchDto().getSecondTeam().getPlayers(), PlayerType.GK));
                }
            } else {
                if (action == 1) {
                    matchStepDto.getStatisticDto().getGoalKick().x++;
                    matchStepDto.setSecondPlayer(getRandomPlayerByType(matchStepDto.getMatchDto().getSecondTeam().getPlayers(), PlayerType.GK));
                    attackFactor = Math.random() * (matchStepDto.getFirstPlayer().getSkill() * ATTACK_SKILL_INDEX + matchStepDto.getFirstPlayer().getSpeed() * ATTACK_SPEED_INDEX) * matchStepDto.getFirstTeamChance() / 100
                            - Math.random() * (matchStepDto.getSecondPlayer().getSkill() * KEEPER_SKILL_INDEX + matchStepDto.getSecondPlayer().getSpeed() * KEEPER_SPEED_INDEX) * (matchStepDto.getSecondTeamChance() + addition) / 100;

                    matchStepDto.setFirstTeamBall(false);
                    stepLog += matchStepDto.getSecondPlayer().getName() + attackLog(attackFactor, addition);
                    if (attackFactor > 0) {
                        matchStepDto.setPosition(2);
                        matchStepDto.increaseGoal(1);
                        stepLog += matchStepDto.getFirstPlayer().getName() + attackLog(attackFactor, addition) + matchStepDto.showGoals();
                        matchStepDto.getStatisticDto().getGoals().x++;
                        startPointBall = true;
                        matchStepDto.getStatisticDto().getGoalsList().add(new GoalsInMatch(
                                null, matchStepDto.getMatchDto().getMatchId(), matchStepDto.getMatchDto().getFirstTeam().getTeam(), matchStepDto.getFirstPlayer(),
                                matchStepDto.getStep(), matchStepDto.getMatchDto().getFirstTeam().getTeam().getLeagueId()
                        ));
                        matchStepDto.minusChance(2, 8);
                        matchStepDto.plusChance(1,5);
                        matchStepDto.setFirstPlayer(getRandomPlayerByType(matchStepDto.getMatchDto().getFirstTeam().getPlayers(), PlayerType.MD));
                        matchStepDto.setSecondPlayer(getRandomPlayerByType(matchStepDto.getMatchDto().getSecondTeam().getPlayers(), PlayerType.MD));
                    } else {
                        matchStepDto.plusChance(2,2);
                        matchStepDto.minusChance(1,3);
                    }
                } else if (action == 2) {
                    if (Math.random() * (matchStepDto.getFirstPlayer().getSkill() * ATTACK_SKILL_INDEX + matchStepDto.getFirstPlayer().getSpeed() * ATTACK_SPEED_INDEX) * matchStepDto.getFirstTeamChance() / 100
                            > Math.random() * (matchStepDto.getSecondPlayer().getSkill() * DEFENSE_SKILL_INDEX + matchStepDto.getSecondPlayer().getSpeed() * DEFENSE_SPEED_INDEX) * (matchStepDto.getSecondTeamChance() + addition) / 100) {
                        matchStepDto.plusChance(1,3);
                        matchStepDto.minusChance(2,1);
                        stepLog += matchStepDto.getFirstPlayer().getName() + " обводит " + matchStepDto.getSecondPlayer().getName();
                        matchStepDto.setSecondPlayer(getRandomPlayerByType(matchStepDto.getMatchDto().getSecondTeam().getPlayers(), PlayerType.CD));
                    } else {
                        matchStepDto.setFirstTeamBall(false);
                        stepLog += matchStepDto.getFirstPlayer().getName() + " теряет мяч";
                        matchStepDto.setFirstPlayer(getRandomPlayerByType(matchStepDto.getMatchDto().getFirstTeam().getPlayers(), PlayerType.ST));
                    }
                } else if (action == 3) {
                    matchStepDto.setPosition(2);
                    matchStepDto.setSecondPlayer(getRandomPlayerByType(matchStepDto.getMatchDto().getSecondTeam().getPlayers(), PlayerType.MD));
                    if (Math.random() * (matchStepDto.getFirstPlayer().getSkill() * ATTACK_BALLOUT_SKILL_INDEX + matchStepDto.getFirstPlayer().getSpeed() * ATTACK_BALLOUT_SPEED_INDEX) * matchStepDto.getFirstTeamChance() / 100
                            > Math.random() * (matchStepDto.getSecondPlayer().getSkill() * MIDDLE_DEFENSE_SKILL_INDEX + matchStepDto.getSecondPlayer().getSpeed() * MIDDLE_DEFENSE_SPEED_INDEX) * (matchStepDto.getSecondTeamChance()) / 130) {
                        matchStepDto.plusChance(1, 5);
                        matchStepDto.minusChance(2,5);
                        stepLog += matchStepDto.getFirstPlayer().getName() + " делает пас назад";
                    } else {
                        matchStepDto.plusChance(2, 5);
                        matchStepDto.minusChance(1,5);
                        matchStepDto.setFirstTeamBall(false);
                        stepLog += matchStepDto.getSecondPlayer().getName() + " перехватывает обратную передачу";
                    }

                    matchStepDto.setFirstPlayer(getRandomPlayerByType(matchStepDto.getMatchDto().getFirstTeam().getPlayers(), PlayerType.MD));
                }
            }
        }

        updateBallCoordinate(matchStepDto, startPointBall);
        matchStepDto.setLastStepLog(stepLog);
        matchStepDto.getLog().add(matchStepDto.getLastStepLog());
        matchStepDto.nullableActionStep();
        return matchStepDto;
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
        matchRepository.deleteAllFriendlyMatches();

        LocalDateTime beginSimulateTime;
        for (MatchPlay matchPlay : matchPlayList) {
            beginSimulateTime = LocalDateTime.now();
            matchDto = startGame(matchPlay.getFirstTeamId(), matchPlay.getSecondTeamId());
            matchStepDto = makeGameStep(matchDto.getMatchId(), PlaySide.CPU, 1);
            while (!matchStepDto.getLastStepLog().equals(matchStepDto.showGoals())) {
                matchStepDto = makeGameStep(matchDto.getMatchId(), PlaySide.CPU, 1);
            }
            System.out.println(matchStepDto.showGoals() + " simulation time: " + ChronoUnit.SECONDS.between(beginSimulateTime, LocalDateTime.now()) + "s");
        }
    }

    public List<MatchDto> getMatchesForPlayLeaguesGame() {
        User user = userRepository.findFirstByUserLastIp(servletRequest.getRemoteAddr());
        List<MatchDto> result = new ArrayList<>();
        if (user != null && user.getTeamId() != null) {
            List<MatchPlay> matchPlays = matchRepository.getByStarted(LocalDate.now());
            for (MatchPlay m : matchPlays) {
                if (m.getStatus() != MatchStatus.FINISHED && (user.getTeamId().equals(m.getFirstTeamId()) || user.getTeamId().equals(m.getSecondTeamId())))
                    result.add(new MatchDto(m.getId(), PlaySide.CPU, m.getStarted(), teamService.getTeamById(m.getFirstTeamId()), teamService.getTeamById(m.getSecondTeamId()), m.getType()));
            }
        }
        result.addAll(0, matchStepDtos.stream().filter(msd -> msd.getMatchDto().getPlaySide() == PlaySide.FiRST_TEAM).map(MatchStepDto::getMatchDto).collect(Collectors.toList()));
        return result;
    }

    @PostConstruct
    @Scheduled(cron = "5 5 * * * *")
    public List<StatisticDto> updateLastLeagueMatches() {

        String sql = "select s.*, ft.name as ftm, st.name as stm, mp.started from match_play mp " +
                "inner join team ft on ft.id=mp.first_team_id " +
                "inner join team st on st.id=mp.second_team_id " +
                "inner join statistic s on s.match_id = mp.id " +
                "where mp.type = 1 and mp.status = 2 order by mp.started desc limit ?";

        lastMatches.clear();
        lastMatches.addAll(jdbcTemplate.query(sql, new Object[]{GameConstants.AMOUNT_LAST_LEAGUE_GAME_FOR_STATISTIC}, new StatisticDtoRowMapper()));
        for (StatisticDto s : lastMatches) {
            s.getGoalsList().addAll(goalsInMatchRepository.getByMatchId(s.getMatchId()));
        }
        System.out.println("matches statistic updated");
        return lastMatches;
    }

    public List<StatisticDto> getLeagueStatisticOfMatches (Long leagueId) {
        String sql = "select s.*, ft.name as ftm, st.name as stm, mp.started from match_play mp " +
                "inner join team ft on ft.id=mp.first_team_id " +
                "inner join team st on st.id=mp.second_team_id " +
                "left join statistic s on s.match_id = mp.id " +
                "where mp.type = 1 and ft.league_id = ? and mp.started > '" + LocalDate.now().minusDays(2) + "' and mp.started < '" + LocalDate.now().plusDays(2) + "'";

        List<StatisticDto> result = new ArrayList<>(jdbcTemplate.query(sql, new Object[]{leagueId}, new StatisticDtoRowMapper()));
        for (StatisticDto s : result) {
            s.getGoalsList().addAll(goalsInMatchRepository.getByMatchId(s.getMatchId()));
        }

        return result;
    }

    public ArrayList<StatisticDto> getLastMatches() {
        return lastMatches;
    }

    public void updatePlayersExperience(MatchStepDto matchStepDto) {
        List<Player> players = matchStepDto.getMatchDto().getFirstTeam().getPlayers();
        players.addAll(matchStepDto.getMatchDto().getSecondTeam().getPlayers());

        int exp;

        for (Player player : players) {
            exp = player.getExp() + countAndUpdatePlayerExperience(player);
            if (exp > 99) {
                exp = 0;
                if (Math.random() > 0.5) {
                    player.setSpeed(player.getSpeed() + 1);
                } else {
                    player.setSkill(player.getSkill() + 1);
                }
            }
            player.setExp(exp);
        }
        playerRepository.saveAll(players);
    }

    public MatchPlay getLastLeagueGame(Long teamId) {
        return matchRepository.getLastLeagueHomeGame(teamId);
    }

    private int countAndUpdatePlayerExperience(Player player) {
        if (player.getAge() < 21) {
            return (int) (Math.random() * 15) + 1;
        } else if (player.getAge() < 25) {
            return (int) (Math.random() * 10) + 1;
        } if (player.getAge() < 30) {
            return (int) (Math.random() * 7) + 1;
        } if (player.getAge() < 35) {
            return (int) (Math.random() * 5) + 1;
        } if (player.getAge() < 40) {
            return (int) (Math.random() * 3) + 1;
        }
        return 1;
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
            if (factor > 10000) {
                return " невероятно поклал мяч в девятку из далека!!!!";
            } else if (factor > 5000) {
                return " обыграл вратаря и забил гол!!!";
            } else if (factor > 0) {
                return " гол из далека!";
            } else if (factor < 10000) {
                return " с легкостью берет мяч в руки";
            } else if (factor < 5000) {
                return " смотрит как мяч пролетает далеко мимо ворот";
            } else if (factor <= 0) {
                return " реагирует и забирает мяч";
            }
        } else {
            if (factor > 15000) {
                return " не оставил шанса вратврю!!!!";
            } else if (factor > 7000) {
                return " запутал вратаря и точно пробил по воротам!!";
            } else if (factor > 0) {
                return " забивает гол!";
            } else if (factor < 12000) {
                return " выбрал правильную позицию и забрал мяч из под ног";
            } else if (factor < 8000) {
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
        int f = matchStepDto.getGoalFirstTeam();
        int s = matchStepDto.getGoalSecondTeam();
        leagueTableItemRepository.increaseDataForLeagueTable(f > s ? 1 : 0, f < s ? 1 : 0, f == s ? 1 : 0, f, s, f > s ? 3 : f == s ? 1 : 0,
                matchStepDto.getMatchDto().getFirstTeam().getTeam().getId(), matchStepDto.getMatchDto().getFirstTeam().getTeam().getLeagueId()
        );
        leagueTableItemRepository.increaseDataForLeagueTable(f < s ? 1 : 0, f > s ? 1 : 0, f == s ? 1 : 0, s, f, f > s ? 0 : f == s ? 1 : 3,
                matchStepDto.getMatchDto().getSecondTeam().getTeam().getId(), matchStepDto.getMatchDto().getFirstTeam().getTeam().getLeagueId()
        );

        List<LeagueTableItem> leagueTableItems = leagueTableItemRepository.getByLeagueId(matchStepDto.getMatchDto().getFirstTeam().getTeam().getLeagueId());
        Team firstTeam = matchStepDto.getMatchDto().getFirstTeam().getTeam();
        Team secondTeam = matchStepDto.getMatchDto().getSecondTeam().getTeam();
        Stadium stadium = matchStepDto.getStadium();
        // money from league match
        int moneyEarned = matchStepDto.getFuns() * stadium.getTicketPrice();
        firstTeam.setMoney(firstTeam.getMoney() + moneyEarned);
        writeTeamMessages(firstTeam.getId(),
                matchStepDto.getMatchDto().getDate() + " был проведен домашний матч лиги. Присутствовало " + matchStepDto.getFuns() + " болельщиков. Прибыль: " + moneyEarned);
        //funs update
        firstTeam.setFuns(Math.min(firstTeam.getStadium().getPopulation() * FUN_LIMIT, Math.max(0, firstTeam.getFuns() + f * GameConstants.ADD_FUNS_BY_GOAL - s * GameConstants.LOSE_FUNS_BY_GOAL)));
        secondTeam.setFuns(Math.min(secondTeam.getStadium().getPopulation() * FUN_LIMIT,Math.max(0, secondTeam.getFuns() + s * GameConstants.ADD_FUNS_BY_GOAL - f * GameConstants.LOSE_FUNS_BY_GOAL)));

        teamService.updateOrSave(firstTeam);
        teamService.updateOrSave(secondTeam);

        leagueTableItems.sort(Comparator.comparingInt(LeagueTableItem::getPoint).reversed().thenComparing(l -> -l.getGoals()).thenComparing(LeagueTableItem::getGoalLose));
        int i = 0;
        for (LeagueTableItem l : leagueTableItems) {
            leagueTableItemRepository.updatePosition(++i, l.getId());
        }
    }

    private void writeTeamMessages(Long teamId, String body) {
        Message message = new Message();
        message.setToId(teamId);
        message.setBody(body);
        message.setType(MessageTypeEnum.TEAM_ACTION);
        message.setCreateTime(LocalDateTime.now());

        messageRepository.save(message);
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

    public List<Point> returnAllPosition() {
        List<Point> result = new ArrayList<>(Arrays.asList(
                new Point(100, 270),new Point(200, 330),new Point(200, 450),new Point(200, 200),
                new Point(200, 60),new Point(500, 270),new Point(950, 270),new Point(500, 120),
                new Point(950, 120),new Point(500, 420),new Point(950, 420),new Point(1050, 270),
                new Point(950, 330),new Point(950, 450),new Point(950, 200),new Point(950, 60),
                new Point(550, 270),new Point(200, 270),new Point(550, 120),new Point(200, 120),
                new Point(550, 420),new Point(200, 420)
                ));

        return result;
    }

    private void calculatePopulation(MatchStepDto matchStepDto) {
        Team team = matchStepDto.getMatchDto().getFirstTeam().getTeam();
        int funs = team.getFuns();
        Stadium stadium = matchStepDto.getStadium();
        if (funs != 0 && stadium.getTicketPrice() > 0) {
            int funPrice = (int) (GameConstants.FUN_TICKET_PRICE * (double) funs / stadium.getType().getPopulation());
            funPrice = Math.min(funPrice, GameConstants.FUN_TICKET_PRICE);
            if (funPrice < stadium.getTicketPrice()) {
                if (funPrice == 0) {
                    funs = 0;
                } else {
                    funs = (int) (funs / (double) (stadium.getTicketPrice() / funPrice));
                }
            }
        }
        funs = Math.min(funs, stadium.getType().getPopulation());
        matchStepDto.setFuns(funs);
        matchStepDto.setAdditionHomeMaxChance(GameConstants.MAX_ADDITION_CHANCE * funs / stadium.getType().getPopulation());
    }

}
