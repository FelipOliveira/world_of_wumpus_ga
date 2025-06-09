package com.foliveira.config;

public class Messages {
    public static final String BREEZE = "[BREEZE] ";
    public static final String STENCH = "[STENCH] ";
    public static final String GLITTER = "[GLITTER] ";
    public static final String BUMP = "[BUMP] ";
    public static final String SCREAM = "[SCREAM] ";
    public static final String NOTHING = "[NOTHING]";
    public static final String GOLD = "[GOLD]";
    public static final String PIT = "[PIT]";
    public static final String WUMPUS = "[WUMPUS]";

    public static final String WELCOME_LOG = "Welcome to the World of Wumpus. Find the gold and return to entrance to win! Use the movement buttons for turn left, right or move forward. Use the action buttons for search for gold or shoot your arrow.";
    public static final String INFO = "INFO: ";
    public static final String INITIAL_MESSAGE_INFO = "Watch out for [BREEZE] or [STENCH], it indicates the [PITS] and the [WUMPUS] are nearby.";
    public static final String GLITTER_INFO = "the [GLITTER] indicates the gold is in this room. Try and search for it!";
    public static final String WUMPUS_NEARBY_INFO = "The " + WUMPUS + " is nearby. You can use your arrow to fight him or flee.";
    public static final String GOT_GOLD_INFO = "You got the [GOLD]. Head back to entrance to win!";
    public static final String TURN_LEFT_INFO = "Turn to left.";
    public static final String TURN_RIGHT_INFO = "Turn to right.";
    public static final String MOVE_FORWARD_INFO = "Move forward.";
    public static final String SEARCH_INFO = "Search the current room.";
    public static final String SPECIAL_INFO = "Shot your arrow the direction you're facing.";
    public static final String AGENT_WON_LOG = "You did it!";
    public static final String ARROW_HIT_WALL = " but it hit the wall...";
    public static final String NO_ARROW = "Your have no arrows left";
    public static final String AGENT_HIT_WALL = "You hit the wall.";
    public static final String ARROW_HIT_WUMPUS = " You hear a " + SCREAM + "!";
    public static final String AGENT_FELL_PIT = "You fell on a " + PIT;
    public static final String AGENT_CAUGHT_BY_WUMPUS = "You got caught by the " + WUMPUS;

    public static final String YOU_FEEL = "You feel ";
    public static final String GAME_OVER_MESSAGE = "Game over. Your score is ";
    public static final String RESET_MESSAGE = ". Press [R] to reset.";
    public static final String YOU_ARE_FACING = "You're facing ";
    public static final String NORTH = "[NORTH]";
    public static final String EAST = "[EAST]";
    public static final String SOUTH = "[SOUTH]";
    public static final String WEST = "[WEST]";

    public static final String TURN_LEFT_ACTION = "You turned to left.";
    public static final String TURN_RIGHT_ACTION = "You turned to right.";
    public static final String MOVE_ACTION = "You moved forward";
    public static final String SEARCH_ACTION = "You search in the room.";
    public static final String SHOOT_ARROW_ACTION = "You shot the arrow!";
    public static final String FOUND_GOLD = " You found " + GOLD;
    public static final String FOUND_NOTHING = "You found " + NOTHING;

    private Messages() {
    }
}
