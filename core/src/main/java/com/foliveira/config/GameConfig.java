package com.foliveira.config;

public class GameConfig {
    public static final float WIDTH = 800f; //pixels
    public static final float HEIGHT = 600f;//pixels

    public static final float HUD_WIDTH = 400f; //world units
    public static final float HUD_HEIGHT = 400f; //world units

    public static final float WORLD_WIDTH = 32f; //world_units
    public static final float WORLD_HEIGHT = 32f;//world_units
    public static final float WORLD_CENTER_X = WORLD_WIDTH/2;
    public static final float WORLD_CENTER_Y = WORLD_HEIGHT/2;

    public static final int WORLD_SIZE = 6;

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
    public static final int GRAB_GOLD = 4;
    public static final int ACTION = 5;

    //perceptions
    public static final int BREEZE = 1;
    public static final int STENCH = 2;
    public static final int GLITTER = 4;
    public static final int BUMP = 8;
    public static final int SCREAM = 16;

    private GameConfig() {
    }
}
