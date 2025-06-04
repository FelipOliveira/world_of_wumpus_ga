package com.foliveira.config;

public class GameConfig {
    public static final float WIDTH = 640f; //pixels
    public static final float HEIGHT = 480f;//pixels

    public static final float HUD_WIDTH = 640f; //world units
    public static final float HUD_HEIGHT = 480f; //world units

    public static final float WORLD_WIDTH = 640 * 1.5f; //world_units
    public static final float WORLD_HEIGHT = 480f * 1.5f;//world_units
    public static final float WORLD_CENTER_X = WORLD_WIDTH/2;
    public static final float WORLD_CENTER_Y = WORLD_HEIGHT/2;

    public static final int MAP_SIZE = 6;

    // world elements
    public static final int EMPTY = 0;
    public static final int WALL = 1;
    public static final int GOLD = 2;
    public static final int WUMPUS = 4;
    public static final int PIT = 8;
    public static final int ARROW = 16;
    public static final int AGENT = 32;

    public static final int MOVE_FORWARD = 1;
    public static final int ROTATE_LEFT = 2;
    public static final int ROTATE_RIGHT = 3;
    public static final int SEARCH = 4;
    public static final int SPECIAL = 5;

    //perceptions
    public static final int BREEZE = 1;
    public static final int STENCH = 2;
    public static final int GLITTER = 4;
    public static final int BUMP = 8;
    public static final int SCREAM = 16;

    private GameConfig() {
    }
}
