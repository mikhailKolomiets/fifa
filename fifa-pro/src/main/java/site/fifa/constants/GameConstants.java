package site.fifa.constants;

public interface GameConstants {

    int AMOUNT_LAST_LEAGUE_GAME_FOR_STATISTIC = 5;
    long COACH_DAYS_FOR_TEAM_CHANGE = 1;
    long USER_LOGOUT_TIMEOUT = 600;
    long USER_IP_CHECK_DAYS = 1;

    int FUN_TICKET_PRICE = 10;
    int ADD_FUNS_BY_GOAL = 2;
    int LOSE_FUNS_BY_GOAL = 1;
    // max funs of team relative by stadium capacity (FUN_LIMIT * population)
    int FUN_LIMIT = 3;
    int MAX_ADDITION_CHANCE = 50;

    // action calculating constants (1-100 is normally)
    int EQUALS_ACTION_BONUS = 15;

    int ATTACK_SKILL_INDEX = 50;
    int ATTACK_SPEED_INDEX = 25;
    int ATTACK_BALLOUT_SKILL_INDEX = 90;
    int ATTACK_BALLOUT_SPEED_INDEX = 50;

    int MIDDLE_DEFENSE_SKILL_INDEX = 70;
    int MIDDLE_DEFENSE_SPEED_INDEX = 25;
    int MIDDLE_ATTACK_SKILL_INDEX = 100;
    int MIDDLE_ATTACK_SPEED_INDEX = 80;
    int MIDDLE_ACTIVE_INDEX = 90;

    int DEFENSE_BALLOUT_SKILL_INDEX = 65;
    int DEFENSE_BALLOUT_SPEED_INDEX = 100;
    int DEFENSE_SPEED_INDEX = 70;
    int DEFENSE_SKILL_INDEX = 50;

    int KEEPER_SKILL_INDEX = 100;
    int KEEPER_SPEED_INDEX = 50;

    //accounting by speed + skill / this
    int PLAYER_SELL_DELETE_CONSTANT = 10;

    //step of second player waiting in minutes
    int MATCH_WITH_PLAYER_TIMEOUT = 1;

    int CHAT_MESSAGE_AMOUNT = 50;
    String GUEST_NAME = "Гость";

}
