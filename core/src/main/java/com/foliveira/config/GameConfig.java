package com.foliveira.config;

public class GameConfig {
    public static final float WIDTH = 480f; //pixels
    public static final float HEIGHT = 680f;//pixels

    public static final float HUD_WIDTH = 480f; //world units
    public static final float HUD_HEIGHT = 680f; //world units

    public static final float WORLD_WIDTH = 6.0f; //world_units
    public static final float WORLD_HEIGHT = 10.0f;//world_units

    public static final float WORLD_CENTER_X = WORLD_WIDTH/2;
    public static final float WORLD_CENTER_Y = WORLD_HEIGHT/2;

    public static final float OBSTACLE_SPAWN_TIME = 0.25f;

    public static final float MAX_PLAYER_X_SPEED = 0.25f;

    public static final int LIVES_START = 3;

    public static final float SCORE_MAX_TIME = 1.25f;

    public static final float EASY_OBSTACLE_SPEED = 0.1f;
    public static final float MEDIUM_OBSTACLE_SPEED = 0.15f;
    public static final float HARD_OBSTACLE_SPEED = 0.18f;

    public static final float PLAYER_BOUNDS_RADIUS = 0.4f;  //world units
    public static final float PLAYER_SIZE = 2 * PLAYER_BOUNDS_RADIUS;   //world units

    public static final float OBSTACLE_BOUNDS_RADIUS = 0.3f;  //world units
    public static final float OBSTACLE_SIZE = 2 * OBSTACLE_BOUNDS_RADIUS;  //world units

    public static final int WORLD_SIZE = 6;

    // world elements
    public static final int EMPTY = 0;
    public static final int WALL = 1;
    public static final int GOLD = 2;
    public static final int WUMPUS = 4;
    public static final int PIT = 8;
    public static final int ARROW = 16;
    public static final int AGENT = 32;

    private GameConfig() {
    }
}
