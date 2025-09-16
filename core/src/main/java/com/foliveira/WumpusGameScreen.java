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
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.ScissorStack;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.FitViewport;

import com.foliveira.config.Messages;
import com.foliveira.utils.GdxUtils;
import com.foliveira.utils.debug.DebugCameraController;
import com.foliveira.utils.ga.GeneticAlgorithm;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

public class WumpusGameScreen extends ApplicationAdapter {
    public static final int WORLD_SIZE = 4; // NxN matrix
    public static final int NUM_PITS = 3;
    public static final int NUM_BATS = 2;
    private static final int NUM_WUMPUS = 1;
    public static final int NUM_ARROWS = 1;
    private static final int BASE_VIRTUAL_WIDTH = 320;
    private static final int BASE_VIRTUAL_HEIGHT = 240;
    private static final float LOG_BAR_VIRTUAL_HEIGHT = 50f;
    private static final float INFO_BAR_VIRTUAL_HEIGHT = 25f;
    private static final float BUTTONS_CONTAINER_VIRTUAL_HEIGHT = 80f;
    private static final float EFFECTIVE_GAMEPLAY_VIRTUAL_WIDTH = BASE_VIRTUAL_WIDTH;
    private static final float EFFECTIVE_GAMEPLAY_VIRTUAL_HEIGHT = BASE_VIRTUAL_HEIGHT - LOG_BAR_VIRTUAL_HEIGHT - INFO_BAR_VIRTUAL_HEIGHT;

    private enum Direction {
        NORTH, EAST, SOUTH, WEST
    }
    private Direction playerDirection;
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
    private Texture hexRoomBaseTexture;
    private Texture hexPassageNorthTexture;
    private Texture hexPassageEastTexture;
    private Texture hexPassageSouthTexture;
    private Texture hexPassageWestTexture;
    private Texture eastArrowDirectionTexture;
    private Texture northArrowDirectionTexture;
    private Texture southArrowDirectionTexture;
    private Texture westArrowDirectionTexture;
    private Texture mapTexture;
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
    private String defaultInfoBarMessage;

    // buttons
    private TextButton restartButton;
    private TextButton mapButton;
    private TextButton moveButton;
    private TextButton turnLeftButton;
    private TextButton turnRightButton;
    private TextButton searchButton;
    private TextButton specialButton;
    private TextButton toggleAgentButton;

    private char[][] world;
    private int playerX, playerY;
    private int wumpusX, wumpusY;
    private Array<Vector2> pitPositions;
    private Array<Vector2> batPositions;
    private int goldX, goldY;
    public enum GameState {
        PLAYING, GAME_OVER, GAME_WON
    }
    private GameState gameState;
    private boolean hasGold = false;
    private int arrowsLeft = NUM_ARROWS;
    private final Random random = ThreadLocalRandom.current();
    private boolean wumpusAlive = true;
    private float logHeight;
    private float infoBarHeight;
    private float infoBarWidth;
    private float buttonsContainerHeight;
    private float gameAreaY;
    private float gameAreaHeight;

    private MapScreen mapScreen;      // Nova tela de mapa
    public WumpusWorldState currentGameState;
    private HashSet<Vector2> visitedRooms; // Para rastrear salas visitadas
    private int score; // Placar do jogo

    private ReactiveAgent agent;
    private ReactiveAgentV2 agentV2;
    //private GeneticAgent geneticAgent;
    private NeuralAgent neuralAgent;
    private boolean isAgentPlaying = false;
    private float agentActionTimer = 0f;
    private static final float AGENT_ACTION_DELAY = 0.5f;

    private boolean shouldRepositionGoldAfterDeath = false;
    private int repositionGoldTargetX = -1;
    private int repositionGoldTargetY = -1;

    private GeneticAlgorithm ga;

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
        loadTextures(); //comment for now(no textures yet...)

        skin = new Skin(Gdx.files.internal("ui/uiskin.json"));

        BitmapFont uiFont = skin.getFont("default");
//        BitmapFont logFont = skin.getFont("default");
        float lineHeight = uiFont.getLineHeight();
        logHeight = (lineHeight * 4) + 10;

        infoBarHeight = BASE_VIRTUAL_HEIGHT * 0.1f;
        buttonsContainerHeight = BASE_VIRTUAL_HEIGHT * 0.25f;
        gameAreaHeight = BASE_VIRTUAL_HEIGHT - (logHeight + infoBarHeight);
        gameAreaY = infoBarHeight;

        agent = new ReactiveAgent();
        agentV2 = new ReactiveAgentV2();
        //geneticAgent = new GeneticAgent();
        setupUI();
        neuralAgent = new NeuralAgent();
        ga = new GeneticAlgorithm();

        initializeWorld();
        gameState = GameState.PLAYING;
        playerDirection = Direction.NORTH;
        defaultInfoBarMessage = Messages.INITIAL_MESSAGE_INFO;
        appendToLog(Messages.WELCOME_LOG);
        updatePerceptions();
        updateInfoBar(defaultInfoBarMessage);
    }

    private void setupUI() {
        Table hudRootTable = new Table(skin);
        hudRootTable.setFillParent(true);
        //hudRootTable.debug();

        Table topBarTable = new Table(skin);
        //logTable.setBackground("default-rect");
        logLabel = new Label("", skin);
        logLabel.setWrap(true);
        logScrollPane = new ScrollPane(logLabel, skin);
        logScrollPane.setFadeScrollBars(true);
        logScrollPane.setScrollingDisabled(true, false);
        topBarTable.add(logScrollPane).expand().fill().pad(5);

        toggleAgentButton = new TextButton("IA ON/OFF", skin);
        toggleAgentButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                isAgentPlaying = !isAgentPlaying;
                appendToLog("Intelligent Agent: " + (isAgentPlaying ? "[ACTIVATED]" : "[DEACTIVATED]"));
                agentActionTimer = 0f;
                setGameButtonsEnabled(!isAgentPlaying);
                mapButton.setDisabled(isAgentPlaying);
            }
        });
        toggleAgentButton.addListener(new ClickListener() {
            @Override public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) { updateInfoBar("Toggle between player and Intelligent Agent."); }
            @Override public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) { updateInfoBar(defaultInfoBarMessage); }
        });
        topBarTable.add(toggleAgentButton).width(40).height(35).pad(2).align(Align.right);

        hudRootTable.add(topBarTable).height(logHeight).expandX().fillX().padLeft(5).padRight(5).row();

        hudRootTable.add().expand().fill().row();

        Table buttonsContainerTable = new Table(skin);
        //buttonsContainerTable.setBackground("default-rect");
        buttonsContainerTable.align(Align.bottom);
        //buttonsContainerTable.defaults().pad(2);

        Table leftButtons = new Table(skin);
        leftButtons.defaults().width(40).height(35);
        leftButtons.align(Align.bottomLeft);
        moveButton = new TextButton("MOVE", skin);
        moveButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
               if (currentGameState.gameState == GameState.PLAYING) moveForward(currentGameState);
            }
        });
        moveButton.addListener(new ClickListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                updateInfoBar(Messages.MOVE_FORWARD_INFO);
            }
            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                updateInfoBar(defaultInfoBarMessage);
            }
        });
        turnLeftButton = new TextButton("T_L", skin);
        turnLeftButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (currentGameState.gameState == GameState.PLAYING) turnLeft(currentGameState);
            }
        });
        turnLeftButton.addListener(new ClickListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                updateInfoBar(Messages.TURN_LEFT_INFO);
            }
            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                updateInfoBar(defaultInfoBarMessage);
            }
        });
        turnRightButton = new TextButton("T_R", skin);
        turnRightButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (currentGameState.gameState == GameState.PLAYING) turnRight(currentGameState);
            }
        });
        turnRightButton.addListener(new ClickListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                updateInfoBar(Messages.TURN_RIGHT_INFO);
            }
            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                updateInfoBar(defaultInfoBarMessage);
            }
        });
        //leftButtons.add("").row();
        leftButtons.add(moveButton).colspan(2).row();
        leftButtons.add(turnLeftButton);
        leftButtons.add(turnRightButton).row();
        //leftButtons.add("").row();

        Table rightButtons = new Table(skin);
        rightButtons.defaults().width(40).height(35);
        rightButtons.align(Align.bottomRight);
        searchButton = new TextButton("SGD", skin);
        searchButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (currentGameState.gameState == GameState.PLAYING) searchForGold(currentGameState);
            }
        });
        searchButton.addListener(new ClickListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                if (gameState == GameState.PLAYING) updateInfoBar(Messages.SEARCH_INFO);
            }
            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                updateInfoBar(defaultInfoBarMessage);
            }
        });
        specialButton = new TextButton("SHT", skin);
        specialButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (currentGameState.gameState == GameState.PLAYING) shootArrow(currentGameState);
            }
        });
        specialButton.addListener(new ClickListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                updateInfoBar(Messages.SPECIAL_INFO);
            }
            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                updateInfoBar(defaultInfoBarMessage);
            }
        });
        mapButton = new TextButton("MAP", skin);
        mapButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (currentGameState.gameState == GameState.PLAYING) {
                    boolean mapVisible = !mapScreen.isVisible();
                    mapScreen.setVisible(mapVisible);
                    setGameButtonsEnabled(!mapVisible);
                }
            }
        });
        mapButton.addListener(new ClickListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                if (!mapScreen.isVisible()) updateInfoBar(Messages.MAP_INFO);
            }
            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                updateInfoBar(defaultInfoBarMessage);
            }
        });
        //rightButtons.add("").row();
        rightButtons.add(specialButton).colspan(2).row();
        rightButtons.add(searchButton);
        rightButtons.add(mapButton).row();
        //rightButtons.add("").row();

        buttonsContainerTable.add(leftButtons).expandX().align(Align.bottomLeft);
        buttonsContainerTable.add().expandX().fillX();
        buttonsContainerTable.add(rightButtons).expandX().align(Align.bottomRight).row();
//        buttonsContainerTable.add(leftButtons).width(BASE_VIRTUAL_WIDTH * 0.2f).expandY().fillY();
//        buttonsContainerTable.add().expandX().fillX();
//        buttonsContainerTable.add(rightButtons).width(BASE_VIRTUAL_WIDTH * 0.2f).expandY().fillY();

        hudRootTable.add(buttonsContainerTable).height(buttonsContainerHeight).expandX().fillX().row();

        //Table bottomBarTable = new Table(skin);
        //bottomBarTable.setBackground("default-rect");
        //bottomBarTable.pad(5);

        infoBarLabel = new ScrollingLabel(
            Messages.TIPS,
            skin,
            "default",
            Color.WHITE,
            20f,
            hudCamera
        );
        infoBarLabel.addListener(new ClickListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                infoBarLabel.scrollSpeed = 50f;
            }
            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                infoBarLabel.scrollSpeed = 20f;
            }
        });
        //bottomBarTable.add(infoBarLabel).expandX().fillX();
        hudRootTable.add(infoBarLabel).height(infoBarHeight).expandX().fillX().row();

        hudStage.addActor(hudRootTable);

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
                playerDirection = Direction.NORTH;
                restartButton.setVisible(false);
                logLabel.setText("Game restarted.");
                appendToLog(Messages.WELCOME_LOG);
                updatePerceptions();
                updateInfoBar(Messages.INITIAL_MESSAGE_INFO);
                setGameButtonsEnabled(!isAgentPlaying);
                mapButton.setDisabled(isAgentPlaying);
                mapScreen.setVisible(false);
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

    private void setGameButtonsEnabled(boolean enabled) {
        moveButton.setDisabled(!enabled);
        turnLeftButton.setDisabled(!enabled);
        turnRightButton.setDisabled(!enabled);
        searchButton.setDisabled(!enabled);
        specialButton.setDisabled(!enabled);
        // mapButton will be managed apart
        //mapButton.setDisabled(!enabled);
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
            playerTexture = new Texture(Gdx.files.internal("texture/player.png"));
            /*wumpusTexture = new Texture(Gdx.files.internal(""));
            pitTexture = new Texture(Gdx.files.internal(""));
            batTexture = new Texture(Gdx.files.internal(""));


            arrowTexture = new Texture(Gdx.files.internal(""));
            gameOverTexture = new Texture(Gdx.files.internal(""));
            gameWonTexture = new Texture(Gdx.files.internal(""));*/

            stenchTexture = new Texture(Gdx.files.internal("texture/stench.png"));
            breezeTexture = new Texture(Gdx.files.internal("texture/breeze.png"));
            glitterTexture = new Texture(Gdx.files.internal("texture/glitter.png"));

            goldTexture = new Texture(Gdx.files.internal("texture/gold.png"));
            hexRoomBaseTexture = new Texture(Gdx.files.internal("texture/hex_room_base.png"));
            hexPassageNorthTexture = new Texture(Gdx.files.internal("texture/hex_passage_north.png"));
            hexPassageEastTexture = new Texture(Gdx.files.internal("texture/hex_passage_east.png"));
            hexPassageSouthTexture = new Texture(Gdx.files.internal("texture/hex_passage_south.png"));
            hexPassageWestTexture = new Texture(Gdx.files.internal("texture/hex_passage_west.png"));

            eastArrowDirectionTexture = new Texture(Gdx.files.internal("texture/east_arrow_direction.png"));
            northArrowDirectionTexture = new Texture(Gdx.files.internal("texture/north_arrow_direction.png"));
            southArrowDirectionTexture = new Texture(Gdx.files.internal("texture/south_arrow_direction.png"));
            westArrowDirectionTexture = new Texture(Gdx.files.internal("texture/west_arrow_direction.png"));
            mapTexture = new Texture(Gdx.files.internal("texture/map-iso.png"));

        } catch (Exception e) {
            Gdx.app.error(WumpusGameScreen.class.getName(), "Error to load textures: " + e.getMessage());
            Gdx.app.exit();
        }
    }

    public void initializeWorld(){
        System.out.println(WORLD_SIZE + "  " + NUM_PITS);
        currentGameState = new WumpusWorldState();
        currentGameState.worldGrid = new char[WORLD_SIZE][WORLD_SIZE];
        for (int i=0;i<WORLD_SIZE;i++) {
            for (int j=0;j<WORLD_SIZE;j++) {
                currentGameState.worldGrid[i][j] = ' ';
            }
        }

        currentGameState.score = 0;
        currentGameState.visitedRooms = new HashSet<>();
        currentGameState.playerX = 0;
        currentGameState.playerY = 0;
        currentGameState.worldGrid[currentGameState.playerX][currentGameState.playerY] = 'P';
        currentGameState.visitedRooms.add(new Vector2(currentGameState.playerX, currentGameState.playerY));
        currentGameState.playerDirection = Direction.NORTH;
        currentGameState.hasGold = false;
        currentGameState.arrowsLeft = NUM_ARROWS;
        currentGameState.wumpusAlive = true;
        currentGameState.gameState = GameState.PLAYING;

        currentGameState.pitPositions = new Array<>();
        currentGameState.batPositions = new Array<>();

        boolean validLayout = false;
        int attempts = 0;
        while (!validLayout && attempts < 100) {
            attempts++;
            for (int i=0;i<WORLD_SIZE;i++) {
                for (int j=0;j<WORLD_SIZE;j++) {
                    currentGameState.worldGrid[i][j] = ' ';
                }
            }

            currentGameState.worldGrid[playerX][playerY] = 'P';

            currentGameState.wumpusX = -1;
            currentGameState.wumpusY = -1;
            currentGameState.pitPositions.clear();
            currentGameState.batPositions.clear();
            currentGameState.goldX = -1;
            currentGameState.goldY = -1;

            do {
                currentGameState.wumpusX = random.nextInt(WORLD_SIZE);
                currentGameState.wumpusY = random.nextInt(WORLD_SIZE);
            } while (currentGameState.wumpusX == 0 && currentGameState.wumpusY == 0);

            currentGameState.worldGrid[wumpusX][wumpusY] = 'W';

            for (int i=0;i<NUM_PITS;i++) {
                int x, y;
                do {
                    x = random.nextInt(WORLD_SIZE);
                    y = random.nextInt(WORLD_SIZE);
                } while (
                    (x == 0 && y == 0) ||
                    (x == currentGameState.wumpusX && y == currentGameState.wumpusY) ||
                    isPitAt(x,y, currentGameState) ||
                    isBatAt(x,y, currentGameState)
                );
                currentGameState.pitPositions.add(new Vector2(x, y));
                currentGameState.worldGrid[x][y] = 'H';
            }

            for (int i=0;i<NUM_BATS;i++) {
                int x, y;
                do {
                    x = random.nextInt(WORLD_SIZE);
                    y = random.nextInt(WORLD_SIZE);
                } while (
                    (x == 0 && y == 0) ||
                    (x == currentGameState.wumpusX && y == currentGameState.wumpusY) ||
                    isPitAt(x,y, currentGameState) ||
                    isBatAt(x,y, currentGameState)
                );
                currentGameState.batPositions.add(new Vector2(x, y));
                currentGameState.worldGrid[x][y] = 'B';
            }

            do {
                currentGameState.goldX = random.nextInt(WORLD_SIZE);
                currentGameState.goldY = random.nextInt(WORLD_SIZE);
            } while (
                (currentGameState.goldX == 0 && currentGameState.goldY == 0) ||
                (currentGameState.goldX == currentGameState.wumpusX && currentGameState.goldY == currentGameState.wumpusY) ||
                isPitAt(currentGameState.goldX, currentGameState.goldY, currentGameState) || isBatAt(goldX,goldY, currentGameState)
            );

            currentGameState.worldGrid[goldX][goldY] = 'G';

            validLayout = hasValidPathToGold(currentGameState) && hasValidPathToWumpus(currentGameState);

            if (!validLayout) {
                Gdx.app.log(WumpusGameScreen.class.getName(), "invalid layout, trying again...");
            }
        }
        if (attempts >= 100 && !validLayout) {
            Gdx.app.error(WumpusGameScreen.class.getName(), "no valid layout was possible.");
        } else {
            Gdx.app.log(WumpusGameScreen.class.getName(), "valid layout generated after " + attempts + " attempts");
        }
        //geneticAgent.initializeKnowledgeBase();
        neuralAgent.initializeNetwork();
        //GENETIC ALGORITHM=============================================================================

        //ga.initializePopulation(this);
        //ga.run();
    }

    private boolean isPitAt(int x, int y, WumpusWorldState state) {
        for (Vector2 pit : state.pitPositions) {
            if (pit.x == x && pit.y == y) return true;
        }
        return false;
    }

    private boolean isBatAt(int x, int y, WumpusWorldState state) {
        for (Vector2 bat : state.batPositions) {
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

    private boolean hasValidPathToGold(WumpusWorldState state) {
        return findPath(state.playerX, state.playerY, state.goldX, state.goldY);
    }

    private  boolean hasValidPathToWumpus(WumpusWorldState state) {
        if (!state.wumpusAlive) return true;

        return findPath(state.playerX, state.playerY, state.wumpusX, state.wumpusY);
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
                currentGameState.worldGrid[newX][newY] != 'H'
            ) neighbors.add(new Node(newX, newY));
        }
        return neighbors;
    }

    @Override
    public void render() {
        if (currentGameState.gameState == GameState.PLAYING && isAgentPlaying) {
            agentActionTimer -= Gdx.graphics.getDeltaTime();
            if (agentActionTimer <= 0) {
                neuralAgent.decideAndPerformAction(); // Agente decide e executa a próxima ação
                //agent.decideAndPerformAction();
                //agentV2.decideAndPerformAction();
                agentActionTimer = AGENT_ACTION_DELAY;
            }
        }
        GdxUtils.clearScreen();

        debugCameraController.inputDebugHandle(Gdx.graphics.getDeltaTime());
        debugCameraController.applyToCamera(gameplayCamera);

        gameplayCamera.update();
        gameplayViewport.apply();

        batch.setProjectionMatrix(gameplayCamera.combined);

        batch.begin();
        drawHexagonalRoom(currentGameState);
        batch.end();

        hudStage.act(Gdx.graphics.getDeltaTime());
        hudStage.draw();

        if (currentGameState.gameState == GameState.GAME_OVER || currentGameState.gameState == GameState.GAME_WON) {
            restartButton.setVisible(true);
            setGameButtonsEnabled(false);
            mapButton.setDisabled(true);
            mapScreen.setVisible(false);
            // O botão do agente permanece habilitado para permitir que ele continue aprendendo
            // ou seja desativado manualmente.
        } else {
            restartButton.setVisible(false);
            if (isAgentPlaying) { // Se o agente está jogando, desabilita os botões manuais
                setGameButtonsEnabled(false);
                mapButton.setDisabled(false);
            } else { // Se o jogador está no controle, habilita botões manuais e mapa
                setGameButtonsEnabled(true);
                mapButton.setDisabled(false);
                if (mapScreen.isVisible()){ // Se o mapa está aberto, desabilita todos os outros botões de gameplay
                    setGameButtonsEnabled(false);
                }
            }
            toggleAgentButton.setDisabled(false); // Habilita o botão do agente durante o jogo
        }
    }

    private void drawHexagonalRoom(WumpusWorldState state) {
        // Dimensões da viewport de gameplay (EFFECTIVE_GAMEPLAY_VIRTUAL_WIDTH/HEIGHT)
        float viewWidth = gameplayViewport.getWorldWidth(); // 320
        float viewHeight = gameplayViewport.getWorldHeight() - logHeight; // 165

        // A hexRoomBaseTexture será desenhada para preencher a altura da viewport
        // e terá uma largura igual à sua altura, já que a imagem fornecida é 1:1.
        float hexRoomDrawWidth = hexRoomBaseTexture.getWidth(); // 165px
        float hexRoomDrawHeight = hexRoomBaseTexture.getHeight(); // 165px

        // Centraliza a textura da sala hexagonal dentro da viewport de gameplay
        // A textura será desenhada a partir do canto inferior esquerdo.
        float renderX = gameplayCamera.position.x - hexRoomDrawWidth / 2f;
        float renderY = gameplayCamera.position.y - hexRoomDrawHeight / 2f;

        // Desenha a textura base da sala hexagonal
        batch.draw(hexRoomBaseTexture, renderX, renderY, hexRoomDrawWidth, hexRoomDrawHeight);

        // --- Desenha Overlays de Passagem (se houver) ---
        // As posições e tamanhos são baseados nas proporções da hexRoomBaseTexture (165x165)
        // e nas suas especificações de posicionamento nos cantos da sala.
        // Esses valores são estimativas e podem precisar de ajustes finos com sua arte final.

        // Tamanhos de referência para as texturas de passagem
        /*float passageWallWidth = hexRoomDrawWidth * 0.24f;  // ~40px
        float passageWallHeight = hexRoomDrawHeight * 0.15f; // ~25px

        float passageFloorWidth = hexRoomDrawWidth * 0.21f; // ~35px
        float passageFloorHeight = hexRoomDrawHeight * 0.12f; // ~20px
        */
        // Passagem Norte (canto superior esquerdo, junto à parede esquerda)
        if (isValidCell(state.playerX, state.playerY + 1)) {
//            float northPassageX = renderX + hexRoomDrawWidth * 0.05f; // Mais à esquerda
//            float northPassageY = renderY + hexRoomDrawHeight * 0.65f; // Mais para cima
//            batch.draw(hexPassageNorthTexture, northPassageX, northPassageY, passageWallWidth, passageWallHeight);
            batch.draw(hexPassageNorthTexture, renderX, renderY, hexRoomDrawWidth, hexRoomDrawHeight);
        }
        // Passagem Leste (canto superior direito, junto à parede direita)
        if (isValidCell(state.playerX + 1, state.playerY)) {
//            float eastPassageX = renderX + hexRoomDrawWidth * 0.70f; // Mais à direita
//            float eastPassageY = renderY + hexRoomDrawHeight * 0.65f; // Mais para cima
//            batch.draw(hexPassageEastTexture, eastPassageX, eastPassageY, passageWallWidth, passageWallHeight);
            batch.draw(hexPassageEastTexture, renderX, renderY, hexRoomDrawWidth, hexRoomDrawHeight);
        }
        // Passagem Sul (canto inferior direito)
        if (isValidCell(state.playerX, state.playerY - 1)) {
//            float southPassageX = renderX + hexRoomDrawWidth * 0.60f; // Mais à direita
//            float southPassageY = renderY + hexRoomDrawHeight * 0.05f; // Mais para baixo
//            batch.draw(hexPassageSouthTexture, southPassageX, southPassageY, passageFloorWidth, passageFloorHeight);
            batch.draw(hexPassageSouthTexture, renderX, renderY, hexRoomDrawWidth, hexRoomDrawHeight);
        }
        // Passagem Oeste (canto inferior esquerdo)
        if (isValidCell(state.playerX - 1, state.playerY)) {
//            float westPassageX = renderX + hexRoomDrawWidth * 0.15f; // Mais à esquerda
//            float westPassageY = renderY + hexRoomDrawHeight * 0.05f; // Mais para baixo
//            batch.draw(hexPassageWestTexture, westPassageX, westPassageY, passageFloorWidth, passageFloorHeight);
            batch.draw(hexPassageWestTexture, renderX, renderY, hexRoomDrawWidth, hexRoomDrawHeight);
        }
        if (isStench(state.playerX, state.playerY, state)){
            batch.draw(stenchTexture, renderX, renderY, hexRoomDrawWidth, hexRoomDrawHeight);
        }
        if (isBreeze(state.playerX, state.playerY, state)){
            batch.draw(breezeTexture, renderX, renderY, hexRoomDrawWidth, hexRoomDrawHeight);
        }
        /*

        // --- Desenha Jogador e Itens ---
        // Tamanho do jogador e itens, proporcionais à nova dimensão da sala hexagonal
        float playerDrawSize = hexRoomDrawWidth * 0.2f; // ~33 pixels (para 165x165px base)
        float itemDrawSize = playerDrawSize * 0.75f;    // ~24 pixels

        // Posição central do chão da sala na perspectiva isométrica.
        // O jogador ficará centralizado no chão. O item ficará "logo abaixo" dele.
        // Estes são offsets estimados da arte fornecida.
        float roomFloorCenterX = renderX + hexRoomDrawWidth * 0.49f; // Ajustado para o centro visual do chão
        float roomFloorCenterY = renderY + hexRoomDrawHeight * 0.28f; // Ajustado para o centro visual do chão

        // Posição do jogador
        float playerDrawX = roomFloorCenterX - playerDrawSize / 2f;
        float playerDrawY = roomFloorCenterY - playerDrawSize / 2f;
        */
        switch (state.playerDirection) {
            case NORTH:
                batch.draw(northArrowDirectionTexture, renderX, renderY, hexRoomDrawWidth, hexRoomDrawHeight);
                break;
            case EAST:
                batch.draw(eastArrowDirectionTexture, renderX, renderY, hexRoomDrawWidth, hexRoomDrawHeight);
                break;
            case SOUTH:
                batch.draw(southArrowDirectionTexture, renderX, renderY, hexRoomDrawWidth, hexRoomDrawHeight);
                break;
            case WEST:
                batch.draw(westArrowDirectionTexture, renderX, renderY, hexRoomDrawWidth, hexRoomDrawHeight);
                break;
        }
        batch.draw(playerTexture,renderX, renderY, hexRoomDrawWidth, hexRoomDrawHeight);
        /*
        // Posição do item (abaixo do jogador)
        float itemDrawX = roomFloorCenterX - itemDrawSize / 2f;
        float itemDrawY = roomFloorCenterY - itemDrawSize / 2f - (playerDrawSize * 0.1f); // Ligeiramente abaixo do jogador

        if (playerX == wumpusX && playerY == wumpusY && wumpusAlive) {
            batch.draw(wumpusTexture, itemDrawX, itemDrawY, itemDrawSize, itemDrawSize);
        }
        */
        if (state.playerX == state.goldX && state.playerY == state.goldY && !state.hasGold) {
            //batch.draw(goldTexture, itemDrawX, itemDrawY, itemDrawSize, itemDrawSize);
            batch.draw(glitterTexture, renderX, renderY, hexRoomDrawWidth, hexRoomDrawHeight);
        }
        /*
        if (isPitAt(playerX, playerY)) {
            batch.draw(pitTexture, itemDrawX, itemDrawY, itemDrawSize, itemDrawSize);
        }
        if (isBatAt(playerX, playerY)) {
            batch.draw(batTexture, itemDrawX, itemDrawY, itemDrawSize, itemDrawSize);
        }*/
    }

    private void drawIsometricRoom() {
        float gameAreaWidth = gameplayViewport.getWorldWidth();
        float gameAreaHeight = gameplayViewport.getWorldHeight();

        float renderX = gameplayCamera.position.x - gameAreaWidth / 2f;
        float renderY = gameplayCamera.position.y - gameAreaHeight / 2f;

        batch.draw(roomFloorTexture, renderX, renderY, gameAreaWidth, gameAreaHeight);
        float wallWidth = gameAreaWidth * 0.3f;
        float wallHeight = gameAreaHeight * 0.2f;
        float passageWidth = gameAreaWidth * 0.2f;
        float passageHeight = gameAreaHeight * 0.3f;
        float playerSize = gameAreaWidth * 0.2f;

        // draw north wall
        if (isValidCell(playerX, playerY + 1) && !isPitAt(playerX, playerY + 1, currentGameState) && !isBatAt(playerX, playerY + 1, currentGameState)) {
            batch.draw(
                passageTexture,
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
        if (isValidCell(playerX, playerY - 1) && !isPitAt(playerX, playerY - 1, currentGameState) && !isBatAt(playerX, playerY - 1, currentGameState)) {
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
        if (isValidCell(playerX + 1, playerY) && !isPitAt(playerX + 1, playerY,currentGameState) && !isBatAt(playerX + 1, playerY, currentGameState)) {
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
        if (isValidCell(playerX - 1, playerY) && !isPitAt(playerX - 1, playerY, currentGameState) && !isBatAt(playerX - 1, playerY, currentGameState)) {
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
        if (isPitAt(playerX, playerY, currentGameState)) {
            batch.draw(pitTexture,
                renderX + gameAreaWidth * 0.45f - itemSize / 2,
                renderY + gameAreaHeight * 0.45f - itemSize / 2,
                itemSize,
                itemSize
            );
        }
        // draw bat
        if (isBatAt(playerX, playerY, currentGameState)) {
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

    public void moveForward(WumpusWorldState state) {
        if (state.gameState != GameState.PLAYING) return;
        int newX = state.playerX;
        int newY = state.playerY;

        switch (state.playerDirection) {
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
            state.score--;
            state.worldGrid[playerX][playerY] = ' ';
            state.playerX = newX;
            state.playerY = newY;
            state.worldGrid[playerY][playerY] = 'P';
            appendToLog(Messages.MOVE_ACTION);
            state.visitedRooms.add(new Vector2(state.playerX, state.playerY));
            checkRoomContent(state);
            updatePerceptions();
        } else {
            state.score -= 10;
            appendToLog(Messages.AGENT_HIT_WALL);
        }
    }

    public void turnLeft(WumpusWorldState state) {
        if (state.gameState != GameState.PLAYING) return;
        state.score -= 10;
        switch (state.playerDirection) {
            case NORTH:
                state.playerDirection = Direction.WEST;
                break;
            case EAST:
                state.playerDirection = Direction.NORTH;
                break;
            case SOUTH:
                state.playerDirection = Direction.EAST;
                break;
            case WEST:
                state.playerDirection = Direction.SOUTH;
                break;
        }
        appendToLog(Messages.TURN_LEFT_ACTION + " " + Messages.YOU_ARE_FACING + " [" + playerDirection + "]");
        updatePerceptions();
    }

    public void turnRight(WumpusWorldState state) {
        state.score -=10;
        if (state.gameState != GameState.PLAYING) return;
        switch (state.playerDirection) {
            case NORTH:
                state.playerDirection = Direction.EAST;
                break;
            case EAST:
                state.playerDirection = Direction.SOUTH;
                break;
            case SOUTH:
                state.playerDirection = Direction.WEST;
                break;
            case WEST:
                state.playerDirection = Direction.NORTH;
                break;
        }
        appendToLog(Messages.TURN_RIGHT_ACTION + " " + Messages.YOU_ARE_FACING + " [" + playerDirection + "]");
        updatePerceptions();
    }

    public void shootArrow(WumpusWorldState state) {
        if (state.gameState != GameState.PLAYING) return;
        if (state.arrowsLeft > 0) {
            String message = Messages.SHOOT_ARROW_ACTION;
            state.arrowsLeft--;
            state.score -= 50;

            int currentX = state.playerX;
            int currentY = state.playerY;
            int dx = 0, dy = 0;

            switch (state.playerDirection) {
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

                if (currentX == state.wumpusX && currentY == state.wumpusY && state.wumpusAlive) {
                    //appendToLog(Messages.ARROW_HIT_WUMPUS);
                    message = message.concat(Messages.ARROW_HIT_WUMPUS);
                    state.wumpusAlive = false;
                    state.score += 1000;
                    state.worldGrid[wumpusX][wumpusY] = ' ';
                    if (state.hasGold && state.playerX == 0 && state.playerY == 0) {
                        state.gameState = GameState.GAME_WON;
                        appendToLog(Messages.AGENT_WON_LOG);
                        appendToLog(Messages.GAME_OVER_MESSAGE);
                        //appendToLog(Messages.RESET_MESSAGE);
                        updatePerceptions();
                        return;
                    }
                }
            }
            //message = message.concat(Messages.ARROW_HIT_WALL);
            appendToLog(message);
        } else {
            appendToLog(Messages.NO_ARROW);
        }
    }

    public void searchForGold(WumpusWorldState state) {
        if (state.gameState != GameState.PLAYING) return;
        if (state.playerX == state.goldX && state.playerY == state.goldY && !state.hasGold) {
            state.hasGold = true;
            state.score += 1000;
            state.worldGrid[goldX][goldY] = ' ';
            appendToLog(Messages.FOUND_GOLD);
            defaultInfoBarMessage = Messages.GOT_GOLD_INFO;
            updateInfoBar(defaultInfoBarMessage);
            updatePerceptions();
        } else if (state.hasGold) {
            state.score -= 50;
            appendToLog("You already got the gold");
        } else {
            appendToLog(Messages.FOUND_NOTHING);
            state.score -= 50;
        }
    }

    private void checkRoomContent(WumpusWorldState state) {
        if (isPitAt(state.playerX, state.playerY, state)) {
            state.gameState = GameState.GAME_OVER;
            appendToLog(Messages.AGENT_FELL_PIT);
            state.score -= 1000;
            appendToLog(Messages.GAME_OVER_MESSAGE + score);
            //appendToLog(Messages.RESET_MESSAGE);
//        } else if (isBatAt(playerX,playerY)) {
//            teleportPlayer();
//            appendToLog("You got teleported by a bat");
        } else if (state.playerX == state.wumpusX && state.playerY == state.wumpusY && state.wumpusAlive) {
            state.gameState = GameState.GAME_OVER;
            appendToLog(Messages.AGENT_CAUGHT_BY_WUMPUS);
            state.score -= 1000;
            appendToLog(Messages.GAME_OVER_MESSAGE + score);
            //appendToLog(Messages.RESET_MESSAGE);
        } else if (state.hasGold && state.playerX == 0 && state.playerY == 0) {
            state.gameState = GameState.GAME_WON;
            appendToLog(Messages.AGENT_WON_LOG);
            appendToLog(Messages.GAME_OVER_MESSAGE + score);
            //appendToLog(Messages.RESET_MESSAGE);
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
        if (currentGameState.gameState == GameState.PLAYING) {
            String perceptions = Messages.YOU_FEEL;
            boolean sensedSomething = false;

            if (isStench(currentGameState.playerX, currentGameState.playerY, currentGameState)) {
                perceptions += Messages.STENCH;
                sensedSomething = true;
            }
            if (isBreeze(currentGameState.playerX, currentGameState.playerY, currentGameState)) {
                perceptions += Messages.BREEZE;
                sensedSomething = true;
            }
            if (isGlitter(currentGameState)) {
                perceptions += Messages.GLITTER;
                sensedSomething = true;
            }
            if (!sensedSomething) {
                perceptions += Messages.NOTHING;
            }
            appendToLog(perceptions);

            mapScreen.update(
                currentGameState.playerX,
                currentGameState.playerY,
                currentGameState.visitedRooms,
                currentGameState.wumpusAlive,
                currentGameState.hasGold,
                currentGameState.arrowsLeft,
                currentGameState.score
            );
        }
    }

    public boolean isStench(int x, int y, WumpusWorldState state) {
        if (!state.wumpusAlive) return false;
        int [] dx = {0,0,1,-1};
        int [] dy = {1,-1,0,0};

        for (int i=0;i<4;i++){
            int checkX = x + dx[i];
            int checkY = y + dy[i];
            if (isValidCell(checkX, checkY) && checkX == state.wumpusX && checkY == state.wumpusY) return true;
        }
        return false;
    }

    public boolean isBreeze(int x, int y, WumpusWorldState state) {
        int [] dx = {0,0,1,-1};
        int [] dy = {1,-1,0,0};

        for (Vector2 pit : state.pitPositions) {
            for (int i=0;i<4;i++){
                int checkX = x + dx[i];
                int checkY = y + dy[i];
                if (isValidCell(checkX, checkY) && checkX == pit.x && checkY == pit.y) return true;
            }
        }
        return false;
    }

    public boolean isGlitter(WumpusWorldState state) {
        return state.playerX == state.goldX && state.playerY == state.goldY && !state.hasGold;
    }

    private void resetWorldForAgent() {
        currentGameState.playerX = 0;
        currentGameState.playerY = 0;
        currentGameState.playerDirection = Direction.NORTH;
        currentGameState.hasGold = false; // Agente sempre começa sem o ouro
        currentGameState.arrowsLeft = NUM_ARROWS;
        currentGameState.wumpusAlive = true; // Wumpus reaparece
        currentGameState.score = 0;
        currentGameState.gameState = GameState.PLAYING;
        currentGameState.visitedRooms.clear();
        currentGameState.visitedRooms.add(new Vector2(currentGameState.playerX, currentGameState.playerY));

        // Re-popula o grid com base nas posições originais dos perigos e ouro
        for (int i = 0; i < WORLD_SIZE; i++) {
            for (int j = 0; j < WORLD_SIZE; j++) {
                currentGameState.worldGrid[i][j] = ' ';
            }
        }
        currentGameState.worldGrid[currentGameState.playerX][currentGameState.playerY] = 'P';

        // Lógica para reposicionar o ouro se o agente morreu com ele
        if (shouldRepositionGoldAfterDeath) {
            currentGameState.goldX = repositionGoldTargetX;
            currentGameState.goldY = repositionGoldTargetY;
            appendToLog("Agente (NN): Ouro reposicionado em (" + currentGameState.goldX + ", " + currentGameState.goldY + ").");
            shouldRepositionGoldAfterDeath = false; // Reset da flag
            repositionGoldTargetX = -1; // Reset da posição alvo
            repositionGoldTargetY = -1; // Reset da posição alvo
        }
        // Garante que o ouro esteja no grid na sua posição (original ou reposicionada)
        currentGameState.worldGrid[currentGameState.goldX][currentGameState.goldY] = 'G';


        // Garante que o Wumpus esteja no grid
        currentGameState.worldGrid[currentGameState.wumpusX][currentGameState.wumpusY] = 'W';
        //worldGraph.getNode(currentGameState.wumpusX, currentGameState.wumpusY).isObstacle = false; // Garante que não seja um obstáculo se o Wumpus estiver vivo

        // Garante que os poços e morcegos estejam no grid
        for (Vector2 pit : currentGameState.pitPositions) {
            currentGameState.worldGrid[(int)pit.x][(int)pit.y] = 'H';
            //worldGraph.getNode((int)pit.x, (int)pit.y).isObstacle = true; // Garante que seja um obstáculo
        }
        /*for (Vector2 bat : currentGameState.batPositions) {
            currentGameState.worldGrid[(int)bat.x][(int)bat.y] = 'B';
        }*/

        appendToLog("Agente (NN): Reiniciando jogo na sala (0,0) para continuar o aprendizado.");
        updatePerceptions();
    }

    @Override
    public void resize(int width, int height) {
        gameplayViewport.update(width, height, true);
        gameplayCamera.position.set(gameplayViewport.getWorldWidth() / 2f, gameplayViewport.getWorldHeight() / 2f, 0);
        gameplayCamera.update();

        float aspectRatio = (float)width / height;
        float hudVirtualWidth = BASE_VIRTUAL_HEIGHT * aspectRatio;
        hudViewport.setWorldSize(hudVirtualWidth, BASE_VIRTUAL_HEIGHT);
        hudViewport.update(width, height, true);
        hudCamera.position.set(hudViewport.getWorldWidth() / 2f, hudViewport.getWorldHeight() / 2f, 0);
        infoBarLabel.refreshLayout();
        hudCamera.update();


        restartButton.setPosition(
            hudViewport.getWorldWidth() / 2f - restartButton.getWidth() / 2f,
            hudViewport.getWorldHeight() / 2f - restartButton.getHeight() / 2f
        );
    }

    @Override
    public void dispose() {

        playerTexture.dispose();
        /*wumpusTexture.dispose();
        pitTexture.dispose();
        arrowTexture.dispose();
        gameOverTexture.dispose();
        gameWonTexture.dispose();
        roomFloorTexture.dispose();
        passageTexture.dispose();
        batTexture.dispose();*/
        goldTexture.dispose();
        stenchTexture.dispose();
        breezeTexture.dispose();
        glitterTexture.dispose();

        hexRoomBaseTexture.dispose();
        hexPassageNorthTexture.dispose();
        hexPassageEastTexture.dispose();
        hexPassageSouthTexture.dispose();
        hexPassageWestTexture.dispose();
        mapTexture.dispose();

        mapScreen.dispose(); // Descarta os recursos da tela do mapa
        batch.dispose();
        gameplayStage.dispose();
        hudStage.dispose();
        skin.dispose();
        font.dispose();
    }

    private static class ScrollingLabel extends Actor {
        private final BitmapFont font;
        private final Color color;
        private final String fixedPrefix = Messages.INFO;
        private final float fixedPrefixWidth;
        private String scrollingText;
        private float scrollingTextWidth;
        private float scrollSpeed;
        private float currentXOffset;
        private final GlyphLayout layout;
        private final float padding = 10;
        private final Camera camera;

        public ScrollingLabel(
            String scrollingText,
            Skin skin,
            String fontStyleName,
            Color color,
            float scrollSpeed,
            Camera camera
        ) {
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
            Rectangle clipBounds = new Rectangle(clipX, clipY, clipWidth, clipHeight);
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

        public void refreshLayout() {
            setText(this.scrollingText);
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
            //statusTable.setDebug(true);
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
            add(statusTable).expandY().fillY().pad(10); // cell #0
            add().expand().fill(); // Empty cell for the map (drawn directly in draw). cell #1
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

            float miniCellSize = 48;

            float mapAreaWidth = getWidth() - getCells().get(0).getPrefWidth() - getPadLeft() - getPadRight();
            float mapAreaHeight = getHeight() - getPadTop() - getPadBottom();

            float mapOffsetX = getX() + getCells().get(0).getPrefWidth() + (mapAreaWidth / 2f);
            float mapOffsetY = getY() + getPadBottom() + (mapAreaHeight / worldSize);

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
                        shapeRenderer.setColor(1,1,1, flashingAlpha);
                        shapeRenderer.circle(vertices[0], vertices[1] - miniCellSize/4f, miniCellSize/8f);
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

    public class ReactiveAgent {
        public enum Action {
            MOVE_FORWARD,
            TURN_LEFT,
            TURN_RIGHT,
            SHOOT_ARROW,
            GRAB_GOLD,
            NO_ACTION
        }

        public ReactiveAgent() { }

        /**
         * O método principal onde o agente decide e executa uma ação.
         * A decisão é baseada em um conjunto de regras priorizadas.
         */
        public void decideAndPerformAction() {
            // Sentir o ambiente (percepções diretas do jogo, acessadas diretamente)
            boolean hasStench = isStench(playerX, playerY, currentGameState);
            boolean hasBreeze = isBreeze(playerX, playerY, currentGameState);
            boolean hasGlitter = isGlitter(currentGameState);
            boolean wumpusAlive = WumpusGameScreen.this.wumpusAlive;
            int arrowsLeft = WumpusGameScreen.this.arrowsLeft;
            boolean hasGold = WumpusGameScreen.this.hasGold;
            int playerX = WumpusGameScreen.this.playerX;
            int playerY = WumpusGameScreen.this.playerY;
            Direction playerDirection = WumpusGameScreen.this.playerDirection;

            // Adicionar a localização atual às salas visitadas (memória mínima)
            visitedRooms.add(new Vector2(playerX, playerY));

            // Decidir a ação com base nas percepções (regras priorizadas)

            // Regra 1: Se há brilho, pegar o ouro.
            if (hasGlitter) {
                searchForGold(currentGameState);
                //appendToLog("Agente: Peguei o ouro!");
                return;
            }

            // Regra 2: Se o agente tem ouro e está na sala de entrada (0,0), o objetivo foi alcançado.
            if (hasGold && playerX == 0 && playerY == 0) {
                //appendToLog("Agente: Voltei para a entrada com o ouro. Missão cumprida!");
                return;
            }

            // Regra 3: Se há fedor, o Wumpus está vivo e o agente tem flechas, atirar.
            if (hasStench) {
                if (arrowsLeft > 0) {
                    shootArrow(currentGameState);
                }
                else {
                    goBack();
                    moveForward(currentGameState);
                }
                //appendToLog("Agente: Senti fedor, atirei uma flecha!");
                return;
            }

            // Regra 4: Se há brisa (poço próximo), virar para evitar o perigo.
            if (hasBreeze) {
                turnInto();
                moveForward(currentGameState);
                //appendToLog("Agente: Senti brisa, virei para a esquerda.");
                return;
            }

            // Regra 5: Nenhuma percepção imediata de perigo/ouro. Tentar explorar.
            int nextX = playerX;
            int nextY = playerY;
            switch (playerDirection) {
                case NORTH: nextY = playerY + 1; break;
                case EAST: nextX = playerX + 1; break;
                case SOUTH: nextY = playerY - 1; break;
                case WEST: nextX = playerX - 1; break;
            }

            // Verifica se a próxima célula é válida e ainda não foi visitada
            if (isValidCell(nextX, nextY)) {
                moveForward(currentGameState);
                //appendToLog("Agente: Movi para frente em (" + nextX + "," + nextY + ").");
            } else {
                // Se não puder mover para frente (parede ou célula já visitada), virar para tentar outra direção.
                turnInto();
                //appendToLog("Agente: Não pude mover para frente, virei para a esquerda.");
            }
        }

        private void turnInto() {
            // 0: left, 1: right
            if (random.nextBoolean()) turnLeft(currentGameState);
            else turnRight(currentGameState);
        }
        private void goBack() {
            turnLeft(currentGameState);
            turnLeft(currentGameState);
            moveForward(currentGameState);
        }
    }

    public class ReactiveAgentV2 {
        public enum Action {
            MOVE_FORWARD,
            TURN_LEFT,
            TURN_RIGHT,
            SHOOT_ARROW,
            GRAB_GOLD,
            NO_ACTION
        }

        // Classe interna para representar o conhecimento do agente sobre cada célula do mundo
        private class AgentWorldCell {
            boolean visited;
            boolean hasBreezePercept; // Agente percebeu brisa nesta célula
            boolean hasStenchPercept; // Agente percebeu fedor nesta célula
            boolean hasGlitterPercept; // Agente percebeu brilho nesta célula
            boolean isSafeFromPit;    // Crença do agente: é seguro de poço
            boolean isSafeFromWumpus; // Crença do agente: é seguro de Wumpus
            boolean isKnownPit;       // Se o agente inferiu com certeza a presença de um poço
            boolean isKnownWumpus;    // Se o agente inferiu com certeza a presença de um Wumpus

            public AgentWorldCell() {
                visited = false;
                hasBreezePercept = false;
                hasStenchPercept = false;
                hasGlitterPercept = false;
                isSafeFromPit = false; // Inicialmente desconhecido, marcado true se percebido seguro
                isSafeFromWumpus = false; // Inicialmente desconhecido, marcado true se percebido seguro
                isKnownPit = false;
                isKnownWumpus = false;
            }
        }

        private AgentWorldCell[][] knowledgeBase; // Mapa interno do agente

        public ReactiveAgentV2() {
            initializeKnowledgeBase();
        }

        public void initializeKnowledgeBase() {
            knowledgeBase = new AgentWorldCell[WORLD_SIZE][WORLD_SIZE];
            for (int x = 0; x < WORLD_SIZE; x++) {
                for (int y = 0; y < WORLD_SIZE; y++) {
                    knowledgeBase[x][y] = new AgentWorldCell();
                }
            }
            // A sala de entrada (0,0) é sempre segura e visitada.
            if (isValidCell(0,0)) {
                knowledgeBase[0][0].visited = true;
                knowledgeBase[0][0].isSafeFromPit = true;
                knowledgeBase[0][0].isSafeFromWumpus = true;
            }
        }

        /**
         * O método principal onde o agente decide e executa uma ação.
         * A decisão é baseada em um conjunto de regras priorizadas,
         * utilizando as percepções atuais e o conhecimento registrado.
         *
         * @return A ação decidida pelo agente para este turno.
         */
        public Action decideAndPerformAction() {
            // 1. Sentir o ambiente e Atualizar o Modelo Interno (Base de Conhecimento)
            boolean currentHasStench = WumpusGameScreen.this.isStench(playerX, playerY, currentGameState);
            boolean currentHasBreeze = WumpusGameScreen.this.isBreeze(playerX, playerY, currentGameState);
            boolean currentHasGlitter = WumpusGameScreen.this.isGlitter(currentGameState);

            // Marcar a célula atual como visitada e registrar as percepções
            knowledgeBase[playerX][playerY].visited = true;
            knowledgeBase[playerX][playerY].hasBreezePercept = currentHasBreeze;
            knowledgeBase[playerX][playerY].hasStenchPercept = currentHasStench;
            knowledgeBase[playerX][playerY].hasGlitterPercept = currentHasGlitter;

            // Propagar informações de segurança para vizinhos não visitados
            int[] dx = {0, 0, 1, -1};
            int[] dy = {1, -1, 0, 0};

            for (int i = 0; i < 4; i++) {
                int nx = playerX + dx[i];
                int ny = playerY + dy[i];

                if (isValidCell(nx, ny) && !knowledgeBase[nx][ny].visited) {
                    AgentWorldCell neighborCell = knowledgeBase[nx][ny];

                    // Se a célula atual NÃO tem Brisa, seus vizinhos são seguros de Poços
                    if (!currentHasBreeze) {
                        neighborCell.isSafeFromPit = true;
                    }
                    // Se a célula atual NÃO tem Fedor, seus vizinhos são seguros de Wumpus
                    if (!currentHasStench) {
                        neighborCell.isSafeFromWumpus = true;
                    }
                    // Nota: Se a célula atual *tem* Brisa/Fedor, o vizinho é *potencialmente* perigoso.
                    // Para um agente reativo simples, não tentaremos triangulação complexa aqui.
                    // A ausência de percept é que nos dá certeza de segurança.
                }
            }

            // 2. Seleção de Ação (Prioridades)

            // A) Objetivo Imediato: Pegar Ouro
            if (currentHasGlitter && !hasGold) {
                WumpusGameScreen.this.searchForGold(currentGameState);
                WumpusGameScreen.this.appendToLog("Agente (Modelo): Peguei o ouro!");
                return Action.GRAB_GOLD;
            }

            // B) Condição de Vitória: Voltar para a entrada com o ouro
            if (hasGold && playerX == 0 && playerY == 0) {
                WumpusGameScreen.this.appendToLog("Agente (Modelo): Voltei para a entrada com o ouro. Missão cumprida!");
                return Action.NO_ACTION;
            }

            // C) Lidar com a Ameaça do Wumpus (tentativa de inferência e tiro)
            if (currentHasStench && wumpusAlive && arrowsLeft > 0) {
                // Tenta encontrar um vizinho não visitado que não é seguro de Wumpus
                // e que está na linha de tiro atual do agente.
                int targetShootX = -1;
                int targetShootY = -1;

                int checkX = playerX;
                int checkY = playerY;

                int dirX = 0, dirY = 0;
                switch(playerDirection) {
                    case NORTH: dirY = 1; break;
                    case EAST: dirX = 1; break;
                    case SOUTH: dirY = -1; break;
                    case WEST: dirX = -1; break;
                }

                // Verifica na direção atual para onde o Wumpus pode estar (1 célula à frente)
                checkX += dirX;
                checkY += dirY;

                if (isValidCell(checkX, checkY) && !knowledgeBase[checkX][checkY].isSafeFromWumpus) {
                    targetShootX = checkX;
                    targetShootY = checkY;
                } else { // Se não na direção atual, procura por qualquer vizinho suspeito
                    for (int i = 0; i < 4; i++) {
                        int nx = playerX + dx[i];
                        int ny = playerY + dy[i];
                        if (isValidCell(nx, ny) && !knowledgeBase[nx][ny].visited && !knowledgeBase[nx][ny].isSafeFromWumpus) {
                            targetShootX = nx;
                            targetShootY = ny;
                            break; // Encontrou um possível alvo
                        }
                    }
                }

                if (targetShootX != -1) { // Se encontrou um alvo para atirar
                    if (!isFacing(targetShootX, targetShootY)) {
                        return turnTowards(targetShootX, targetShootY); // Primeiro vira para o alvo
                    }
                    WumpusGameScreen.this.shootArrow(currentGameState);
                    WumpusGameScreen.this.appendToLog("Agente (Modelo): Senti fedor, atirei uma flecha na direção " + playerDirection + "!");
                    return Action.SHOOT_ARROW;
                } else { // Se não encontrou um alvo claro para atirar, mas ainda tem flechas
                    WumpusGameScreen.this.shootArrow(currentGameState); // Atira cegamente
                    WumpusGameScreen.this.appendToLog("Agente (Modelo): Senti fedor, atirei uma flecha (sem alvo claro inferido).");
                    return Action.SHOOT_ARROW;
                }
            }

            // D) Explorar Células Seguras e Não Visitadas
            List<Vector2> safeUnvisitedNeighbors = new ArrayList<>();
            for (int i = 0; i < 4; i++) {
                int nx = playerX + dx[i];
                int ny = playerY + dy[i];

                if (isValidCell(nx, ny)) {
                    AgentWorldCell neighborCell = knowledgeBase[nx][ny];
                    if (!neighborCell.visited && neighborCell.isSafeFromPit && neighborCell.isSafeFromWumpus) {
                        safeUnvisitedNeighbors.add(new Vector2(nx, ny));
                    }
                }
            }

            if (!safeUnvisitedNeighbors.isEmpty()) {
                Vector2 targetCell = null;
                // Prioriza ir para frente se for seguro e não visitado
                int forwardX = playerX, forwardY = playerY;
                switch(playerDirection) {
                    case NORTH: forwardY++; break;
                    case EAST: forwardX++; break;
                    case SOUTH: forwardY--; break;
                    case WEST: forwardX--; break;
                }
                Vector2 forwardCell = new Vector2(forwardX, forwardY);
                if (isValidCell(forwardX, forwardY) && safeUnvisitedNeighbors.contains(forwardCell)) {
                    targetCell = forwardCell;
                } else {
                    // Caso contrário, escolhe qualquer vizinho seguro e não visitado
                    targetCell = safeUnvisitedNeighbors.get(0);
                }

                if (targetCell != null) {
                    if (isFacing((int)targetCell.x, (int)targetCell.y)) {
                        WumpusGameScreen.this.moveForward(currentGameState);
                        WumpusGameScreen.this.appendToLog("Agente (Modelo): Movi para frente para (" + (int)targetCell.x + "," + (int)targetCell.y + ").");
                        return Action.MOVE_FORWARD;
                    } else {
                        return turnTowards((int)targetCell.x, (int)targetCell.y); // Vira na direção do alvo
                    }
                }
            }

            // E) Backtracking / Explorar Células Potencialmente Desconhecidas (se não houver caminho totalmente seguro)
            // Para um agente reativo com modelo simples, isso significa buscar qualquer célula não visitada
            // que não seja *conhecidamente* perigosa (poço ou wumpus inferido com certeza).
            List<Vector2> unvisitedUnknownNeighbors = new ArrayList<>();
            for (int i = 0; i < 4; i++) {
                int nx = playerX + dx[i];
                int ny = playerY + dy[i];
                if (isValidCell(nx, ny)) {
                    AgentWorldCell neighborCell = knowledgeBase[nx][ny];
                    // Se não visitado E não é conhecido como poço E não é conhecido como wumpus, consideramos.
                    // (isKnownPit/isKnownWumpus seriam setados por inferências mais complexas ou percepções diretas de perigo).
                    if (!neighborCell.visited && !neighborCell.isKnownPit && !neighborCell.isKnownWumpus) {
                        unvisitedUnknownNeighbors.add(new Vector2(nx, ny));
                    }
                }
            }

            if (!unvisitedUnknownNeighbors.isEmpty()) {
                Vector2 targetCell = unvisitedUnknownNeighbors.get(0); // Pega o primeiro como fallback
                if (isFacing((int)targetCell.x, (int)targetCell.y)) {
                    WumpusGameScreen.this.moveForward(currentGameState);
                    WumpusGameScreen.this.appendToLog("Agente (Modelo): Movi para uma sala desconhecida (" + (int)targetCell.x + "," + (int)targetCell.y + ").");
                    return Action.MOVE_FORWARD;
                } else {
                    return turnTowards((int)targetCell.x, (int)targetCell.y);
                }
            }

            // F) Se completamente preso, apenas virar
            WumpusGameScreen.this.appendToLog("Agente (Modelo): Estou preso ou sem opções seguras. Virando para tentar outra coisa.");
            WumpusGameScreen.this.turnLeft(currentGameState);
            return Action.TURN_LEFT;
        }

        /**
         * Verifica se o agente está virado para a célula alvo.
         */
        private boolean isFacing(int targetX, int targetY) {
            if (playerDirection == Direction.NORTH && targetY > playerY && targetX == playerX) return true;
            if (playerDirection == Direction.EAST && targetX > playerX && targetY == playerY) return true;
            if (playerDirection == Direction.SOUTH && targetY < playerY && targetX == playerX) return true;
            if (playerDirection == Direction.WEST && targetX < playerX && targetY == playerY) return true;
            return false;
        }

        /**
         * Vira o agente em direção à célula alvo.
         * @return A ação tomada (TURN_LEFT ou TURN_RIGHT).
         */
        private Action turnTowards(int targetX, int targetY) {
            // Lógica para determinar a melhor direção para virar
            // Isso pode ser aprimorado com mais inteligência (ex: virar na direção mais curta)
            // Por simplicidade, tenta virar para uma direção que o aproxime do alvo.
            // Aqui estamos fazendo uma virada simples (esquerda ou direita).

            // Calcula a diferença de coordenadas
            int deltaX = targetX - playerX;
            int deltaY = targetY - playerY;

            if (deltaX > 0) { // Alvo está a Leste
                if (playerDirection == Direction.NORTH) { WumpusGameScreen.this.turnRight(currentGameState); return Action.TURN_RIGHT; }
                if (playerDirection == Direction.SOUTH) { WumpusGameScreen.this.turnLeft(currentGameState); return Action.TURN_LEFT; }
                // Se já estiver no Leste, isso não deve ser chamado, mas como fallback
                // se estiver no Oeste, precisa virar duas vezes. Priorizamos uma virada única.
                WumpusGameScreen.this.turnLeft(currentGameState); // Ou turnRight, dependendo da rotação
                return Action.TURN_LEFT;
            } else if (deltaX < 0) { // Alvo está a Oeste
                if (playerDirection == Direction.NORTH) { WumpusGameScreen.this.turnLeft(currentGameState); return Action.TURN_LEFT; }
                if (playerDirection == Direction.SOUTH) { WumpusGameScreen.this.turnRight(currentGameState); return Action.TURN_RIGHT; }
                WumpusGameScreen.this.turnRight(currentGameState); // Ou turnLeft
                return Action.TURN_RIGHT;
            } else if (deltaY > 0) { // Alvo está ao Norte
                if (playerDirection == Direction.EAST) { WumpusGameScreen.this.turnLeft(currentGameState); return Action.TURN_LEFT; }
                if (playerDirection == Direction.WEST) { WumpusGameScreen.this.turnRight(currentGameState); return Action.TURN_RIGHT; }
                WumpusGameScreen.this.turnRight(currentGameState); // Ou turnLeft
                return Action.TURN_RIGHT;
            } else if (deltaY < 0) { // Alvo está ao Sul
                if (playerDirection == Direction.EAST) { WumpusGameScreen.this.turnRight(currentGameState); return Action.TURN_RIGHT; }
                if (playerDirection == Direction.WEST) { WumpusGameScreen.this.turnLeft(currentGameState); return Action.TURN_LEFT; }
                WumpusGameScreen.this.turnLeft(currentGameState); // Ou turnRight
                return Action.TURN_LEFT;
            }

            // Caso de fallback ou alvo na mesma célula
            WumpusGameScreen.this.turnLeft(currentGameState);
            return Action.TURN_LEFT;
        }
    }

    /**
     * Representa o estado completo do jogo em um dado momento.
     * Usado para simulações pelo Algoritmo Genético.
     * Esta é uma classe aninhada estática para ser independente de uma instância de WumpusWorldGame,
     * permitindo que o AG a copie livremente.
     */
    public static class WumpusWorldState {
        public int playerX, playerY;
        public Direction playerDirection;
        public boolean hasGold;
        public int arrowsLeft;
        public boolean wumpusAlive;
        public int wumpusX, wumpusY;
        public int goldX, goldY;
        public Array<Vector2> pitPositions = new Array<>();
        public Array<Vector2> batPositions = new Array<>();
        public HashSet<Vector2> visitedRooms;
        public int score;
        public GameState gameState;
        public char[][] worldGrid; // Representa o layout do mundo (Wumpus, Poços, Ouro)

        /**
         * Cria uma cópia profunda do estado atual. Essencial para simulações.
         */
        public WumpusWorldState copy() {
            WumpusWorldState newState = new WumpusWorldState();
            newState.playerX = this.playerX;
            newState.playerY = this.playerY;
            newState.playerDirection = this.playerDirection;
            newState.hasGold = this.hasGold;
            newState.arrowsLeft = this.arrowsLeft;
            newState.wumpusAlive = this.wumpusAlive;
            newState.wumpusX = this.wumpusX;
            newState.wumpusY = this.wumpusY;
            newState.goldX = this.goldX;
            newState.goldY = this.goldY;
            newState.score = this.score;
            newState.gameState = this.gameState;

            // Cópia profunda de Array<Vector2> e HashSet<Vector2>
            newState.pitPositions = new Array<>(this.pitPositions);
            newState.batPositions = new Array<>(this.batPositions);
            newState.visitedRooms = new HashSet<>(this.visitedRooms);

            // Cópia profunda do grid do mundo
            newState.worldGrid = new char[WORLD_SIZE][WORLD_SIZE];
            for (int i = 0; i < WORLD_SIZE; i++) {
                System.arraycopy(this.worldGrid[i], 0, newState.worldGrid[i], 0, WORLD_SIZE);
            }

            return newState;
        }
    }

    /**
     * Agente que utiliza um Algoritmo Genético para encontrar uma sequência de ações.
     * Esta é uma classe interna não estática, permitindo acesso direto aos métodos
     * de ação e percepção do jogo WumpusWorldGame para simulação.
     */
    public class GeneticAgent {
        // Constantes do Algoritmo Genético
        private static final int POPULATION_SIZE = 10;
        private static final int MAX_GENERATIONS = 20;
        private static final float MUTATION_RATE = 0.03f; // 3% de chance de mutação por ação
        private static final float CROSSOVER_RATE = 0.7f; // 70% de chance de cruzamento
        // Comprimento do cromossomo: O suficiente para um caminho considerável.
        // WORLD_SIZE * WORLD_SIZE * 2 ou 3 para cobrir bastante espaço e viradas.
        private static final int CHROMOSOME_LENGTH = WORLD_SIZE * WORLD_SIZE * 3;

        // Ações possíveis para o cromossomo
        public enum Action {
            MOVE_FORWARD, TURN_LEFT, TURN_RIGHT, SHOOT, GRAB, NO_OP;

            // Retorna uma ação aleatória para a inicialização e mutação
            public static Action getRandomAction() {
                Action[] actions = Action.values();
                // Excluir NO_OP da seleção aleatória para ações reais, se desejado.
                // Ou incluir para permitir que o AG aprenda "não fazer nada" em certas situações.
                // Para este AG, incluir NO_OP para ter mais flexibilidade no tamanho real do caminho.
                return actions[ThreadLocalRandom.current().nextInt(actions.length)];
            }
        }

        // Representa um indivíduo na população do Algoritmo Genético
        private class Chromosome implements Comparable<Chromosome> {
            Action[] actions; // Sequência de ações
            float fitness;     // Aptidão do cromossomo

            public Chromosome() {
                actions = new Action[CHROMOSOME_LENGTH];
                for (int i = 0; i < CHROMOSOME_LENGTH; i++) {
                    actions[i] = Action.getRandomAction(); // Inicializa com ações aleatórias
                }
                fitness = 0;
            }

            @Override
            public int compareTo(Chromosome other) {
                // Para ordenar do mais apto (maior fitness) para o menos apto
                return Float.compare(other.fitness, this.fitness);
            }
        }

        private List<Action> plannedPath;
        private int currentActionIndex;
        private Random random;

        public GeneticAgent() {
            random = ThreadLocalRandom.current();
            initializeKnowledgeBase();
        }

        /**
         * Inicializa a base de conhecimento do agente.
         * Para este GA, a "base de conhecimento" principal é a capacidade de simular o ambiente,
         * mas podemos ter uma memória das salas visitadas durante a simulação para guiar a fitness.
         * No entanto, como o GA busca um caminho completo, a memória em si é mais para a avaliação.
         */
        public void initializeKnowledgeBase() {
            // Reinicia qualquer estado interno que o agente possa ter acumulado.
            plannedPath = null;
            currentActionIndex = 0;
            // Para GA, não há uma "base de conhecimento" explícita persistente entre chamadas,
            // pois o algoritmo gera e avalia soluções do zero em cada execução.
        }

        /**
         * Avalia a aptidão de um cromossomo simulando suas ações em uma cópia do estado do jogo.
         * @param initialState O estado inicial do jogo para a simulação.
         * @param chromosome O cromossomo (sequência de ações) a ser avaliado.
         * @return A aptidão calculada para o cromossomo.
         */
        private float evaluateFitness(WumpusWorldState initialState, Chromosome chromosome) {
            WumpusWorldState simState = initialState.copy(); // Trabalha em uma cópia para não alterar o jogo real
            float fitness = 0;
            boolean goldReached = false;
            boolean returnedToStartWithGold = false;
            int initialArrows = simState.arrowsLeft;
            int movesTaken = 0;

            for (Action action : chromosome.actions) {
                if (simState.gameState != GameState.PLAYING) {
                    // Se o jogo terminou na simulação (morte ou vitória), para de simular este cromossomo.
                    break;
                }

                // Aplica a ação ao estado simulado
                switch (action) {
                    case MOVE_FORWARD:
                        int prevPlayerX = simState.playerX;
                        int prevPlayerY = simState.playerY;
                        moveForward(simState); // Este método agora opera em 'state'
                        if (simState.playerX == prevPlayerX && simState.playerY == prevPlayerY) {
                            fitness -= 50; // Grande penalidade por tentar mover para fora do mundo/preso
                        }
                        movesTaken++;
                        fitness -= 1; // Pequena penalidade por cada passo
                        break;
                    case TURN_LEFT:
                        turnLeft(simState);
                        fitness -= 0.1f; // Pequena penalidade para virar
                        break;
                    case TURN_RIGHT:
                        turnRight(simState);
                        fitness -= 0.1f; // Pequena penalidade para virar
                        break;
                    case SHOOT:
                        int arrowsBeforeShoot = simState.arrowsLeft;
                        shootArrow(simState);
                        if (arrowsBeforeShoot > simState.arrowsLeft) { // Se uma flecha foi usada
                            fitness -= 10; // Penalidade por usar flecha
                            if (!simState.wumpusAlive) {
                                fitness += 200; // Recompensa por matar Wumpus
                                appendToLog("Agente (Simulação): Wumpus morto na simulação!");
                            }
                        } else {
                            fitness -= 5; // Penalidade por atirar sem sucesso
                        }
                        break;
                    case GRAB:
                        boolean hadGoldBeforeGrab = simState.hasGold;
                        searchForGold(simState);
                        if (simState.hasGold && !hadGoldBeforeGrab) {
                            fitness += 500; // Grande recompensa por pegar o ouro
                            goldReached = true;
                            appendToLog("Agente (Simulação): Ouro pego na simulação!");
                        } else if (hadGoldBeforeGrab) {
                            fitness -= 50; // Penalidade por tentar pegar ouro que já tem
                        } else {
                            fitness -= 20; // Penalidade por tentar pegar ouro onde não há
                        }
                        break;
                    case NO_OP:
                        // Sem penalidade ou benefício significativo, útil para padding.
                        fitness -= 0.5f; // Pequena penalidade para desincentivar NO_OPs desnecessárias.
                        break;
                }

                // Penalidades por estado de GAME_OVER
                if (simState.gameState == GameState.GAME_OVER) {
                    fitness -= 1000; // Grande penalidade por morrer
                    break; // Termina a simulação deste cromossomo
                }

                // Recompensa por vitória
                if (simState.gameState == GameState.GAME_WON) {
                    fitness += 10000; // Recompensa massiva por vencer o jogo
                    returnedToStartWithGold = true;
                    break; // Termina a simulação deste cromossomo
                }
            }

            // Penalidades/recompensas finais baseadas no resultado da simulação
            if (goldReached && !returnedToStartWithGold) {
                // Recompensa adicional se pegou o ouro mas não voltou à entrada ainda
                fitness += 100;
            }

            // Se o Wumpus ainda estiver vivo e o agente não atirou ou errou, e não venceu o jogo, penalizar.
            if (simState.wumpusAlive && simState.gameState != GameState.GAME_WON && initialArrows == NUM_ARROWS && currentGameState.wumpusAlive) {
                // Penalidade por não matar o Wumpus se era uma condição de vitória esperada
                // (isso é para o caso de o objetivo ser opcional, mas o GA pode aprender a fazê-lo)
                // Se o wumpus não foi morto e o jogo acabou sem vitória, grande penalidade
                if (simState.gameState == GameState.PLAYING) { // Se o jogo não terminou, ainda tem chance de matá-lo
                    fitness -= 50;
                }
            }

            // Se não pegou o ouro e não venceu
            if (!simState.hasGold && simState.gameState != GameState.GAME_WON) {
                fitness -= 200; // Grande penalidade por não cumprir o objetivo principal
            }

            return fitness;
        }

        /**
         * Executa o Algoritmo Genético para encontrar o melhor caminho.
         * @return A sequência de ações do cromossomo mais apto.
         */
        private List<Action> runGeneticAlgorithm() {
            List<Chromosome> population = new ArrayList<>();
            for (int i = 0; i < POPULATION_SIZE; i++) {
                population.add(new Chromosome());
            }

            Chromosome bestOverallChromosome = null;

            for (int generation = 0; generation < MAX_GENERATIONS; generation++) {
                // 1. Avaliação
                for (Chromosome chromosome : population) {
                    chromosome.fitness = evaluateFitness(currentGameState, chromosome);
                }

                // 2. Classificação
                Collections.sort(population); // Ordena do mais apto para o menos apto

                // Elite: Mantém o melhor cromossomo da geração atual
                if (bestOverallChromosome == null || population.get(0).fitness > bestOverallChromosome.fitness) {
                    bestOverallChromosome = population.get(0);
                    appendToLog("Agente (GA): Nova melhor aptidão: " + String.format("%.2f", bestOverallChromosome.fitness) + " na Geração " + generation);
                }

                List<Chromosome> newPopulation = new ArrayList<>();
                // Elitismo: Mantém os 2 melhores da população atual para a próxima geração
                newPopulation.add(population.get(0));
                newPopulation.add(population.get(1));

                // Preenche o restante da nova população através de seleção, cruzamento e mutação
                while (newPopulation.size() < POPULATION_SIZE) {
                    Chromosome parent1 = selectParent(population);
                    Chromosome parent2 = selectParent(population);

                    Chromosome child1, child2;

                    // Cruzamento (Crossover)
                    if (random.nextFloat() < CROSSOVER_RATE) {
                        Chromosome[] children = crossover(parent1, parent2);
                        child1 = children[0];
                        child2 = children[1];
                    } else {
                        // Sem cruzamento, filhos são cópias dos pais
                        child1 = new Chromosome();
                        System.arraycopy(parent1.actions, 0, child1.actions, 0, CHROMOSOME_LENGTH);
                        child2 = new Chromosome();
                        System.arraycopy(parent2.actions, 0, child2.actions, 0, CHROMOSOME_LENGTH);
                    }

                    // Mutação
                    mutate(child1);
                    mutate(child2);

                    newPopulation.add(child1);
                    if (newPopulation.size() < POPULATION_SIZE) { // Garante que não adicione mais do que o POPULATION_SIZE
                        newPopulation.add(child2);
                    }
                }
                population = newPopulation;
            }

            // Após todas as gerações, avalia a aptidão final
            for (Chromosome chromosome : population) {
                chromosome.fitness = evaluateFitness(currentGameState, chromosome);
            }
            Collections.sort(population); // Ordena novamente para garantir o melhor
            bestOverallChromosome = population.get(0); // O melhor da última geração ou o melhor geral

            appendToLog("Agente (GA): Algoritmo Genético concluído. Melhor aptidão final: " + String.format("%.2f", bestOverallChromosome.fitness));

            // Retorna o caminho do melhor cromossomo, removendo NO_OPs no final
            List<Action> finalPath = new ArrayList<>();
            for (Action action : bestOverallChromosome.actions) {
                if (action != Action.NO_OP) {
                    finalPath.add(action);
                }
            }
            return finalPath;
        }

        /**
         * Seleciona um pai usando seleção por torneio.
         * @param population A população atual de cromossomos.
         * @return O cromossomo selecionado.
         */
        private Chromosome selectParent(List<Chromosome> population) {
            // Tamanho do torneio: 3 a 5 geralmente funciona bem
            int tournamentSize = 5;
            Chromosome best = null;
            for (int i = 0; i < tournamentSize; i++) {
                int randomIndex = random.nextInt(population.size());
                Chromosome candidate = population.get(randomIndex);
                if (best == null || candidate.fitness > best.fitness) {
                    best = candidate;
                }
            }
            return best;
        }

        /**
         * Realiza o cruzamento (crossover) entre dois pais para criar dois filhos.
         * @param parent1 O primeiro cromossomo pai.
         * @param parent2 O segundo cromossomo pai.
         * @return Um array de dois cromossomos filhos.
         */
        private Chromosome[] crossover(Chromosome parent1, Chromosome parent2) {
            Chromosome child1 = new Chromosome();
            Chromosome child2 = new Chromosome();

            int crossoverPoint = random.nextInt(CHROMOSOME_LENGTH); // Ponto de corte único

            for (int i = 0; i < CHROMOSOME_LENGTH; i++) {
                if (i < crossoverPoint) {
                    child1.actions[i] = parent1.actions[i];
                    child2.actions[i] = parent2.actions[i];
                } else {
                    child1.actions[i] = parent2.actions[i];
                    child2.actions[i] = parent1.actions[i];
                }
            }
            return new Chromosome[]{child1, child2};
        }

        /**
         * Aplica mutação a um cromossomo.
         * @param chromosome O cromossomo a ser mutado.
         */
        private void mutate(Chromosome chromosome) {
            for (int i = 0; i < CHROMOSOME_LENGTH; i++) {
                if (random.nextFloat() < MUTATION_RATE) {
                    chromosome.actions[i] = Action.getRandomAction(); // Substitui por uma ação aleatória
                }
            }
        }

        /**
         * Decide e executa a próxima ação do agente.
         * Se um caminho não foi planejado ou foi concluído, ele executa o Algoritmo Genético.
         */
        public void decideAndPerformAction() {
            if (plannedPath == null || currentActionIndex >= plannedPath.size()) {
                appendToLog("Agente (GA): Iniciando planejamento de nova rota...");
                plannedPath = runGeneticAlgorithm();
                currentActionIndex = 0;
                if (plannedPath.isEmpty()) {
                    appendToLog("Agente (GA): Nenhuma rota viável encontrada ou rota já concluída.");
                    // Se não há mais ações a serem executadas, pode desativar o agente
                    isAgentPlaying = false; // Desativa o agente se o plano estiver vazio
                    return;
                }
            }

            // Executa a próxima ação do caminho planejado no estado real do jogo
            Action nextAction = plannedPath.get(currentActionIndex);
            currentActionIndex++;

            switch (nextAction) {
                case MOVE_FORWARD:
                    moveForward(currentGameState);
                    break;
                case TURN_LEFT:
                    turnLeft(currentGameState);
                    break;
                case TURN_RIGHT:
                    turnRight(currentGameState);
                    break;
                case SHOOT:
                    shootArrow(currentGameState);
                    break;
                case GRAB:
                    searchForGold(currentGameState);
                    break;
                case NO_OP:
                    appendToLog("Agente (GA): No-op (pausa).");
                    // Pode querer adicionar uma pequena penalidade no fitness se muitos NO_OPs
                    // são usados em um caminho "otimizado", mas já estamos fazendo isso no evaluateFitness
                    break;
            }
        }
    }
    /**
     * Agent that uses a Simple Neural Network to make decisions.
     * This is a non-static inner class, allowing direct access to the
     * WumpusWorldGame action and perception methods.
     */
    public class NeuralAgent {
        // Neural Network Definition
        private static final int INPUT_NODES = 12; // playerX, playerY, Direction (4), Stench, Breeze, Glitter, HasGold, ArrowsLeft, WumpusAlive
        private static final int HIDDEN_NODES = 16; // Can be adjusted
        private static final int OUTPUT_NODES = 5; // MOVE_FORWARD, TURN_LEFT, TURN_RIGHT, SHOOT, GRAB (NO_OP removed)

        private static final float LEARNING_RATE = 0.01f; // Learning rate for weight adjustment
        private static final float EXPLORATION_RATE = 0.1f; // Exploration rate (epsilon-greedy)

        // Weights and biases of the neural network
        private float[][] weightsInputHidden; // Weights from input to hidden layer
        private float[] biasesHidden;         // Biases of the hidden layer
        private float[][] weightsHiddenOutput; // Weights from hidden to output layer
        private float[] biasesOutput;         // Biases of the output layer

        private Random random;

        public NeuralAgent() {
            random = ThreadLocalRandom.current();
            initializeNetwork();
        }

        /**
         * Initializes the weights and biases of the neural network with small random values.
         */
        public void initializeNetwork() {
            weightsInputHidden = new float[INPUT_NODES][HIDDEN_NODES];
            biasesHidden = new float[HIDDEN_NODES];
            weightsHiddenOutput = new float[HIDDEN_NODES][OUTPUT_NODES];
            biasesOutput = new float[OUTPUT_NODES];

            // Initialize weights and biases with small random values
            for (int i = 0; i < INPUT_NODES; i++) {
                for (int j = 0; j < HIDDEN_NODES; j++) {
                    weightsInputHidden[i][j] = (random.nextFloat() * 2 - 1) * 0.1f; // Values between -0.1 and 0.1
                }
            }
            for (int i = 0; i < HIDDEN_NODES; i++) {
                biasesHidden[i] = (random.nextFloat() * 2 - 1) * 0.1f;
            }
            for (int i = 0; i < HIDDEN_NODES; i++) {
                for (int j = 0; j < OUTPUT_NODES; j++) {
                    weightsHiddenOutput[i][j] = (random.nextFloat() * 2 - 1) * 0.1f;
                }
            }
            for (int i = 0; i < OUTPUT_NODES; i++) {
                biasesOutput[i] = (random.nextFloat() * 2 - 1) * 0.1f;
            }
            appendToLog("Agent (NN): Neural Network initialized.");
        }

        /**
         * Sigmoid activation function.
         */
        private float sigmoid(float x) {
            return 1.0f / (1.0f + (float) Math.exp(-x));
        }

        /**
         * Performs the forward pass through the neural network.
         * @param inputs The input values.
         * @return The output values of the network.
         */
        private float[] feedForward(float[] inputs) {
            // Input layer to hidden layer
            float[] hiddenOutputs = new float[HIDDEN_NODES];
            for (int j = 0; j < HIDDEN_NODES; j++) {
                float sum = 0;
                for (int i = 0; i < INPUT_NODES; i++) {
                    sum += inputs[i] * weightsInputHidden[i][j];
                }
                hiddenOutputs[j] = sigmoid(sum + biasesHidden[j]);
            }

            // Hidden layer to output layer
            float[] finalOutputs = new float[OUTPUT_NODES];
            for (int j = 0; j < OUTPUT_NODES; j++) {
                float sum = 0;
                for (int i = 0; i < HIDDEN_NODES; i++) {
                    sum += hiddenOutputs[i] * weightsHiddenOutput[i][j];
                }
                finalOutputs[j] = sigmoid(sum + biasesOutput[j]);
            }
            return finalOutputs;
        }

        /**
         * Converts the current game state into an array of inputs for the neural network.
         */
        private float[] getStateInputs(WumpusWorldState state) {
            float[] inputs = new float[INPUT_NODES];
            int index = 0;

            // Player position (normalized)
            inputs[index++] = (float) state.playerX / (WORLD_SIZE - 1);
            inputs[index++] = (float) state.playerY / (WORLD_SIZE - 1);

            // Player direction (one-hot encoded)
            inputs[index++] = (state.playerDirection == Direction.NORTH) ? 1.0f : 0.0f;
            inputs[index++] = (state.playerDirection == Direction.EAST) ? 1.0f : 0.0f;
            inputs[index++] = (state.playerDirection == Direction.SOUTH) ? 1.0f : 0.0f;
            inputs[index++] = (state.playerDirection == Direction.WEST) ? 1.0f : 0.0f;

            // Perceptions
            inputs[index++] = WumpusGameScreen.this.isStench(state.playerX, state.playerY, state) ? 1.0f : 0.0f;
            inputs[index++] = WumpusGameScreen.this.isBreeze(state.playerX, state.playerY, state) ? 1.0f : 0.0f;
            inputs[index++] = WumpusGameScreen.this.isGlitter(state) ? 1.0f : 0.0f;

            // Other game states
            inputs[index++] = state.hasGold ? 1.0f : 0.0f;
            inputs[index++] = (float) state.arrowsLeft / NUM_ARROWS; // Normalized
            inputs[index++] = state.wumpusAlive ? 1.0f : 0.0f;

            return inputs;
        }

        /**
         * Maps the neural network output index to a game action.
         */
        private Action getActionFromOutput(int outputIndex) {
            switch (outputIndex) {
                case 0: return Action.MOVE_FORWARD;
                case 1: return Action.TURN_LEFT;
                case 2: return Action.TURN_RIGHT;
                case 3: return Action.SHOOT;
                case 4: return Action.GRAB;
                default: return Action.MOVE_FORWARD; // Fallback, should not happen with correct OUTPUT_NODES
            }
        }

        /**
         * Calculates an immediate reward based on the state change.
         * @param oldState State before the action.
         * @param newState State after the action.
         * @return The reward value.
         */
        private float calculateImmediateReward(WumpusWorldState oldState, WumpusWorldState newState) {
            float reward = 0;

            // Rewards and Penalties
            if (newState.gameState == GameState.GAME_WON) {
                reward += 10000; // Large reward for winning
            } else if (newState.gameState == GameState.GAME_OVER) {
                reward -= 1000; // Large penalty for dying
            }

            if (newState.hasGold && !oldState.hasGold) {
                reward += 500; // Reward for picking up gold
            }

            if (!newState.wumpusAlive && oldState.wumpusAlive) {
                reward += 200; // Reward for killing Wumpus
            }

            // Penalties for actions
            if (newState.playerX != oldState.playerX || newState.playerY != oldState.playerY) {
                reward -= 1; // Small penalty for movement
            }
            if (newState.arrowsLeft < oldState.arrowsLeft) {
                reward -= 10; // Penalty for using an arrow
            }

            // Penalty for being in a dangerous room (without dying yet)
            if (WumpusGameScreen.this.isBreeze(newState.playerX, newState.playerY, newState) && newState.gameState == GameState.PLAYING) {
                reward -= 5;
            }
            if (WumpusGameScreen.this.isStench(newState.playerX, newState.playerY, newState) && newState.gameState == GameState.PLAYING) {
                reward -= 5;
            }

            return reward;
        }

        /**
         * Adjusts the neural network weights based on the received reward.
         * This is a simplified form of reinforcement learning.
         * @param inputs The inputs that led to the action.
         * @param chosenActionIndex The index of the chosen action.
         * @param reward The received reward.
         */
        private void adjustWeights(float[] inputs, int chosenActionIndex, float reward) {
            // Recalculate network outputs to get intermediate values
            float[] hiddenOutputs = new float[HIDDEN_NODES];
            for (int j = 0; j < HIDDEN_NODES; j++) {
                float sum = 0;
                for (int i = 0; i < INPUT_NODES; i++) {
                    sum += inputs[i] * weightsInputHidden[i][j];
                }
                hiddenOutputs[j] = sigmoid(sum + biasesHidden[j]);
            }

            float[] finalOutputs = new float[OUTPUT_NODES];
            for (int j = 0; j < OUTPUT_NODES; j++) {
                float sum = 0;
                for (int i = 0; i < HIDDEN_NODES; i++) {
                    sum += hiddenOutputs[i] * weightsHiddenOutput[i][j];
                }
                finalOutputs[j] = sigmoid(sum + biasesOutput[j]);
            }

            // Weight and bias adjustment (very simplified, not full backpropagation)
            // The idea is that if the reward is positive, we increase the weights that led to that output.
            // If negative, we decrease them.
            float targetOutput = (reward > 0) ? 1.0f : 0.0f; // Simplified target: 1 for good, 0 for bad
            float error = targetOutput - finalOutputs[chosenActionIndex]; // Error for the chosen action

            // Adjust weights from hidden to output layer
            for (int i = 0; i < HIDDEN_NODES; i++) {
                weightsHiddenOutput[i][chosenActionIndex] += LEARNING_RATE * error * hiddenOutputs[i];
            }
            biasesOutput[chosenActionIndex] += LEARNING_RATE * error;

            // Adjust weights from input to hidden layer (more complex without backprop, simplified)
            // We propagate the "error" back to the hidden layer.
            for (int i = 0; i < INPUT_NODES; i++) {
                for (int j = 0; j < HIDDEN_NODES; j++) {
                    // This is a heuristically simplified adjustment, not mathematically derived from the gradient.
                    // For real backpropagation, we would need the derivative of the activation function.
                    weightsInputHidden[i][j] += LEARNING_RATE * error * inputs[i] * (hiddenOutputs[j] * (1 - hiddenOutputs[j])); // Sigmoid derivative
                }
            }
            for (int i = 0; i < HIDDEN_NODES; i++) {
                biasesHidden[i] += LEARNING_RATE * error * (hiddenOutputs[i] * (1 - hiddenOutputs[i]));
            }
        }

        /**
         * Decides and executes the agent's next action.
         */
        public void decideAndPerformAction() {
            // Stores the state before the action to calculate reward and for gold repositioning
            WumpusWorldState oldState = currentGameState.copy();

            // If the game ended, check if the agent should be reset
            if (currentGameState.gameState != GameState.PLAYING) {
                if (isAgentPlaying) { // Check if the agent is still in control
                    // If the agent died with gold, mark to reposition gold
                    if (currentGameState.gameState == GameState.GAME_OVER && oldState.hasGold) {
                        WumpusGameScreen.this.shouldRepositionGoldAfterDeath = true;
                        WumpusGameScreen.this.repositionGoldTargetX = oldState.playerX;
                        WumpusGameScreen.this.repositionGoldTargetY = oldState.playerY;
                        // Gold is "dropped" in the current state, so resetWorldForAgent can reposition it
                        currentGameState.hasGold = false;
                    }
                    WumpusGameScreen.this.resetWorldForAgent();
                } else {
                    // If the agent is no longer playing (manually deactivated), do nothing.
                    return;
                }
            }

            // 1. Get perceptions from the current game state
            float[] inputs = getStateInputs(currentGameState);

            // 2. Pass perceptions through the neural network to get action outputs
            float[] outputs = feedForward(inputs);

            // 3. Choose an action (Epsilon-greedy for exploration)
            int chosenActionIndex;
            if (random.nextFloat() < EXPLORATION_RATE) {
                // Exploration: Choose a random action
                chosenActionIndex = random.nextInt(OUTPUT_NODES);
                appendToLog("Agent (NN): Exploring with random action: " + getActionFromOutput(chosenActionIndex));
            } else {
                // Exploitation: Choose the action with the highest output
                float maxOutput = -1.0f;
                chosenActionIndex = -1;
                for (int i = 0; i < OUTPUT_NODES; i++) {
                    if (outputs[i] > maxOutput) {
                        maxOutput = outputs[i];
                        chosenActionIndex = i;
                    }
                }
                appendToLog("Agent (NN): Chose action based on network: " + getActionFromOutput(chosenActionIndex) + " (Value: " + String.format("%.2f", maxOutput) + ")");
            }

            // 4. Execute the chosen action in the real game environment
            Action chosenAction = getActionFromOutput(chosenActionIndex);
            switch (chosenAction) {
                case MOVE_FORWARD:
                    moveForward(currentGameState);
                    break;
                case TURN_LEFT:
                    turnLeft(currentGameState);
                    break;
                case TURN_RIGHT:
                    turnRight(currentGameState);
                    break;
                case SHOOT:
                    shootArrow(currentGameState);
                    break;
                case GRAB:
                    searchForGold(currentGameState);
                    break;
                // NO_OP removed
            }

            // 5. Calculate reward and adjust neural network weights
            float reward = calculateImmediateReward(oldState, currentGameState);
            adjustWeights(inputs, chosenActionIndex, reward);
            appendToLog("Agent (NN): Reward received: " + String.format("%.2f", reward));

            // If the game ended AFTER this action, the next call to decideAndPerformAction()
            // will restart the world via `resetWorldForAgent()`.
        }

        // Mapping of index to Action enum (for readability)
        private enum Action {
            MOVE_FORWARD, TURN_LEFT, TURN_RIGHT, SHOOT, GRAB; // NO_OP removed
        }
    }
}
