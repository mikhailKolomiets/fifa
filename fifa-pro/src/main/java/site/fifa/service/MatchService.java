package site.fifa.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.fifa.dto.*;
import site.fifa.dto.mappers.StatisticDtoRowMapper;
import site.fifa.entity.*;
import site.fifa.entity.match.GoalsInMatch;
import site.fifa.entity.match.MatchPlay;
import site.fifa.entity.match.MatchStatus;
import site.fifa.entity.match.MatchType;
import site.fifa.repository.*;

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
    private PlayerRepository playerRepository;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static ArrayList<MatchStepDto> matchStepDtos = new ArrayList<>();
    private static ArrayList<StatisticDto> lastMatches = new ArrayList<>();

    @Transactional
    public MatchDto startMatchWithPC(Long firstTeamId, Long secondTeamId, boolean isPC) {

        MatchPlay match = matchRepository.getByFirstTeamIdAndSecondTeamIdAndStatus(firstTeamId, secondTeamId, MatchStatus.CREATED)
                .stream().findAny().orElse(null);
        if (match == null) {
            match = matchRepository.save(new MatchPlay(MatchStatus.STARTED, MatchType.FRIENDLY, LocalDate.now(), firstTeamId, secondTeamId));
        } else if (isPC || getMatchStepDtoById(match.getId()) != null) {
            matchRepository.updateMatchStatusById(MatchStatus.STARTED, match.getId());
        }

        MatchStepDto matchStepDto = new MatchStepDto();
        matchStepDto.setMatchDto(new MatchDto(match.getId(), PlaySide.CPU, LocalDate.now(), teamService.getTeamById(firstTeamId), teamService.getTeamById(secondTeamId)));
        matchStepDto.setFirstPlayer(getRandomPlayerByType(matchStepDto.getMatchDto().getFirstTeam().getPlayers(), PlayerType.MD));
        matchStepDto.setSecondPlayer(getRandomPlayerByType(matchStepDto.getMatchDto().getSecondTeam().getPlayers(), PlayerType.MD));
        matchStepDto.getStatisticDto().setFirstTeamName(matchStepDto.getMatchDto().getFirstTeam().getTeam().getName());
        matchStepDto.getStatisticDto().setSecondTeamName(matchStepDto.getMatchDto().getSecondTeam().getTeam().getName());
        if (!isPC) {
            matchStepDto.getMatchDto().setPlaySide(PlaySide.FiRST_TEAM);
        }
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
                lastMatches.add(matchStepDto.getStatisticDto());
                statisticService.saveStatistic(new Statistic(matchId, matchStepDto.getStatisticDto()));
                matchRepository.updateMatchStatusById(MatchStatus.FINISHED, matchId);
                updateLeagueTable(matchStepDto);
                if (action > 0) {
                    updatePlayersExperience(matchStepDto);
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

        int addition;

        // calculate step action algorithm
        if (action < 1) {
            matchStepDto.setSecondTeamAction(randomizeActionByTeamChance(matchStepDto.getSecondTeamChance()));
            action = randomizeActionByTeamChance(matchStepDto.getFirstTeamChance());
        } else if (matchStepDto.getMatchDto().getPlaySide() != PlaySide.CPU) {
            if (action > 9) {
                matchStepDto.setFirstTeamAction(action / 10);
            } else {
                matchStepDto.setSecondTeamAction(action);
            }
            if (matchStepDto.getFirstTeamAction() == 0 || matchStepDto.getSecondTeamAction() == 0) {
                matchStepDto.setStep(matchStepDto.getStep() - 1);
                updateBallCoordinate(matchStepDto, startPointBall);
                return matchStepDto;
            }
            action = matchStepDto.getFirstTeamAction();
        } else {
            matchStepDto.setSecondTeamAction(randomizeActionByTeamChance(matchStepDto.getSecondTeamChance()));
        }

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
                        matchStepDto.setFirstPlayer(getRandomPlayerByType(matchStepDto.getMatchDto().getFirstTeam().getPlayers(), PlayerType.MD));
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
                        matchStepDto.setFirstPlayer(getRandomPlayerByType(matchStepDto.getMatchDto().getFirstTeam().getPlayers(), PlayerType.MD));
                        matchStepDto.setSecondPlayer(getRandomPlayerByType(matchStepDto.getMatchDto().getSecondTeam().getPlayers(), PlayerType.MD));
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

        for (MatchPlay matchPlay : matchPlayList) {
            matchDto = startMatchWithPC(matchPlay.getFirstTeamId(), matchPlay.getSecondTeamId(), true);
            matchStepDto = makeStepWithCPU(matchDto.getMatchId(), -1);
            while (!matchStepDto.getLastStepLog().equals(matchStepDto.showGoals())) {
                matchStepDto = makeStepWithCPU(matchDto.getMatchId(), -1);
            }
            System.out.println(matchStepDto.showGoals());
        }
    }

    public List<MatchDto> getMatchesForPlayLeaguesGame() {
        List<MatchDto> result = new ArrayList<>();
        List<MatchPlay> matchPlays = matchRepository.getByStarted(LocalDate.now());
        for (MatchPlay m : matchPlays) {
            result.add(new MatchDto(m.getId(), PlaySide.CPU, m.getStarted(), teamService.getTeamById(m.getFirstTeamId()), teamService.getTeamById(m.getSecondTeamId())));
        }
        result.addAll(0, matchStepDtos.stream().filter(msd -> msd.getMatchDto().getPlaySide() == PlaySide.FiRST_TEAM).map(MatchStepDto::getMatchDto).collect(Collectors.toList()));
        return result;
    }

    @PostConstruct
    @Scheduled(cron = "5 5 * * * *")
    public List<StatisticDto> updateLastLeagueMatches() {
        // amount matches in the result
        int amount = 5;

        String sql = "select s.*, ft.name as ftm, st.name as stm  from match_play mp " +
                "inner join team ft on ft.id=mp.first_team_id " +
                "inner join team st on st.id=mp.second_team_id " +
                "inner join statistic s on s.match_id = mp.id " +
                "where mp.type = 1 and mp.status = 2 order by mp.started desc limit ?";

        lastMatches.clear();
        lastMatches.addAll(jdbcTemplate.query(sql, new Object[]{amount}, new StatisticDtoRowMapper()));
        for (StatisticDto s : lastMatches) {
            s.getGoalsList().addAll(goalsInMatchRepository.getByMatchId(s.getMatchId()));
        }
        System.out.println("matches statistic updated");
        return lastMatches;
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
            playerRepository.save(player);
        }

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
        int firstTeamCoefficient = leagueTableItems.size() - matchStepDto.getMatchDto().getFirstTeam().getLeaguePosition() + 1;
        int secondTeamCoefficient = leagueTableItems.size() - matchStepDto.getMatchDto().getSecondTeam().getLeaguePosition() + 1;
        Team firstTeam = matchStepDto.getMatchDto().getFirstTeam().getTeam();
        Team secondTeam = matchStepDto.getMatchDto().getSecondTeam().getTeam();
        firstTeam.setMoney(firstTeam.getMoney() + firstTeamCoefficient * (f > s ? 5 : f == s ? 3 : 1));
        secondTeam.setMoney(secondTeam.getMoney() + secondTeamCoefficient * (s > f ? 5 : f == s ? 3 : 1));

        teamService.updateOrSave(firstTeam);
        teamService.updateOrSave(secondTeam);

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
