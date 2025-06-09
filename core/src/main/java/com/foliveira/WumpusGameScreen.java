package com.foliveira;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.foliveira.config.GameConfig;
import com.foliveira.config.Messages;
import com.foliveira.entities.GameStatus;
import com.foliveira.utils.GdxUtils;
import com.foliveira.utils.debug.DebugCameraController;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Set;

public class WumpusGameScreen extends ApplicationAdapter {
    private static final int WORLD_SIZE = 10;
    private static final int NUM_PITS = 5;
    private static final int NUM_BATS = 2;
    private static final int NUM_WUMPUS = 1;
    private static final int NUM_ARROWS = 1;
    private static final int VIRTUAL_WIDTH = 320;
    private static final int VIRTUAL_HEIGHT = 240;

    private enum Direction {
        NORTH, EAST, SOUTH, WEST
    }
    private Direction playerDirection;
    private Texture grassTexture;
    private Texture playerTexture;
    private Texture wumpusTexture;
    private Texture pitTexture;
    private Texture batTexture;
    private Texture goldTexture;
    private Texture stenchTexture;
    private Texture breezeTexture;
    private Texture glitterTexture;
    private Texture arrowTexture;
    private Texture gameOverTexture;
    private Texture gameWonTexture;
    private Texture roomFloorTexture;
    private Texture wallTexture;
    private Texture passageTexture;
    private SpriteBatch batch;
    private OrthographicCamera camera;
    private DebugCameraController debugCameraController;
    private Viewport viewport;
    private BitmapFont font;
    private Stage stage;
    private Skin skin;
    private Label logLabel;
    private ScrollPane logScrollPane;
    private ScrollingLabel infoBarLabel;
    private TextButton restartButton;
    private char[][] world;
    private int playerX, playerY;
    private int wumpusX, wumpusY;
    private Array<Vector2> pitPositions;
    private Array<Vector2> batPositions;
    private int goldX, goldY;
    private enum GameState {
        PLAYING, GAME_OVER, GAME_WON
    }
    private GameState gameState;
    private boolean hasGold = false;
    private int arrowsLeft = NUM_ARROWS;
    private final Random random = new Random();
    private boolean wumpusAlive = true;

    @Override
    public void create() {
        camera = new OrthographicCamera();
        viewport = new FitViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camera);
        viewport.apply();

        camera.position.set(viewport.getWorldWidth() / 2, viewport.getWorldHeight() / 2, 0);
        camera.update();

        debugCameraController = new DebugCameraController();
        debugCameraController.setStartPosition((float) VIRTUAL_WIDTH /2, (float) VIRTUAL_HEIGHT /2);

        batch = new SpriteBatch();
        font = new BitmapFont();
        font.setColor(Color.WHITE);
        font.getData().setScale(0.5f);
        //loadTextures();

        skin = new Skin(Gdx.files.internal("ui/uiskin.json"));
        stage = new Stage(viewport, batch);
        Gdx.input.setInputProcessor(stage);

        setupUI();

        initializeWorld();
        gameState = GameState.PLAYING;
        playerDirection = Direction.EAST;
        appendToLog(Messages.WELCOME_LOG);
        updatePerceptions();
        updateInfoBar(Messages.INITIAL_MESSAGE_INFO);
    }

    private void setupUI() {
        Table rootTable = new Table(skin);
        rootTable.setFillParent(true);
        rootTable.debug();

        Table logTable = new Table(skin);
        //logTable.setBackground("default-rect");
        logLabel = new Label("", skin);
        logLabel.setWrap(true);
        logScrollPane = new ScrollPane(logLabel, skin);
        logScrollPane.setFadeScrollBars(false);
        logScrollPane.setScrollingDisabled(true, false);
        logTable.add(logScrollPane).expand().fill().pad(2);

        rootTable.add(logTable).height(VIRTUAL_HEIGHT * 0.25f).expandX().fillX().row();

        Table centerTable = new Table(skin);
        //centerTable.setBackground("default-rect");
        rootTable.add(centerTable).expand().fill().row();

        Table leftButtons = new Table(skin);
        leftButtons.defaults().pad(2).width(60).height(40);
        leftButtons.align(Align.center);
        TextButton moveButton = new TextButton("MOVE", skin);
        moveButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
               if (gameState == GameState.PLAYING) moveForward();
            }
        });
        TextButton turnLeftButton = new TextButton("TURN LEFT", skin);
        turnLeftButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (gameState == GameState.PLAYING) turnLeft();
            }
        });
        TextButton turnRightButton = new TextButton("TURN RIGHT", skin);
        turnRightButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (gameState == GameState.PLAYING) turnRight();
            }
        });
        leftButtons.add("").row();
        leftButtons.add(moveButton).row();
        leftButtons.add(turnLeftButton).padRight(5);
        leftButtons.add(turnRightButton).padLeft(5).row();
        leftButtons.add("").row();

        centerTable.add(leftButtons).width(VIRTUAL_WIDTH * 0.2f).expandY().fillY();

        centerTable.add().expand().fill();

        Table rightButtons = new Table(skin);
        rightButtons.defaults().pad(2).width(60).height(40);
        rightButtons.align(Align.center);
        TextButton searchButton = new TextButton("SEARCH", skin);
        searchButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (gameState == GameState.PLAYING) searchForGold();
            }
        });
        TextButton specialButton = new TextButton("SPECIAL", skin);
        specialButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (gameState == GameState.PLAYING) shootArrow();
            }
        });
        rightButtons.add("").row();
        rightButtons.add(specialButton).row();
        rightButtons.add(searchButton).row();
        rightButtons.add("").row();

        centerTable.add(rightButtons).width(VIRTUAL_WIDTH * 0.2f).expandY().fillY();

        infoBarLabel = new ScrollingLabel(Messages.INFO, skin, "default", Color.WHITE, 20f);

        rootTable.add(infoBarLabel).height((VIRTUAL_HEIGHT) * 0.1f).expandX().fillX().pad(2).row();

        restartButton = new TextButton("RESTART", skin);
        restartButton.setVisible(false);
        restartButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                initializeWorld();
                gameState = GameState.PLAYING;
                hasGold = false;
                arrowsLeft = NUM_ARROWS;
                wumpusAlive = true;
                playerDirection = Direction.EAST;
                restartButton.setVisible(false);
                logLabel.setText("");
                appendToLog(Messages.WELCOME_LOG);
                updatePerceptions();
                updateInfoBar(Messages.INITIAL_MESSAGE_INFO);
            }
        });
        stage.addActor(restartButton);
        restartButton.setSize(100, 35);
        restartButton.setPosition(
            (float) VIRTUAL_WIDTH / 2 - restartButton.getWidth() / 2,
            (float) VIRTUAL_HEIGHT / 2 - restartButton.getHeight() / 2);

        stage.addActor(rootTable);
    }

    private void appendToLog(String message) {
        logLabel.setText(logLabel.getText().toString() + "\n- " + message);
        logScrollPane.scrollTo(0,0,0,0);
    }

    private void updateInfoBar(String message) {
        infoBarLabel.setText(Messages.INFO + message);
    }

    private void loadTextures(){
        try {
            grassTexture = new Texture(Gdx.files.internal(""));
            playerTexture = new Texture(Gdx.files.internal(""));
            wumpusTexture = new Texture(Gdx.files.internal(""));
            pitTexture = new Texture(Gdx.files.internal(""));
            batTexture = new Texture(Gdx.files.internal(""));
            goldTexture = new Texture(Gdx.files.internal(""));
            stenchTexture = new Texture(Gdx.files.internal(""));
            breezeTexture = new Texture(Gdx.files.internal(""));
            glitterTexture = new Texture(Gdx.files.internal(""));
            arrowTexture = new Texture(Gdx.files.internal(""));
            gameOverTexture = new Texture(Gdx.files.internal(""));
            gameWonTexture = new Texture(Gdx.files.internal(""));

            roomFloorTexture = new Texture(Gdx.files.internal(""));
            wallTexture = new Texture(Gdx.files.internal(""));
            passageTexture = new Texture(Gdx.files.internal(""));
        } catch (Exception e) {
            Gdx.app.error(WumpusGameScreen.class.getName(), "Error to load textures: " + e.getMessage());
            Gdx.app.exit();
        }
    }

    public void initializeWorld(){
        world = new char[WORLD_SIZE][WORLD_SIZE];
        for (int i=0;i<WORLD_SIZE;i++) {
            for (int j=0;j<WORLD_SIZE;j++) {
                world[i][j] = ' ';
            }
        }

        playerX = 0;
        playerY = 0;
        world[playerX][playerX] = 'P';

        pitPositions = new Array<>();
        batPositions = new Array<>();
        boolean validLayout = false;
        while (!validLayout) {
            for (int i=0;i<WORLD_SIZE;i++) {
                for (int j=0;j<WORLD_SIZE;j++) {
                    world[i][j] = ' ';
                }
            }
            world[playerX][playerY] = 'P';
            wumpusX = -1;
            wumpusY = -1;
            pitPositions.clear();
            batPositions.clear();
            goldX = -1;
            goldY = -1;
            do {
                wumpusX = random.nextInt(WORLD_SIZE);
                wumpusY = random.nextInt(WORLD_SIZE);
            } while (wumpusX == 0 && wumpusY == 0);
            world[wumpusX][wumpusY] = 'W';
            for (int i=0;i<NUM_PITS;i++) {
                int x, y;
                do {
                    x = random.nextInt(WORLD_SIZE);
                    y = random.nextInt(WORLD_SIZE);
                } while ((x == 0 && y == 0) || (x == wumpusX && y == wumpusY) || isPitAt(x,y) || isBatAt(x,y));
                pitPositions.add(new Vector2(x, y));
                world[x][y] = 'H';
            }
            for (int i=0;i<NUM_BATS;i++) {
                int x, y;
                do {
                    x = random.nextInt(WORLD_SIZE);
                    y = random.nextInt(WORLD_SIZE);
                } while ((x == 0 && y == 0) || (x == wumpusX && y == wumpusY) || isPitAt(x,y) || isBatAt(x,y));
                batPositions.add(new Vector2(x, y));
                world[x][y] = 'B';
            }
            do {
                goldX = random.nextInt(WORLD_SIZE);
                goldY = random.nextInt(WORLD_SIZE);
            } while ((goldX == 0 && goldY == 0)|| (goldX == wumpusX && goldY == wumpusY) || isPitAt(goldX,goldY) || isBatAt(goldX,goldY));
            world[goldX][goldY] = 'G';
            validLayout = hasValidPathToGold() && hasValidPathToWumpus();
            if (!validLayout) {
                Gdx.app.log(WumpusGameScreen.class.getName(), "invalid layout, trying again...");
            }
            Gdx.app.log(WumpusGameScreen.class.getName(), "valid layout generated.");
        }
    }

    private boolean isPitAt(int x, int y) {
        for (Vector2 pit : pitPositions) {
            if (pit.x == x && pit.y == y) return true;
        }
        return false;
    }

    private boolean isBatAt(int x, int y) {
        for (Vector2 bat : batPositions) {
            if (bat.x == x && bat.y == y) return true;
        }
        return false;
    }

    public static class Node {
        int x, y;
        int gCost, hCost;
        Node parent;

        public Node(int x, int y) {
            this.x = x;
            this.y = y;
        }

        int fCost() {
            return gCost + hCost;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            Node node = (Node) obj;
            return x == node.x && y == node.y;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y);
        }
    }

    private int calculateHeuristic(int startX, int startY, int endX, int endY) {
        return Math.abs(endX - startX) + Math.abs(endY - startY);
    }

    private boolean hasValidPathToGold() {
        return findPath(playerX, playerY, goldX, goldY);
    }

    private  boolean hasValidPathToWumpus() {
        if (wumpusAlive) return true;
        return findPath(playerX, playerY, wumpusX, wumpusY);
    }

    private boolean findPath(int startX, int startY, int endX, int endY) {
        PriorityQueue<Node> openList = new PriorityQueue<>(Comparator.comparingInt(Node::fCost));
        Set<Node> closedList = new HashSet<>();

        Node startNode = new Node(startX, startY);
        startNode.hCost = calculateHeuristic(startX, startY, endX, endY);
        openList.add(startNode);

        while (!openList.isEmpty()) {
            Node currentNode = openList.poll();
            if (currentNode.x == endX && currentNode.y == endY) return true;

            closedList.add(currentNode);

            Array<Node> neighbors = getNeighbors(currentNode.x, currentNode.y);
            for (Node neighbor : neighbors) {
                if (closedList.contains(neighbor)) continue;
                int newGCost = currentNode.gCost + 1;
                if (!openList.contains(neighbor) || newGCost < neighbor.gCost) {
                    neighbor.gCost = newGCost;
                    neighbor.hCost = calculateHeuristic(neighbor.x, neighbor.y, endX, endY);
                    neighbor.parent = currentNode;
                    if (!openList.contains(neighbor)) {
                        openList.add(neighbor);
                    } else {
                        openList.remove(neighbor);
                        openList.add(neighbor);
                    }
                }
            }
        }
        return false;
    }

    private Array<Node> getNeighbors(int x, int y) {
        Array<Node> neighbors = new Array<>();
        int[] dx = {0,0,-1,1};
        int[] dy = {-1,1,0,0};

        for (int i=0;i<4;i++) {
            int newX = x + dx[i];
            int newY = y + dy[i];
            if (
                newX >= 0 && newX < WORLD_SIZE
                && newY >=0 && newY < WORLD_SIZE &&
                world[newX][newY] != 'H'
            ) neighbors.add(new Node(newX, newY));
        }
        return neighbors;
    }

    @Override
    public void render() {
        debugCameraController.inputDebugHandle(Gdx.graphics.getDeltaTime());
        debugCameraController.applyToCamera(camera);

        GdxUtils.clearScreen();

        camera.update();

        batch.setProjectionMatrix(camera.combined);

        batch.begin();
        //drawIsometricRoom();
        batch.end();

        stage.act(Gdx.graphics.getDeltaTime());
        stage.draw();

        restartButton.setVisible(gameState == GameState.GAME_OVER || gameState == GameState.GAME_WON);
    }

    private void drawIsometricRoom() {
        float gameAreaWidth = VIRTUAL_WIDTH * 0.6f;
        float gameAreaHeight = VIRTUAL_HEIGHT * 0.65f;

        float gameAreaX = VIRTUAL_WIDTH * 0.2f;
        float gameAreaY = VIRTUAL_HEIGHT * 0.1f;

        // draw north wall
        batch.draw(roomFloorTexture, gameAreaX, gameAreaY,gameAreaWidth, gameAreaHeight);
        if (isValidCell(playerX, playerY + 1) && !isPitAt(playerX, playerY + 1) && !isBatAt(playerX, playerY + 1)) {
            batch.draw(
                passageTexture,
                gameAreaX + gameAreaWidth * 0.35f,
                gameAreaY + gameAreaHeight * 0.8f,
                gameAreaWidth * 0.3f,
                gameAreaHeight * 0.2f
            );
        } else {
            batch.draw(
                wallTexture,
                gameAreaX + gameAreaWidth * 0.35f,
                gameAreaY + gameAreaHeight * 0.8f,
                gameAreaWidth * 0.3f,
                gameAreaHeight * 0.2f
            );
        }

        // draw south wall
        if (isValidCell(playerX, playerY - 1) && !isPitAt(playerX, playerY - 1) && !isBatAt(playerX, playerY - 1)) {
            batch.draw(
                passageTexture,
                gameAreaX + gameAreaWidth * 0.35f,
                gameAreaY,
                gameAreaWidth * 0.3f,
                gameAreaHeight * 0.2f
            );
        } else {
            batch.draw(
                wallTexture,
                gameAreaX + gameAreaWidth * 0.35f,
                gameAreaY,
                gameAreaWidth * 0.3f,
                gameAreaHeight * 0.2f
            );
        }

        // draw east wall
        if (isValidCell(playerX + 1, playerY) && !isPitAt(playerX + 1, playerY) && !isBatAt(playerX + 1, playerY)) {
            batch.draw(
                passageTexture,
                gameAreaX + gameAreaWidth * 0.8f,
                gameAreaY + gameAreaHeight * 0.35f,
                gameAreaWidth * 0.2f,
                gameAreaHeight * 0.3f
            );
        } else {
            batch.draw(
                wallTexture,
                gameAreaX + gameAreaWidth * 0.8f,
                gameAreaY + gameAreaHeight * 0.35f,
                gameAreaWidth * 0.2f,
                gameAreaHeight * 0.3f
            );
        }

        // draw west wall
        if (isValidCell(playerX - 1, playerY) && !isPitAt(playerX - 1, playerY) && !isBatAt(playerX - 1, playerY)) {
            batch.draw(
                passageTexture,
                gameAreaX,
                gameAreaY + gameAreaHeight * 0.35f,
                gameAreaWidth * 0.2f,
                gameAreaHeight * 0.3f
            );
        } else {
            batch.draw(
                wallTexture,
                gameAreaX,
                gameAreaY + gameAreaHeight * 0.35f,
                gameAreaWidth * 0.2f,
                gameAreaHeight * 0.3f
            );
        }

        // draw player
        batch.draw(
            playerTexture,
            gameAreaX + gameAreaWidth * 0.4f,
            gameAreaY + gameAreaHeight * 0.4f,
            gameAreaWidth * 0.2f,
            gameAreaHeight * 0.2f
        );

        /* draw room elements (if present) */
        // draw wumpus
        if (playerX == wumpusX && playerY == wumpusY && wumpusAlive) {
            batch.draw(wumpusTexture,
                gameAreaX + gameAreaWidth * 0.5f,
                gameAreaY + gameAreaHeight * 0.5f,
                gameAreaWidth * 0.15f,
                gameAreaHeight * 0.15f
            );
        }
        // draw gold
        if (playerX == goldX && playerY == goldY && !hasGold) {
            batch.draw(goldTexture,
                gameAreaX + gameAreaWidth * 0.45f,
                gameAreaY + gameAreaHeight * 0.45f,
                gameAreaWidth * 0.1f,
                gameAreaHeight * 0.1f
            );
        }
        // draw pit
        if (isPitAt(playerX, playerY)) {
            batch.draw(pitTexture,
                gameAreaX + gameAreaWidth * 0.45f,
                gameAreaY + gameAreaHeight * 0.45f,
                gameAreaWidth * 0.1f,
                gameAreaHeight * 0.1f
            );
        }
        // draw bat
        if (isBatAt(playerX, playerY)) {
            batch.draw(batTexture,
                gameAreaX + gameAreaWidth * 0.45f,
                gameAreaY + gameAreaHeight * 0.45f,
                gameAreaWidth * 0.1f,
                gameAreaHeight * 0.1f
            );
        }
    }

    private boolean isValidCell(int x, int y) {
        return x >= 0 && x < WORLD_SIZE && y >=0 && y < WORLD_SIZE;
    }

    private void moveForward() {
        if (gameState != GameState.PLAYING) return;
        int newX = playerX;
        int newY = playerY;

        switch (playerDirection) {
            case NORTH:
                newY++ ;
                break;
            case EAST:
                newX++;
                break;
            case SOUTH:
                newY--;
                break;
            case WEST:
                newX--;
                break;
        }

        if (isValidCell(newX, newY)) {
            world[playerX][playerY] = ' ';
            playerX = newX;
            playerY = newY;
            world[playerY][playerY] = 'P';
            appendToLog(Messages.MOVE_ACTION);
            checkRoomContent();
            updatePerceptions();
        } else {
            appendToLog(Messages.AGENT_HIT_WALL);
        }
    }

    private void turnLeft() {
        if (gameState != GameState.PLAYING) return;
        switch (playerDirection) {
            case NORTH:
                playerDirection = Direction.WEST;
                break;
            case EAST:
                playerDirection = Direction.NORTH;
                break;
            case SOUTH:
                playerDirection = Direction.EAST;
                break;
            case WEST:
                playerDirection = Direction.SOUTH;
                break;
        }
        appendToLog(Messages.TURN_LEFT_ACTION + " " + Messages.YOU_ARE_FACING + " [" + playerDirection + "]");
        updatePerceptions();
    }

    private void turnRight() {
        if (gameState != GameState.PLAYING) return;
        switch (playerDirection) {
            case NORTH:
                playerDirection = Direction.EAST;
                break;
            case EAST:
                playerDirection = Direction.SOUTH;
                break;
            case SOUTH:
                playerDirection = Direction.WEST;
                break;
            case WEST:
                playerDirection = Direction.NORTH;
                break;
        }
        appendToLog(Messages.TURN_RIGHT_ACTION + " " + Messages.YOU_ARE_FACING + " [" + playerDirection + "]");
        updatePerceptions();
    }

    private void shootArrow() {
        if (gameState != GameState.PLAYING) return;
        if (arrowsLeft > 0) {
            arrowsLeft--;
            appendToLog(Messages.SHOOT_ARROW_ACTION);

            int currentX = playerX;
            int currentY = playerY;
            int dx = 0, dy = 0;

            switch (playerDirection) {
                case NORTH:
                    dy = 1;
                    break;
                case EAST:
                    dx = 1;
                    break;
                case SOUTH:
                    dy = -1;
                case WEST:
                    dx = -1;
                    break;
            }

            while (isValidCell(currentX, currentY)) {
                currentX += dx;
                currentY += dy;

                if (currentX == wumpusX && currentY == wumpusY && wumpusAlive) {
                    appendToLog(Messages.ARROW_HIT_WUMPUS);
                    wumpusAlive = false;
                    world[wumpusX][wumpusY] = ' ';
                    if (hasGold && playerX == 0 && playerY == 0) {
                        gameState = GameState.GAME_WON;
                        appendToLog(Messages.AGENT_WON_LOG);
                        appendToLog(Messages.GAME_OVER_MESSAGE);
                        appendToLog(Messages.RESET_MESSAGE);
                        updatePerceptions();
                        return;
                    }
                }
            }
            appendToLog(Messages.ARROW_HIT_WALL);
        } else {
            appendToLog(Messages.NO_ARROW);
        }
    }

    private void searchForGold() {
        if (gameState != GameState.PLAYING) return;
        if (playerX == goldX && playerY == goldY && !hasGold) {
            hasGold = true;
            world[goldX][goldY] = ' ';
            appendToLog(Messages.FOUND_GOLD);
            updateInfoBar(Messages.GOT_GOLD_INFO);
            updatePerceptions();
        } else {
            appendToLog(Messages.FOUND_NOTHING);
        }
    }

    private void checkRoomContent() {
        if (isPitAt(playerX, playerY)) {
            gameState = GameState.GAME_OVER;
            appendToLog(Messages.AGENT_FELL_PIT);
            appendToLog(Messages.GAME_OVER_MESSAGE);
            appendToLog(Messages.RESET_MESSAGE);
        } else if (isBatAt(playerX,playerY)) {
            teleportPlayer();
            appendToLog("You got teleported by a bat");
        } else if (playerX == wumpusX && playerY == wumpusY && wumpusAlive) {
            gameState = GameState.GAME_OVER;
            appendToLog(Messages.AGENT_CAUGHT_BY_WUMPUS);
            appendToLog(Messages.GAME_OVER_MESSAGE);
            appendToLog(Messages.RESET_MESSAGE);
        } else if (hasGold && playerX == 0 && playerY == 0) {
            gameState = GameState.GAME_WON;
            appendToLog(Messages.AGENT_WON_LOG);
            appendToLog(Messages.GAME_OVER_MESSAGE);
            appendToLog(Messages.RESET_MESSAGE);
        }
    }

    private void teleportPlayer() {
        int newX, newY;
        do {
            newX = random.nextInt(WORLD_SIZE);
            newY = random.nextInt(WORLD_SIZE);
        } while (world[newX][newY] != ' ');

        world[playerX][playerY] = ' ';
        playerX = newX;
        playerY = newY;
        world[playerX][playerY] = 'P';
        appendToLog("You are now in [" + playerX + "][" + playerY + "]");
        updatePerceptions();
    }

    private void updatePerceptions() {
        String perceptions = Messages.YOU_FEEL;
        boolean sensedSomething = false;

        //if ()
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
        camera.position.set(viewport.getWorldWidth()/2,  viewport.getWorldHeight()/2, 0);
        camera.update();
        stage.getViewport().update(width,height,true);
        restartButton.setPosition((float) VIRTUAL_WIDTH / 2 - restartButton.getWidth() / 2, (float) VIRTUAL_HEIGHT / 2 - restartButton.getHeight() / 2);
    }

    @Override
    public void dispose() {
        /*grassTexture.dispose();
        playerTexture.dispose();
        wumpusTexture.dispose();
        pitTexture.dispose();
        batTexture.dispose();
        goldTexture.dispose();
        stenchTexture.dispose();
        breezeTexture.dispose();
        glitterTexture.dispose();
        arrowTexture.dispose();
        gameOverTexture.dispose();
        gameWonTexture.dispose();
        roomFloorTexture.dispose();
        passageTexture.dispose();*/
        batch.dispose();
        stage.dispose();
        skin.dispose();
    }

    private static class ScrollingLabel extends Actor {
        private BitmapFont font;
        private Color color;
        private String text;
        private float textWidth;
        private float scrollSpeed;
        private float currentXOffset;
        private final GlyphLayout layout;
        private final float padding = 10;
        public ScrollingLabel(String text, Skin skin, String fontStyleName, Color color, float scrollSpeed) {
            this.font = skin.getFont(fontStyleName);
            this.color = color;
            this.text = text;
            this.scrollSpeed = scrollSpeed;
            this.layout = new GlyphLayout();
            setText(text);
        }

        public void setText(String newText) {
            this.text = newText;
            layout.setText(font, text);
            this.textWidth = layout.width;
            this.currentXOffset = 0;
        }

        @Override
        public void act(float delta) {
            if (textWidth > getWidth()) {
                currentXOffset -= scrollSpeed * delta;
                if (currentXOffset<= -(textWidth + padding)) {
                    currentXOffset += (textWidth + padding);
                }
            }
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            font.setColor(color);
            font.draw(
              batch,
              text,
              getX() + currentXOffset,
              getY() + getHeight() / 2 + layout.height / 2
            );
            if (textWidth > getWidth()) {
                font.draw(
                    batch,
                    text,
                    getX() + currentXOffset + textWidth + padding,
                    getY() + getHeight() / 2 + layout.height / 2
                );
            }
        }
    }
}
