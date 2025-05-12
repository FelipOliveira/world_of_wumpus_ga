package com.foliveira.screens.game;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Logger;
import com.foliveira.WumpusGame;
import com.foliveira.config.GameConfig;
import com.foliveira.entities.Background;
import com.foliveira.entities.Player;
import com.foliveira.enviroment.Room;

public class GameController {

    private static final Logger log = new Logger(GameScreen.class.getName(), Logger.DEBUG);

    private Player player;
    private Array<Room> rooms = new Array<Room>();
    private Background background;
    private float scoreTimer;
    private int score;
    private int displayScore;
    private Sound hit;
    private final WumpusGame game;
    private final AssetManager assetManager;

    private final float startPlayerX = (GameConfig.WORLD_WIDTH - GameConfig.PLAYER_SIZE) / 2f;
    private final float startPlayerY = 1 - GameConfig.PLAYER_SIZE / 2f;


    //constructor
    public GameController(WumpusGame game) {
        this.game = game;
        assetManager = game.getAssetManager();
        //init();
    }
}
