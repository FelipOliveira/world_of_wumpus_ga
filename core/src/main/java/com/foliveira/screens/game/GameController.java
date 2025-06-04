package com.foliveira.screens.game;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.Logger;
import com.foliveira.WumpusGame;
import com.foliveira.commom.PathFinder;
import com.foliveira.config.GameConfig;
import com.foliveira.config.Messages;
import com.foliveira.entities.AbstractWorldObject;
import com.foliveira.entities.Agent;
import com.foliveira.entities.Arrow;
import com.foliveira.entities.Background;
import com.foliveira.entities.Empty;
import com.foliveira.entities.GameStatus;
import com.foliveira.entities.Gold;
import com.foliveira.entities.Pit;
import com.foliveira.entities.Wall;
import com.foliveira.entities.World;
import com.foliveira.entities.Wumpus;

import java.util.Random;

public class GameController {

    private static final Logger log = new Logger(GameScreen.class.getName(), Logger.DEBUG);
    private Background background;
    private float scoreTimer;
    private int score;
    private int displayScore;
    private Sound hit;
    private final WumpusGame game;
    private final AssetManager assetManager;

    World world;
    Agent agent;
    Gold gold;
    Arrow arrow;
    int action;
    Wumpus wumpus;
    AbstractWorldObject[][] map;

    String statusMessage = "";
    String agentMessage;
    String infoBarMessage = "";

    GameStatus status;

    private final Random random = new Random();

    //constructor
    public GameController(WumpusGame game) {
        this.game = game;
        assetManager = game.getAssetManager();
        init();
    }

    private void init(){
        world = new World(GameConfig.MAP_SIZE);
        // create a new empty map
        map = new AbstractWorldObject[GameConfig.MAP_SIZE][GameConfig.MAP_SIZE];
        for (int i = 0; i<GameConfig.MAP_SIZE; i++){
            for (int j = 0; j<GameConfig.MAP_SIZE; j++){
                if (i==0 || i==GameConfig.MAP_SIZE -1 || j==0 || j==GameConfig.MAP_SIZE -1) map[i][j] = new Wall();
                else map[i][j] = new Empty();
            }
        }

        agent = new Agent(1,1,0);
        arrow = new Arrow(agent.getX(),agent.getY());
        map[agent.getX()][agent.getY()] = agent;

        agentMessage = agent.actionSense;

        int wumpusX, wumpusY;

        boolean validMap = true;
        while (validMap) { // populate the map
            wumpus = new Wumpus(true);
            do {
                int x = random.nextInt(GameConfig.MAP_SIZE);
                int y = random.nextInt(GameConfig.MAP_SIZE);
                if (map[x][y] instanceof Empty && (x != 1 && y != 1)) {
                    map[x][y] = wumpus;
                    wumpusX = x;
                    wumpusY = y;
                    break;
                }
            } while (true);

            // put the pits
            int numPits = 3;
            for (int i = 0; i < numPits; i++) {
                do {
                    int x = random.nextInt(GameConfig.MAP_SIZE);
                    int y = random.nextInt(GameConfig.MAP_SIZE);
                    if (map[x][y] instanceof Empty && (x != 1 && y != 1)) {
                        map[x][y] = new Pit();
                        break;
                    }
                } while (true);
            }

            // put the gold
            do {
                int x = random.nextInt(GameConfig.MAP_SIZE);
                int y = random.nextInt(GameConfig.MAP_SIZE);
                if (map[x][y] instanceof Empty && (x != 1 && y != 1)) {
                    gold = new Gold(x, y, false);
                    break;
                }
            } while (true);

            validMap = PathFinder.hasValidPathToGold(agent.getX(), agent.getY(), gold.getX(), gold.getY(), map) &&
                PathFinder.hasValidPathToWumpus(agent.getX(), agent.getY(), wumpusX, wumpusY, map);
        }
        status = GameStatus.PLAYING;
        display();
        displayPercepts();
    }

    public void display() {
        for (int i = 0; i<GameConfig.MAP_SIZE; i++){
            for (int j = 0; j<GameConfig.MAP_SIZE; j++){
                if (i == gold.getX() && j == gold.getY() && !gold.isTaken()) {
                    System.out.print(" G ");
                } else {
                    map[i][j].display();
                }
            }
            if (i == 0) System.out.print("\tACTIONS:");
            if (i == 1) System.out.print("\t[LEFT] TURN LEFT");
            if (i == 2) System.out.print("\t[RIGHT] TURN RIGHT");
            if (i == 3) System.out.print("\t[UP] MOVE FORWARD");
            else if (i == 4) System.out.print("\t[Z] SHOOT ARROW");
            else if (i == 5) System.out.print("\t[X] GRAB GOLD");
            System.out.println();
        }
    }

    public void displayPercepts() {
        int percepts = agent.getPassivePercepts(map, gold);
        System.out.print("You feel...");
        statusMessage = Messages.YOU_FEEL;
        if ((percepts & GameConfig.BREEZE) != 0) {
            System.out.print("[BREEZE] ");
            statusMessage = statusMessage.concat(Messages.BREEZE);
        }
        if ((percepts & GameConfig.STENCH) != 0) {
            System.out.print("[STENCH] ");
            statusMessage = statusMessage.concat(Messages.STENCH);
            infoBarMessage = Messages.WUMPUS_NEARBY_INFO;
        }
        if ((percepts & GameConfig.GLITTER) != 0) {
            System.out.print("[GLITTER] ");
            statusMessage = statusMessage.concat(Messages.GLITTER);
            infoBarMessage = Messages.GLITTER_INFO;
        }
        if (percepts == 0) {
            System.out.print("[NOTHING]");
            statusMessage = statusMessage.concat(Messages.NOTHING);
            infoBarMessage = agent.isHasGold() ? Messages.GOT_GOLD_INFO : Messages.INITIAL_MESSAGE_INFO;
        }
        System.out.println();
    }

    public void processAction(int actionCode) {
        action = actionCode;
        validate();

    }

    public void restart() {
        init();
    }

    private void validate() {

            switch (action) {
                case 1:
                    agent.move(map);
                    break;
                case 2:
                    agent.turnLeft();
                    break;
                case 3:
                    agent.turnRight();
                    break;
                case 4:
                    agent.grabGold(gold);
                    break;
                case 5:
                    agent.shootArrow(arrow, wumpus, map);
                    break;
            }

        tick();
    }

    private void tick() {
        isAgentWon();
        isGameOver();
        if (status == GameStatus.WON || status == GameStatus.GAMEOVER) {
            gameOver();
        }
        if (status == GameStatus.PLAYING) {
            display();
            displayPercepts();
        }
    }

    private void isAgentWon(){
        if (agent.getX() == 1 && agent.getY() == 1 && agent.isAlive() && agent.isHasGold())
            status = GameStatus.WON;
    }

    private void gameOver() {
        System.out.println("Game Over. Your score is " + agent.getScore() + "\nPress [R] to reset");
        statusMessage = Messages.GAME_OVER_MESSAGE + agent.getScore() + Messages.RESET_MESSAGE;
    }

    public void isGameOver() {
        if(!agent.isAlive()) status = GameStatus.GAMEOVER;
    }

    public void update(float delta) {
        //processAction();
    }

}
