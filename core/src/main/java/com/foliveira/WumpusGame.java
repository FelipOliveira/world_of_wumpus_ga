package com.foliveira;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Logger;
import com.badlogic.gdx.utils.ScreenUtils;
import com.foliveira.utils.GdxUtils;
import com.foliveira.utils.manager.GameState;
import com.foliveira.utils.manager.GameStateManager;

import java.util.Random;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class WumpusGame extends Game implements GameStateManager.StateListener{
    //private Stage stage;
    //private Skin skin;

    private AssetManager assetManager;
    private SpriteBatch batch;

    //=========================================================================

    // world elements
    static final int EMPTY = 0;
    static final int PIT = 1;
    static final int WUMPUS = 2;
    static final int GOLD = 3;
    static final int AGENT = 4;
    static final int ARROW = 5;
    static final int WALL = 6;

    // agent perception
    static final int BREEZE = 1;
    static final int STENCH = 2;
    static final int GLITTER = 4;
    static final int BUMP = 8;
    static final int SCREAM = 16;

    // world variables
    static int[][] world;
    static int agentX, agentY;
    static int agentDirection; // (0: right, 1: down, 2: left, 3: up)
    static boolean hasArrow = true;
    static boolean wumpusAlive = true;
    static boolean agentAlive = true;
    static int score = 0;
    static int worldSize = 6;
    static boolean hasGold = false;
    //static boolean lockScreen = false;
    static Random random = new Random();
    static GameStateManager manager;


    //=========================================================================

    @Override
    public void create() {
        Gdx.app.setLogLevel(Application.LOG_DEBUG);

        assetManager = new AssetManager();
        assetManager.getLogger().setLevel(Logger.DEBUG);
        batch = new SpriteBatch();


        //setScreen(new LoadingScreen(this));



        /*stage = new Stage(new FitViewport(640, 480));
        skin = new Skin(Gdx.files.internal("ui/uiskin.json"));

        Window window = new Window("Example screen", skin, "border");
        window.defaults().pad(4f);
        window.add("This is a simple Scene2D view.").row();
        final TextButton button = new TextButton("Click me!", skin);
        button.pad(8f);
        button.addListener(new ChangeListener() {
            @Override
            public void changed(final ChangeEvent event, final Actor actor) {
                button.setText("Clicked.");
            }
        });
        window.add(button);
        window.pack();
        // We round the window position to avoid awkward half-pixel artifacts.
        // Casting using (int) would also work.
        window.setPosition(MathUtils.roundPositive(stage.getWidth() / 2f - window.getWidth() / 2f),
            MathUtils.roundPositive(stage.getHeight() / 2f - window.getHeight() / 2f));
        window.addAction(Actions.sequence(Actions.alpha(0f), Actions.fadeIn(1f)));
        stage.addActor(window);

        Gdx.input.setInputProcessor(stage);*/

        //=========================================================================

        init();
        //=========================================================================
    }

    @Override
    public void render() {
        GdxUtils.clearScreen();
        //=========================================================================
        processAction();



        //=========================================================================

        /*stage.act(Gdx.graphics.getDeltaTime());
        stage.draw();*/
    }

    @Override
    public void resize(int width, int height) {
        /*stage.getViewport().update(width, height);*/
    }

    @Override
    public void dispose() {
        /*stage.dispose();
        skin.dispose();*/
        assetManager.dispose();
        batch.dispose();
    }

    public AssetManager getAssetManager() {
        return assetManager;
    }

    public SpriteBatch getBatch() {
        return batch;
    }
//=============================================================================================

    public void init() {

        // create the world
        world = new int[worldSize][worldSize];
        for (int i=0;i<worldSize;i++){
            for (int j=0;j<worldSize;j++){
                world[i][j] = EMPTY;
            }
        }

        // put the walls
        for (int i=0;i<worldSize;i++){
            world[i][0] = WALL;
            world[i][worldSize-1] = WALL;
            world[0][i] = WALL;
            world[worldSize-1][i] = WALL;
        }

        // put the wumpus
        do {
            int x = random.nextInt(worldSize);
            int y = random.nextInt(worldSize);
            if (world[x][y] == EMPTY && (x != 1 && y != 1)){
                world[x][y] = WUMPUS;
                break;
            }
        } while (true);

        // put the gold
        do {
            int x = random.nextInt(worldSize);
            int y = random.nextInt(worldSize);
            if (world[x][y] == EMPTY && (x != 1 && y != 1)){
                world[x][y] = GOLD;
                break;
            }
        } while (true);

        // put the pits
        int numPits = 3;
        for (int i=0;i<numPits;i++){
            do {
                int x = random.nextInt(worldSize);
                int y = random.nextInt(worldSize);
                if (world[x][y] == EMPTY && (x != 1 && y != 1)){
                    world[x][y] = PIT;
                    break;
                }
            } while (true);
        }

        // put the agent in the start position
        agentX = 1;
        agentY = 1;
        agentDirection = 1;
        world[agentX][agentY] = AGENT;

        manager = new GameStateManager(GameState.DISPLAY);

        manager.addListener(this);
        System.out.println("World of Wumpus");
        tick();
    }

    static void displayWorld(){
        for (int i=0;i<worldSize;i++){
            for (int j=0;j<worldSize;j++){
                switch (world[i][j]){
                    case EMPTY:
                        System.out.print("   ");
                        break;
                    case PIT:
                        System.out.print(" P ");
                        break;
                    case WUMPUS:
                        System.out.print(" W ");
                        break;
                    case GOLD:
                        System.out.print(" G ");
                        break;
                    case AGENT:
                        switch (agentDirection) {
                            case 0:
                                System.out.print(" > ");
                                break;
                            case 1:
                                System.out.print(" v ");
                                break;
                            case 2:
                                System.out.print(" < ");
                                break;
                            case 3:
                                System.out.print(" ^ ");
                                break;
                        }break;
                    case ARROW:
                        System.out.print(" A ");
                        break;
                    case WALL:
                        System.out.print(" # ");
                        break;
                    default:
                        System.out.print(" ? ");
                        break;
                }
            }
            if (i == 0) System.out.print("\tActions");
            if (i == 1) System.out.print("\t[ARROW KEYS] move");
            else if (i == 2) System.out.print("\t[Z] shoot arrow");
            else if (i == 3) System.out.print("\t[X] grab gold");
            System.out.println();
        }
    }

    static void displayPercepts() {
        int percepts = getPercepts();
        System.out.print("You feel... ");
        if ((percepts & BREEZE) != 0) System.out.print("[breeze] ");
        if ((percepts & STENCH) != 0) System.out.print("[stench] ");
        if ((percepts & GLITTER) != 0) System.out.print("[glitter] ");
        if ((percepts & BUMP) != 0) System.out.print("[bump] ");
        if ((percepts & SCREAM) != 0) System.out.print("[scream] ");
        if (percepts == 0) System.out.print("[nothing]");
        System.out.println();
    }

    static int getPercepts() {
        int percepts = 0;
        int x = agentX;
        int y = agentY;

        // check pit's breeze (adjacent)
        if (world[x+1][y] == PIT ||
            world[x-1][y] == PIT ||
            world[x][y-1] == PIT ||
            world[x][y+1] == PIT)
            percepts |= BREEZE;

        // check wumpus's stench (adjacent)
        if (world[x+1][y] == WUMPUS ||
            world[x-1][y] == WUMPUS ||
            world[x][y-1] == WUMPUS ||
            world[x][y+1] == WUMPUS)
            percepts |= STENCH;

        // check gold's glitter
        if (world[x][y] == GOLD) percepts |= GLITTER;

        return percepts;
    }

    static void processAction() {
        if (agentAlive) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) {
                rotate(agentX, agentY - 1, 2);
                tick();
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
                rotate(agentX, agentY + 1, 0);
                tick();
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
                rotate(agentX - 1, agentY, 3);
                tick();
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
                rotate(agentX + 1, agentY, 1);
                tick();
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.Z)) {
                shootArrow();
                tick();
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.X)) {
                grabGold();
                tick();
            }
        }

    }

    static void move(int newX, int newY) {
        score -= 2;
        if (world[newX][newY] == WALL) {
            System.out.println("You hit a wall");
        }else {
            if (world[newX][newY] == GOLD) {
                System.out.println("You found gold!");
                score += 1000;
                hasGold = true;
            } else if (world[newX][newY] == PIT) {
                System.out.println("You fell on a pit!");
                agentAlive = false;
            } else if (world[newX][newY] == WUMPUS) {
                System.out.println("You got caught by the Wumpus!");
                agentAlive = false;
            }
            world[agentX][agentY] = EMPTY;
            agentX = newX;
            agentY = newY;
            world[agentX][agentY] = AGENT;
        }
    }

    static void rotate(int newX, int newY, int newAgentDirection) {
        if (agentDirection == newAgentDirection) {
            move(newX, newY);
        }
        else {
            score -= 1;
            agentDirection = newAgentDirection;
        }
    }

    static void moveArrow() {

    }

    static void shootArrow() {
        if (hasArrow) {
            //hasArrow = false;
            score -= 10;
            System.out.println("You shot the arrow!");
            int x = agentX;
            int y = agentY;

            switch (agentDirection) {
                case 0: // right
                    for (int i=x;i<=worldSize;i++) {
                        if (world[x][i] == WUMPUS) {
                            System.out.println("You hear a [SCREAM]!");
                            wumpusAlive = false;
                            world[x][i] = EMPTY;
                            score += 500;
                            return;
                        }
                        if (world[x][i] == WALL) {
                            System.out.println("Your arrow hit the wall!");
                            if (world[x][i-1] == EMPTY){
                                world[x][i-1] = ARROW;
                            }
                            return;
                        }
                    }
                    break;
                case 1: // down
                    for (int i=y;i<worldSize;i++) {
                        if (world[i][y] == WUMPUS) {
                            System.out.println("You hear a [SCREAM]!");
                            wumpusAlive = false;
                            world[i][y] = EMPTY;
                            score += 500;
                            return;
                        }
                        if (world[i][y] == WALL) {
                            System.out.println("Your arrow hit the wall!");
                            if (world[i-1][y] == EMPTY){
                                world[i-1][y] = ARROW;
                            }
                            return;
                        }
                    }
                    break;
                case 2: // left
                    for (int i=x;i>0;i--) {
                        if (world[x][i] == WUMPUS) {
                            System.out.println("You hit the Wumpus!");
                            wumpusAlive = false;
                            world[x][i] = EMPTY;
                            score += 500;
                            return;
                        }
                        if (world[x][i] == WALL) {
                            System.out.println("Your arrow hit the wall!");
                            if (world[x][i+1] == EMPTY){
                                world[x][i+1] = ARROW;
                            }
                            return;
                        }
                    }
                    break;
                case 3: // up
                    for (int i=y;i>=0;i--) {
                        if (world[i][y] == WUMPUS) {
                            System.out.println("You hear a [SCREAM]!");
                            wumpusAlive = false;
                            world[i][y] = EMPTY;
                            score += 500;
                            return;
                        }
                        if (world[i][y] == WALL) {
                            System.out.println("Your arrow hit the wall!");
                            if (world[i+1][y] == EMPTY){
                                world[i+1][y] = ARROW;
                            }
                            return;
                        }
                    }
                    break;
            }
            System.out.println("You lose your arrow!");
        } else {
            System.out.println("Your have no arrow left");
        }
    }

    static void grabGold() {
        if (world[agentX][agentY] == GOLD) {
            System.out.println("You found gold!");
            score += 1000;
            world[agentX][agentY] = EMPTY;
        } else {
            System.out.println("You found nothing");
        }
    }

    static void gameOver() {
        System.out.println("Game Over. Your score is " + score);
    }

    static void tick() {
        if (isAgentWon() || !agentAlive) gameOver();
        if (agentAlive) {
            displayWorld();
            displayPercepts();
        }
    }

    static boolean isAgentWon(){
        return agentX == 1 && agentY == 1 && agentAlive && hasGold;
    }

    @Override
    public void onStageChanged(GameState newState) {
        switch (manager.getCurrentState()){
            case DISPLAY:
                displayWorld();
            case SENSE:
                displayPercepts();
                break;
            case TRAP:

                break;
            case ACTION:

                break;
            case MOVE:

                break;
        }
    }


}
