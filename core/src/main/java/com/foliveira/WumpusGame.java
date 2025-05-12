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

import java.util.Random;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class WumpusGame extends Game {
    //private Stage stage;
    //private Skin skin;

    private AssetManager assetManager;
    private SpriteBatch batch;

    //=========================================================================
    int[][] cave = new int[][]{
        {0, 0, 0, 0},
        {0, 0, 0, 0},
        {0, 0, 0, 0},
        {0, 0, 0, 0}
    };
    int[] player = new int[] {0,0};
    int[] exit = new int[] {0,0};
    int[] wumpus;
    int[] gold;
    int[] pit1;
    int[] pit2;
    int[] pit3;
    Array<int[]> breeze = new Array<>();
    Array<int[]> stench = new Array<>();
    boolean _hasGold = false;
    boolean playerIsAlive = true;
    boolean playing = true;

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
    static int score = 0;
    static int worldSize = 6;
    static boolean hasGold = false;
    static boolean lockScreen = false;
    static Random random = new Random();


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
        /*cave = new int[][]{
            {0, 0, 0, 0},
            {0, 0, 0, 0},
            {0, 0, 0, 0},
            {0, 0, 0, 0}
        };*/

        //player = new int[] {0,0};
        //gold = new int[] {2,1};
        //wumpus = new int[] {2,0};
        populate();

        stench.add(
            new int[] {wumpus[0] - 1, wumpus[1]},
            new int[] {wumpus[0], wumpus[1] - 1},
            new int[] {wumpus[0] + 1, wumpus[1]},
            new int[] {wumpus[0], wumpus[1] + 1}
        );

        //printCave();

        init();
        //=========================================================================
    }

    @Override
    public void render() {
        ScreenUtils.clear(0f, 0f, 0f, 1f);
        //=========================================================================
        if (isOverlap(player, wumpus) || isOverlapPits(player)) lose();
        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN) && player[0] + 1 < cave.length && playing) {
            player[0] += 1;
            printCave();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP) && player[0] - 1 >= 0 && playing) {
            player[0] -= 1;
            printCave();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT) && player[1] + 1 < cave.length && playing) {
            player[1] += 1;
            printCave();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT) && player[1] - 1 >= 0 && playing) {
            player[1] -= 1;
            printCave();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) && isOverlapGold(player) && playing) {
            hasGold = true;
        }

        if (isOverlap(player, exit) && hasGold) win();

        if (Gdx.input.isKeyJustPressed(Input.Keys.R) && !playing) resetGame();

        if (!lockScreen) {
            displayPercepts();
            processAction();
            if (isGameOver()) {
                System.out.println("Game Over");
                System.out.println("your score is " + score);
            }
            lockScreen = false;
        }

        //=========================================================================

        /*stage.act(Gdx.graphics.getDeltaTime());
        stage.draw();*/
    }

    private void resetGame() {
        playerIsAlive = true;
        playing = true;
        populate();
    }

    private void lose() {
        playing = false;
        playerIsAlive = false;
        System.out.println("You lose... Press R to restart");
    }

    private void win() {
        playing = false;
        System.out.println("You win!");
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
    public void printCave(){
        for (int lin=0; lin< cave.length; lin++){
            for (int col=0; col< cave.length; col++){
                if (player[0] == lin && player[1] == col) System.out.print(" A ");
                else if (gold[0] == lin && gold[1] == col && !hasGold) System.out.print(" G ");
                else if (wumpus[0] == lin && wumpus[1] == col) System.out.print(" W ");
                else if (wumpus[0] - 1 == lin && wumpus[1] == col) System.out.print(" S ");
                else if (wumpus[0] == lin && wumpus[1] -1 == col) System.out.print(" S ");
                else if (wumpus[0] + 1 == lin && wumpus[1] == col) System.out.print(" S ");
                else if (wumpus[0] == lin && wumpus[1] + 1 == col) System.out.print(" S ");
                else if (pit1[0] == lin && pit1[1] == col) System.out.print(" P ");
                else if (pit2[0] == lin && pit2[1] == col) System.out.print(" P ");
                else if (pit3[0] == lin && pit3[1] == col) System.out.print(" P ");
                else System.out.print(" " + 0 + " ");
            }
            System.out.print("\n");
        }
        System.out.print("player:[" + player[0] + "][" + player[1] + "]\n");
        System.out.print("[" + pit1[0] + "][" + pit1[1] + "]\n");
        System.out.print("[" + pit2[0] + "][" + pit2[1] + "]\n");
        System.out.print("[" + pit3[0] + "][" + pit3[1] + "]");
        System.out.print("\n");
    }

    private void addWumpus(){
        wumpus = new int[] {MathUtils.random(1, cave.length - 1), MathUtils.random(1, cave.length - 1)};
    }

    private int[] addPit(){
        return new int[] {MathUtils.random(1, cave.length - 1), MathUtils.random(1, cave.length - 1)};
    }

    private void addGold() {
        gold = new int[] {MathUtils.random(1, cave.length - 1), MathUtils.random(1, cave.length - 1)};
    }

    private boolean isOverlapGold(int[] obj) {
        return gold[0] == obj[0] && gold[1] == obj[1];
    }

    private boolean isOverlap(int[] obj1, int[] obj2) {
        return obj1[0] == obj2[0] && obj1[1] == obj2[1];
    }

    private boolean isOverlapWumpus(int[] obj) {
        return wumpus[0] == obj[0] && wumpus[1] == obj[1];
    }

    private boolean isOverlapPits(int[] obj) {
        return pit1[0] == obj[0] && pit1[1] == obj[1]
            || pit2[0] == obj[0] && pit2[1] == obj[1]
            || pit3[0] == obj[0] && pit3[1] == obj[1];
    }

    public boolean limitCave(int[] loc){
        return loc[0] >= 0 && loc[0] < cave.length && loc[1] >= 0 && loc[1] < cave.length ;
    }

    private void populate() {
        addPlayer();
        addWumpus();

        do {
            addGold();
        } while (isOverlap(gold, wumpus));

        do {
            pit1 = addPit();
        } while (isOverlap(pit1, wumpus) || isOverlap(pit1, gold));

        do {
            pit2 = addPit();
        } while (isOverlap(pit2, wumpus) || isOverlap(pit2, gold) || isOverlap(pit2, pit1));

        do {
            pit3 = addPit();
        } while (isOverlap(pit3, wumpus) || isOverlap(pit3, gold) || isOverlap(pit3, pit1) || isOverlap(pit3, pit2));

    }

    private void addPlayer() {
        player = new int[] {0,0};
    }

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

        displayWorld();
    }

    static void displayWorld(){
        System.out.println("\nMundo de Wumpus:");
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
            System.out.println();
        }
    }

    static void displayPercepts() {
        if (lockScreen) {
            int percepts = getPercepts();
            System.out.print("\nYou feel... ");
            if ((percepts & BREEZE) != 0) System.out.print("breeze, ");
            if ((percepts & STENCH) != 0) System.out.print("stench, ");
            if ((percepts & GLITTER) != 0) System.out.print("glitter, ");
            if ((percepts & BUMP) != 0) System.out.print("bump, ");
            if ((percepts & SCREAM) != 0) System.out.print("scream, ");
            if ((percepts & BUMP) != 0) System.out.print("bump, ");
            if (percepts == 0) System.out.print("nothing...");
            System.out.print(".");
        }
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
        System.out.println("Actions:\n" +
            "[LEFT] turn left\n" +
            "[RIGHT] turn right\n" +
            "[UP] move forward\n" +
            "[A] shoot arrow\n" +
            "[S] grab gold");

        if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) {
            turnLeft();
            displayWorld();
        }
        else if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
            turnRight();
            displayWorld();
        }
        else if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
            moveForward();
            displayWorld();
        }
        else if (Gdx.input.isKeyJustPressed(Input.Keys.A)) {
            shootArrow();
            displayWorld();
        }
        else if (Gdx.input.isKeyJustPressed(Input.Keys.S)) {
            grabGold();
            displayWorld();
        }

    }

    static void turnLeft() {
        agentDirection = (agentDirection + 3) % 4;
        score -= 1;
    }

    static void turnRight() {
        agentDirection = (agentDirection + 1) % 4;
    }

    static void moveForward() {
        int newX = agentX;
        int newY = agentY;

        switch (agentDirection) {
            case 0: //right
                newX++;
                break;
            case 1: //down
                newY++;
                break;
            case 2: //left
                newX--;
                break;
            case 3: //up
                newY--;
                break;
        }
        score -= 1;

        if (world[newX][newY] != WALL) {
            world[agentX][agentY] = EMPTY;
            agentX = newX;
            agentY = newY;
            world[agentX][agentY] = AGENT;
            if (world[agentX][agentY] == PIT) {
                System.out.println("You fell on a pit!");
            } else if (world[agentX][agentY] == WUMPUS) {
                System.out.println("You got caught by the Wumpus!");
            }
        } else {
            System.out.println("You hit a wall");
            displayPercepts();
        }
    }

    static void shootArrow() {
        if (hasArrow) {
            hasArrow = false;
            score -= 10;
            System.out.println("You shot the arrow!");
            int x = agentX;
            int y = agentY;

            switch (agentDirection) {
                case 0: // right
                    for (int i=x+1;i<worldSize;i++) {
                        if (world[i][y] == WUMPUS) {
                            System.out.println("You hit the Wumpus!");
                            wumpusAlive = false;
                            world[i][y] = EMPTY;
                            score += 500;
                            return;
                        }
                        if (world[i][y] == WALL) {
                            System.out.println("Your arrow hit the wall!");
                            return;
                        }
                    }
                    break;
                case 1: // down
                    for (int i=y+1;i<worldSize;i++) {
                        if (world[x][i] == WUMPUS) {
                            System.out.println("You hit the Wumpus!");
                            wumpusAlive = false;
                            world[x][i] = EMPTY;
                            score += 500;
                            return;
                        }
                        if (world[x][i] == WALL) {
                            System.out.println("Your arrow hit the wall!");
                            return;
                        }
                    }
                    break;
                case 2: // left
                    for (int i=x-1;i>0;i--) {
                        if (world[i][y] == WUMPUS) {
                            System.out.println("You hit the Wumpus!");
                            wumpusAlive = false;
                            world[i][y] = EMPTY;
                            score += 500;
                            return;
                        }
                        if (world[i][y] == WALL) {
                            System.out.println("Your arrow hit the wall!");
                            return;
                        }
                    }
                    break;
                case 3: // up
                    for (int i=y-1;i>=0;i--) {
                        if (world[x][i] == WUMPUS) {
                            System.out.println("You hit the Wumpus!");
                            wumpusAlive = false;
                            world[x][i] = EMPTY;
                            score += 500;
                            return;
                        }
                        if (world[x][i] == WALL) {
                            System.out.println("Your arrow hit the wall!");
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

    static boolean isGameOver() {
        if (world[agentX][agentY] == PIT || world[agentX][agentY] == WUMPUS) return true;
        return false;
    }
}
