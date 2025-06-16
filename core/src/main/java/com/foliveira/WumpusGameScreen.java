package com.foliveira;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ScissorStack;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.FitViewport;

import com.foliveira.config.Messages;
import com.foliveira.utils.GdxUtils;
import com.foliveira.utils.debug.DebugCameraController;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class WumpusGameScreen extends ApplicationAdapter {
    private static final int WORLD_SIZE = 4;
    private static final int NUM_PITS = 3;
    private static final int NUM_BATS = 2;
    private static final int NUM_WUMPUS = 1;
    private static final int NUM_ARROWS = 1;
    private static final int BASE_VIRTUAL_WIDTH = 320;
    private static final int BASE_VIRTUAL_HEIGHT = 240;

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
    //private AssetManager manager;
    private OrthographicCamera gameplayCamera;
    private ExtendViewport gameplayViewport;
    private Stage gameplayStage;
    private OrthographicCamera hudCamera;
    private FitViewport hudViewport;
    private Stage hudStage;
    private DebugCameraController debugCameraController;
    private BitmapFont font;
    private Skin skin;
    private Label logLabel;
    private ScrollPane logScrollPane;
    private ScrollingLabel infoBarLabel;

    private TextButton restartButton;
    private TextButton mapButton;
    private TextButton moveButton;
    private TextButton turnLeftButton;
    private TextButton turnRightButton;
    private TextButton searchButton;
    private TextButton specialButton;

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
    private final Random random = ThreadLocalRandom.current();
    private boolean wumpusAlive = true;
    private float logHeight;
    private float infoBarHeight;
    private float buttonsContainerHeight;
    private float gameAreaY;
    private float gameAreaHeight;

    private MapScreen mapScreen;      // Nova tela de mapa
    private HashSet<Vector2> visitedRooms; // Para rastrear salas visitadas
    private int score; // Placar do jogo

    @Override
    public void create() {
        batch = new SpriteBatch();

        // Gameplay layer============
        gameplayCamera = new OrthographicCamera();
        gameplayViewport = new ExtendViewport(BASE_VIRTUAL_WIDTH, BASE_VIRTUAL_HEIGHT, gameplayCamera);
        gameplayStage = new Stage(gameplayViewport, batch);
        //gameplayViewport.apply();

        // ==========HUD-LAYER===============
        hudCamera = new OrthographicCamera();
        hudViewport = new FitViewport(BASE_VIRTUAL_WIDTH, BASE_VIRTUAL_HEIGHT, hudCamera);
        hudStage = new Stage(hudViewport, batch);

        InputMultiplexer multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(hudStage);
        multiplexer.addProcessor(gameplayStage);
        Gdx.input.setInputProcessor(multiplexer);

//        gameplayCamera.position.set(gameplayViewport.getWorldWidth() / 2, gameplayViewport.getWorldHeight() / 2, 0);
//        gameplayCamera.update();

        debugCameraController = new DebugCameraController();
        debugCameraController.setStartPosition((float) BASE_VIRTUAL_WIDTH /2, (float) BASE_VIRTUAL_HEIGHT /2);

        font = new BitmapFont();
        font.setColor(Color.WHITE);
        font.getData().setScale(0.5f);
        //loadTextures(); //comment for now(no textures yet...)

        skin = new Skin(Gdx.files.internal("ui/uiskin.json"));

        BitmapFont uiFont = skin.getFont("default");
//        BitmapFont logFont = skin.getFont("default");
        float lineHeight = uiFont.getLineHeight();
        logHeight = (lineHeight * 4) + 10;

        infoBarHeight = BASE_VIRTUAL_HEIGHT * 0.1f;
        buttonsContainerHeight = BASE_VIRTUAL_HEIGHT * 0.25f;
        gameAreaHeight = BASE_VIRTUAL_HEIGHT - (logHeight + infoBarHeight + buttonsContainerHeight);
        gameAreaY = infoBarHeight + buttonsContainerHeight;

        setupUI();

        initializeWorld();
        gameState = GameState.PLAYING;
        playerDirection = Direction.NORTH;
        appendToLog(Messages.WELCOME_LOG);
        updatePerceptions();
        //updateInfoBar(Messages.INITIAL_MESSAGE_INFO);
    }

    private void setupUI() {
        Table hudRootTable = new Table(skin);
        hudRootTable.setFillParent(true);
        hudRootTable.debug();

        Table logTable = new Table(skin);
        //logTable.setBackground("default-rect");
        logLabel = new Label("", skin);
        logLabel.setWrap(true);
        logScrollPane = new ScrollPane(logLabel, skin);
        logScrollPane.setFadeScrollBars(true);
        logScrollPane.setScrollingDisabled(true, false);
        logTable.add(logScrollPane).expand().fill().pad(5);

        hudRootTable.add(logTable).height(logHeight).expandX().fillX().padLeft(5).padRight(5).row();

        hudRootTable.add().expand().fill().row();

        Table buttonsContainerTable = new Table(skin);
        //buttonsContainerTable.setBackground("default-rect");
        buttonsContainerTable.defaults().pad(2);

        Table leftButtons = new Table(skin);
        leftButtons.defaults().pad(2).width(60).height(40);
        leftButtons.align(Align.left);
        TextButton moveButton = new TextButton("MOVE", skin);
        moveButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
               if (gameState == GameState.PLAYING) moveForward();
               printWorld();
            }
        });
        TextButton turnLeftButton = new TextButton("TURN LEFT", skin);
        turnLeftButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (gameState == GameState.PLAYING) turnLeft();
                printWorld();
            }
        });
        TextButton turnRightButton = new TextButton("TURN RIGHT", skin);
        turnRightButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (gameState == GameState.PLAYING) turnRight();
                printWorld();
            }
        });
        leftButtons.add("").row();
        leftButtons.add(moveButton).row();
        leftButtons.add(turnLeftButton).padRight(5);
        leftButtons.add(turnRightButton).padLeft(5).row();
        leftButtons.add("").row();

        /*centerTable.add(leftButtons).width(VIRTUAL_WIDTH * 0.25f).expandY().fillY();

        centerTable.add().expand().fill();*/

        Table rightButtons = new Table(skin);
        rightButtons.defaults().pad(2).width(60).height(40);
        rightButtons.align(Align.right);
        TextButton searchButton = new TextButton("SEARCH", skin);
        searchButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (gameState == GameState.PLAYING) searchForGold();
                printWorld();
            }
        });
        TextButton specialButton = new TextButton("SPECIAL", skin);
        specialButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (gameState == GameState.PLAYING) shootArrow();
                printWorld();
            }
        });
        rightButtons.add("").row();
        rightButtons.add(specialButton).row();
        rightButtons.add(searchButton).row();
        rightButtons.add("").row();

        //centerTable.add(rightButtons).width(VIRTUAL_WIDTH * 0.2f).expandY().fillY();

        buttonsContainerTable.add(leftButtons).width(BASE_VIRTUAL_WIDTH * 0.2f).expandY().fillY();
        buttonsContainerTable.add().expandX().fillX();
        buttonsContainerTable.add(rightButtons).width(BASE_VIRTUAL_WIDTH * 0.2f).expandY().fillY();

        hudRootTable.add(buttonsContainerTable).height(buttonsContainerHeight).expandX().fillX().padBottom(15).row();


        Table bottomBarTable = new Table(skin);
        //bottomBarTable.setBackground("default-rect");
        bottomBarTable.pad(2);

        infoBarLabel = new ScrollingLabel(Messages.TIPS, skin, "default", Color.WHITE, 20f, gameplayCamera);
        bottomBarTable.add(infoBarLabel).expandX().fillX();
        mapButton = new TextButton("MAP", skin);
        mapButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (gameState == GameState.PLAYING) {
                    mapScreen.setVisible(!mapScreen.isVisible()); // Toggles map screen visibility
                }
            }
        });
        bottomBarTable.add(mapButton).width(60).height(40).padLeft(5);
        hudRootTable.add(bottomBarTable).height(infoBarHeight).expandX().fillX().row();

        hudStage.addActor(hudRootTable);

        //rootTable.add(infoBarLabel).height(infoBarHeight).expandX().fillX().row();

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
        hudStage.addActor(restartButton);
        restartButton.setSize(100, 35);
        restartButton.setPosition(
            (float) BASE_VIRTUAL_WIDTH / 2 - restartButton.getWidth() / 2,
            (float) BASE_VIRTUAL_HEIGHT / 2 - restartButton.getHeight() / 2
        );

        // Initializes and adds the map screen (invisible by default)
        mapScreen = new MapScreen(skin, "default", WORLD_SIZE);
        mapScreen.setFillParent(true); // Makes the MapScreen fill the entire Stage
        mapScreen.setVisible(false);
        hudStage.addActor(mapScreen); // Adds to Stage so it's overlaid
    }

    private void appendToLog(String message) {
        logLabel.setText(logLabel.getText().toString() + "\n- " + message);
        logScrollPane.layout();
        logScrollPane.setScrollPercentY(1);
    }

    private void updateInfoBar(String message) {
        infoBarLabel.setText(message);
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

        score = 0;
        visitedRooms = new HashSet<>();

        playerX = 0;
        playerY = 0;
        world[playerX][playerX] = 'P';

        visitedRooms.add(new Vector2(playerX, playerY));

        pitPositions = new Array<>();
        batPositions = new Array<>();

        boolean validLayout = false;
        int attempts = 0;
        while (!validLayout && attempts < 100) {
            attempts++;
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
        }
        if (attempts >= 100 && !validLayout) {
            Gdx.app.error(WumpusGameScreen.class.getName(), "no valid layout was possible.");
        } else {
            Gdx.app.log(WumpusGameScreen.class.getName(), "valid layout generated after " + attempts + " attempts");
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
        if (!wumpusAlive) return true;

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
        debugCameraController.applyToCamera(gameplayCamera);

        GdxUtils.clearScreen();

        gameplayCamera.update();

        batch.setProjectionMatrix(gameplayCamera.combined);

        batch.begin();
        //drawIsometricRoom();
        batch.end();

        hudStage.act(Gdx.graphics.getDeltaTime());
        hudStage.draw();

        restartButton.setVisible(gameState == GameState.GAME_OVER || gameState == GameState.GAME_WON);
    }

    private void drawIsometricRoom() {
        float gameAreaWidth = gameplayViewport.getWorldWidth();
        float gameAreaHeight = gameplayViewport.getWorldHeight();

        float renderX = gameplayCamera.position.x - gameAreaWidth / 2f;
        float renderY = gameplayCamera.position.y - gameAreaHeight / 2f;

        float gameAreaX = BASE_VIRTUAL_WIDTH * 0.2f;


        batch.draw(roomFloorTexture, renderX, renderY, gameAreaWidth, gameAreaHeight);
        float wallWidth = gameAreaWidth * 0.3f;
        float wallHeight = gameAreaHeight * 0.2f;
        float passageWidth = gameAreaWidth * 0.2f;
        float passageHeight = gameAreaHeight * 0.3f;
        float playerSize = gameAreaWidth * 0.2f;

        // draw north wall
        if (isValidCell(playerX, playerY + 1) && !isPitAt(playerX, playerY + 1) && !isBatAt(playerX, playerY + 1)) {
            batch.draw(
                isValidCell(playerX, playerY + 1) ? passageTexture : wallTexture,
                renderX + gameAreaWidth * 0.35f,
                renderY + gameAreaHeight * 0.8f,
                wallWidth,
                wallHeight
            );
        } else {
            batch.draw(
                wallTexture,
                renderX + gameAreaWidth * 0.35f,
                renderY + gameAreaHeight * 0.8f,
                wallWidth,
                wallHeight
            );
        }

        // draw south wall
        if (isValidCell(playerX, playerY - 1) && !isPitAt(playerX, playerY - 1) && !isBatAt(playerX, playerY - 1)) {
            batch.draw(
                passageTexture,
                renderX + gameAreaWidth * 0.35f,
                renderY,
                wallWidth,
                wallHeight
            );
        } else {
            batch.draw(
                wallTexture,
                renderX + gameAreaWidth * 0.35f,
                renderY,
                wallWidth,
                wallHeight
            );
        }

        // draw east wall
        if (isValidCell(playerX + 1, playerY) && !isPitAt(playerX + 1, playerY) && !isBatAt(playerX + 1, playerY)) {
            batch.draw(
                passageTexture,
                renderX + gameAreaWidth * 0.8f,
                renderY + gameAreaHeight * 0.35f,
                passageWidth,
                passageHeight
            );
        } else {
            batch.draw(
                wallTexture,
                renderX + gameAreaWidth * 0.8f,
                renderY + gameAreaHeight * 0.35f,
                passageWidth,
                passageHeight
            );
        }

        // draw west wall
        if (isValidCell(playerX - 1, playerY) && !isPitAt(playerX - 1, playerY) && !isBatAt(playerX - 1, playerY)) {
            batch.draw(
                passageTexture,
                renderX,
                renderY + gameAreaHeight * 0.35f,
                passageWidth,
                passageHeight
            );
        } else {
            batch.draw(
                wallTexture,
                renderX,
                renderY + gameAreaHeight * 0.35f,
                passageWidth,
                passageHeight
            );
        }

        // draw player
        batch.draw(
            playerTexture,
            renderX + gameAreaWidth * 0.4f,
            renderY + gameAreaHeight * 0.4f,
            playerSize,
            playerSize
        );

        /* draw room elements (if present) */
        float itemSize = playerSize * 0.75f;

        // draw wumpus
        if (playerX == wumpusX && playerY == wumpusY && wumpusAlive) {
            batch.draw(wumpusTexture,
                renderX + gameAreaWidth * 0.5f - itemSize / 2,
                renderY + gameAreaHeight * 0.5f - itemSize / 2,
                itemSize,
                itemSize
            );
        }
        // draw gold
        if (playerX == goldX && playerY == goldY && !hasGold) {
            batch.draw(goldTexture,
                renderX + gameAreaWidth * 0.45f - itemSize / 2,
                renderY + gameAreaHeight * 0.45f - itemSize / 2,
                itemSize,
                itemSize
            );
        }
        // draw pit
        if (isPitAt(playerX, playerY)) {
            batch.draw(pitTexture,
                renderX + gameAreaWidth * 0.45f - itemSize / 2,
                renderY + gameAreaHeight * 0.45f - itemSize / 2,
                itemSize,
                itemSize
            );
        }
        // draw bat
        if (isBatAt(playerX, playerY)) {
            batch.draw(batTexture,
                renderX + gameAreaWidth * 0.45f - itemSize / 2,
                renderY + gameAreaHeight * 0.45f - itemSize / 2,
                itemSize,
                itemSize
            );
        }
    }

    private boolean isValidCell(int x, int y) {
        return x >= 0 && x < WORLD_SIZE && y >=0 && y < WORLD_SIZE;
    }

    private void moveForward() {
        if (gameState != GameState.PLAYING) return;
        score--;
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
            visitedRooms.add(new Vector2(playerX, playerY));
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
            String message = Messages.SHOOT_ARROW_ACTION;
            arrowsLeft--;
            score -= 10;

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
                    message = message.concat(Messages.ARROW_HIT_WUMPUS);
                    wumpusAlive = false;
                    score += 200;
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
            message = message.concat(Messages.ARROW_HIT_WALL);
            appendToLog(message);
        } else {
            appendToLog(Messages.NO_ARROW);
        }
    }

    private void searchForGold() {
        if (gameState != GameState.PLAYING) return;
        if (playerX == goldX && playerY == goldY && !hasGold) {
            hasGold = true;
            score += 500;
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
            score -= 1000;
            appendToLog(Messages.GAME_OVER_MESSAGE);
            appendToLog(Messages.RESET_MESSAGE);
//        } else if (isBatAt(playerX,playerY)) {
//            teleportPlayer();
//            appendToLog("You got teleported by a bat");
        } else if (playerX == wumpusX && playerY == wumpusY && wumpusAlive) {
            gameState = GameState.GAME_OVER;
            appendToLog(Messages.AGENT_CAUGHT_BY_WUMPUS);
            score -= 1000;
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
        mapScreen.update(playerX, playerY, visitedRooms, wumpusAlive, hasGold, arrowsLeft, score);
        String perceptions = Messages.YOU_FEEL;
        boolean sensedSomething = false;

        if (isStench(playerX, playerY)) {
            perceptions += Messages.STENCH;
            sensedSomething = true;
        }
        if (isBreeze(playerX, playerY)) {
            perceptions += Messages.BREEZE;
            sensedSomething = true;
        }
        if (isGlitter()) {
            perceptions += Messages.GLITTER;
            sensedSomething = true;
        }
        if (!sensedSomething) {
            perceptions += Messages.NOTHING;
        }
        appendToLog(perceptions);
    }

    private boolean isStench(int x, int y) {
        if (!wumpusAlive) return false;
        int [] dx = {0,0,1,-1};
        int [] dy = {1,-1,0,0};

        for (int i=0;i<4;i++){
            int checkX = x + dx[i];
            int checkY = y + dy[i];
            if (isValidCell(checkX, checkY) && checkX == wumpusX && checkY == wumpusY) return true;
        }
        return false;
    }

    private boolean isBreeze(int x, int y) {
        int [] dx = {0,0,1,-1};
        int [] dy = {1,-1,0,0};

        for (Vector2 pit : pitPositions) {
            for (int i=0;i<4;i++){
                int checkX = x + dx[i];
                int checkY = y + dy[i];
                if (isValidCell(checkX, checkY) && checkX == pit.x && checkY == pit.y) return true;
            }
        }
        return false;
    }

    private boolean isGlitter() {
        return playerX == goldX && playerY == goldY && !hasGold;
    }

    @Override
    public void resize(int width, int height) {
        gameplayViewport.update(width, height);
        gameplayCamera.position.set(
            gameplayViewport.getWorldWidth()/2,
            gameplayViewport.getWorldHeight()/2,
            0
        );
        gameplayCamera.update();

        float aspectRatio = (float)width/height;
        float hudVirtualWidth = BASE_VIRTUAL_HEIGHT * aspectRatio;
        hudViewport.setWorldSize(hudVirtualWidth, BASE_VIRTUAL_HEIGHT);
        hudViewport.update(width, height, true);
        hudCamera.position.set(
            hudViewport.getWorldWidth() / 2f,
            hudViewport.getWorldHeight() / 2f,
            0
        );
        hudCamera.update();

        restartButton.setPosition(
            hudViewport.getWorldWidth() / 2f - restartButton.getWidth() / 2f,
            hudViewport.getWorldHeight() / 2f - restartButton.getHeight() / 2f
        );
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
        mapScreen.dispose(); // Descarta os recursos da tela do mapa
        batch.dispose();
        gameplayStage.dispose();
        hudStage.dispose();
        skin.dispose();
    }

    private static class ScrollingLabel extends Actor {
        private final BitmapFont font;
        private final Color color;
        private final String fixedPrefix = Messages.INFO;
        private final float fixedPrefixWidth;
        private String scrollingText;
        private float scrollingTextWidth;
        private final float scrollSpeed;
        private float currentXOffset;
        private final GlyphLayout layout;
        private final float padding = 10;
        private final Camera camera;
        public ScrollingLabel(String scrollingText, Skin skin, String fontStyleName, Color color, float scrollSpeed, Camera camera) {
            this.font = skin.getFont(fontStyleName);
            this.color = color;
            //this.text = scrollingText;
            this.scrollSpeed = scrollSpeed;
            this.layout = new GlyphLayout();

            layout.setText(font, fixedPrefix);
            this.fixedPrefixWidth = layout.width;
            this.camera = camera;

            setText(scrollingText);
        }

        public void setText(String newScrollingText) {
            this.scrollingText = newScrollingText;
            layout.setText(font, scrollingText);
            this.scrollingTextWidth = layout.width;
            this.currentXOffset = 0;
        }

        @Override
        public void act(float delta) {
            super.act(delta);
            float availableScrollingAreaWidth = getWidth() - fixedPrefixWidth;
            if (scrollingTextWidth > availableScrollingAreaWidth && availableScrollingAreaWidth > 0) {
                currentXOffset -= scrollSpeed * delta;
                if (currentXOffset <= -(scrollingTextWidth + padding)) {
                    currentXOffset += (scrollingTextWidth + padding);
                }
            } else {
                currentXOffset = 0;
            }
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            font.setColor(color);
            font.draw(
                batch,
                Messages.INFO,
                getX() + 5,
                getY() + getHeight() / 2 + layout.height / 2
            );

            float scrollingDrawTextX = getX() + fixedPrefixWidth;
            float clipX = scrollingDrawTextX;
            float clipY = getY();
            float clipWidth = getWidth() - fixedPrefixWidth;
            float clipHeight = getHeight() + BASE_VIRTUAL_HEIGHT * 0.1f;

            batch.flush();

            Rectangle scissors = new Rectangle();
            Rectangle clipBounds = new Rectangle(clipX,clipY,clipWidth,clipHeight);
            ScissorStack.calculateScissors(camera, batch.getTransformMatrix(), clipBounds, scissors);
            if (ScissorStack.pushScissors(scissors)) {
                font.draw(
                    batch,
                    scrollingText,
                    scrollingDrawTextX + currentXOffset,
                    getY() + getHeight() / 2 + layout.height / 2
                );
                if (scrollingTextWidth > clipWidth) {
                    font.draw(
                        batch,
                        scrollingText,
                        scrollingDrawTextX + currentXOffset + scrollingTextWidth + padding,
                        getY() + getHeight() / 2 + layout.height / 2
                    );
                    batch.flush();
                }
                ScissorStack.popScissors();
            }
        }
    }

    private void printWorld() {
        for (int i=0;i<WORLD_SIZE;i++) {
            for (int j=0;j<WORLD_SIZE;j++) {
                if (world[i][j] == 'P') {
                    switch (playerDirection) {
                        case NORTH:
                            System.out.print(" ^ ");
                            break;
                        case EAST:
                            System.out.print(" > ");
                            break;
                        case SOUTH:
                            System.out.print(" v ");
                            break;
                        case WEST:
                            System.out.print(" < ");
                            break;
                    }
                } else if(world[i][j] == 'B'){
                    System.out.print(" " + world[i][j] + " ");
                } else {
                    System.out.print(" " + world[i][j] + " ");
                }

            }
            System.out.print("\n");
        }
    }

    private static class MapScreen extends Table {
        private final Skin skin;
        private final BitmapFont font;
        private final int worldSize;
        private final ShapeRenderer shapeRenderer;

        // Game data to display
        private int playerX, playerY;
        private HashSet<Vector2> visitedRooms;
        private boolean wumpusAlive;
        private boolean hasGold;
        private int arrowsLeft;
        private int score;

        private float flashingAlpha = 1.0f;
        private float flashingSpeed = 2.0f; // Flashing speed
        private boolean alphaIncreasing = false;

        // Labels for status information
        private Label scoreLabel;
        private Label arrowsLabel;
        private Label wumpusStatusLabel;
        private Label goldStatusLabel;

        public MapScreen(Skin skin, String fontStyleName, int worldSize) {
            this.skin = skin;
            this.font = skin.getFont(fontStyleName);
            this.worldSize = worldSize;
            this.shapeRenderer = new ShapeRenderer();

            // Configures the semi-transparent background
            //setBackground(skin.newDrawable("default-rect", 0, 0, 0, 0.7f)); // Black with 70% opacity

            // Adds status labels
            Table statusTable = new Table(skin);
            statusTable.defaults().pad(5).align(Align.left);

            scoreLabel = new Label("SCORE: ", skin, "default", Color.WHITE);
            arrowsLabel = new Label("ARROWS: ", skin, "default", Color.WHITE);
            wumpusStatusLabel = new Label("WUMPUS: ", skin, "default", Color.WHITE);
            goldStatusLabel = new Label("GOLD: ", skin, "default", Color.WHITE);

            // Resizes font labels to be compatible with resolution
            scoreLabel.setFontScale(0.7f);
            arrowsLabel.setFontScale(0.7f);
            wumpusStatusLabel.setFontScale(0.7f);
            goldStatusLabel.setFontScale(0.7f);

            statusTable.add(scoreLabel).row();
            statusTable.add(arrowsLabel).row();
            statusTable.add(wumpusStatusLabel).row();
            statusTable.add(goldStatusLabel).row();

            // Map screen layout: status on left, map on right
            add(statusTable).expandY().fillY().pad(10);
            add().expand().fill(); // Empty cell for the map (drawn directly in draw)
            row(); // Starts a new row for the close button

            TextButton closeButton = new TextButton("BACK", skin);
            closeButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    setVisible(false); // Hides the map screen
                }
            });
            add(closeButton).colspan(2).width(100).height(35).pad(10).align(Align.center);
        }

        /**
         * Updates the data displayed on the map screen.
         */
        public void update(int playerX, int playerY, HashSet<Vector2> visitedRooms, boolean wumpusAlive, boolean hasGold, int arrowsLeft, int score) {
            this.playerX = playerX;
            this.playerY = playerY;
            this.visitedRooms = visitedRooms;
            this.wumpusAlive = wumpusAlive;
            this.hasGold = hasGold;
            this.arrowsLeft = arrowsLeft;
            this.score = score;

            scoreLabel.setText("SCORE: " + score);
            arrowsLabel.setText("ARROWS: " + arrowsLeft);
            wumpusStatusLabel.setText("WUMPUS: " + (wumpusAlive ? "ALIVE" : "DEAD"));
            goldStatusLabel.setText("GOLD: " + (hasGold ? "YES" : "NO"));
        }

        /**
         * Updates the flashing logic for the player's square.
         */
        @Override
        public void act(float delta) {
            super.act(delta);
            if (alphaIncreasing) {
                flashingAlpha += flashingSpeed * delta;
                if (flashingAlpha >= 1.0f) {
                    flashingAlpha = 1.0f;
                    alphaIncreasing = false;
                }
            } else {
                flashingAlpha -= flashingSpeed * delta;
                if (flashingAlpha <= 0.2f) { // Almost invisible
                    flashingAlpha = 0.2f;
                    alphaIncreasing = true;
                }
            }
        }

        /**
         * Draws the isometric map and status information.
         */
        @Override
        public void draw(Batch batch, float parentAlpha) {
            super.draw(batch, parentAlpha); // Desenha o fundo da tabela e as labels/botões

            batch.end();
            shapeRenderer.setProjectionMatrix(getStage().getCamera().combined);

            float mapAreaWidth = getWidth() - getCells().get(0).getPrefWidth() - getPadLeft() - getPadRight();
            float mapAreaHeight = getHeight() - getCells().get(2).getPrefHeight() - getPadTop() - getPadBottom();

            float mapOffsetX = getX() + getCells().get(0).getPrefWidth() + (mapAreaWidth / 2f);
            float mapOffsetY = getY() + getPadBottom() + (mapAreaHeight / 2f);

            float miniCellSize = 32;

            shapeRenderer.begin(ShapeType.Line);
            for (int y = 0; y < worldSize; y++) {
                for (int x = 0; x < worldSize; x++) {
                    float isoX = mapOffsetX + (x - y) * (miniCellSize / 2f);
                    float isoY = mapOffsetY + (x + y) * (miniCellSize / 4f);

                    shapeRenderer.setColor(Color.DARK_GRAY);
                    float[] vertices = new float[] {
                        isoX, isoY + miniCellSize / 4f,
                        isoX + miniCellSize / 2f, isoY,
                        isoX, isoY - miniCellSize / 4f,
                        isoX - miniCellSize / 2f, isoY
                    };
                    shapeRenderer.polygon(vertices);
                    if (x == playerX && y == playerY) {
                        shapeRenderer.setColor(1,1,1,flashingAlpha);
                        shapeRenderer.polygon(vertices);
                    }
                }
            }
            shapeRenderer.end();

            shapeRenderer.begin(ShapeType.Line);
            for (int y = 0; y < worldSize; y++) {
                for (int x = 0; x < worldSize; x++) {
                    float isoX = mapOffsetX + (x - y) * (miniCellSize / 2f);
                    float isoY = mapOffsetY + (x + y) * (miniCellSize / 4f);

                    Vector2 currentCell = new Vector2(x, y);
                    if (visitedRooms.contains(currentCell)) {
                        shapeRenderer.setColor(Color.WHITE);
                        float[] vertices = new float[] {
                          isoX, isoY + miniCellSize / 4f,
                          isoX + miniCellSize / 2f, isoY,
                          isoX, isoY - miniCellSize / 4f,
                          isoX - miniCellSize / 2f, isoY
                        };
                        shapeRenderer.polygon(vertices);
                    }
                }
            }
            shapeRenderer.end();



            batch.begin(); // reset the SpriteBatch
        }

        public void dispose() {
            shapeRenderer.dispose();
        }
    }
}
