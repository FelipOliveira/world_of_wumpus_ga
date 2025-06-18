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
    private float infoBarWidth;
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
        //hudRootTable.debug();

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
        buttonsContainerTable.align(Align.bottom);
        //buttonsContainerTable.defaults().pad(2);

        Table leftButtons = new Table(skin);
        leftButtons.defaults().width(40).height(35);
        leftButtons.align(Align.bottomLeft);
        moveButton = new TextButton("MOVE", skin);
        moveButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
               if (gameState == GameState.PLAYING) moveForward();
            }
        });
        turnLeftButton = new TextButton("T_L", skin);
        turnLeftButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (gameState == GameState.PLAYING) turnLeft();
            }
        });
        turnRightButton = new TextButton("T_R", skin);
        turnRightButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (gameState == GameState.PLAYING) turnRight();
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
                if (gameState == GameState.PLAYING) searchForGold();
            }
        });
        specialButton = new TextButton("SHT", skin);
        specialButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (gameState == GameState.PLAYING) shootArrow();
            }
        });
        mapButton = new TextButton("MAP", skin);
        mapButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (gameState == GameState.PLAYING) {
                    boolean mapVisible = !mapScreen.isVisible();
                    mapScreen.setVisible(mapVisible);
                    setGameButtonsEnabled(!mapVisible);
                }
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
                setGameButtonsEnabled(true);
                mapButton.setDisabled(false);
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
        GdxUtils.clearScreen();

        debugCameraController.inputDebugHandle(Gdx.graphics.getDeltaTime());
        debugCameraController.applyToCamera(gameplayCamera);

        gameplayCamera.update();
        gameplayViewport.apply();

        batch.setProjectionMatrix(gameplayCamera.combined);

        batch.begin();
        drawHexagonalRoom();
        batch.end();

        hudStage.act(Gdx.graphics.getDeltaTime());
        hudStage.draw();

        if (gameState == GameState.GAME_OVER || gameState == GameState.GAME_WON) {
            restartButton.setVisible(true);
            setGameButtonsEnabled(false);
            mapButton.setDisabled(true);
            mapScreen.setVisible(false);
        } else {
            restartButton.setVisible(false);
            if (mapScreen.isVisible()) {
                setGameButtonsEnabled(false);
                mapButton.setDisabled(false);
            } else {
                setGameButtonsEnabled(true);
                mapButton.setDisabled(false);
            }
        }
    }

    private void drawHexagonalRoom() {
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

        switch (playerDirection) {
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
        if (isValidCell(playerX, playerY + 1)) {
//            float northPassageX = renderX + hexRoomDrawWidth * 0.05f; // Mais à esquerda
//            float northPassageY = renderY + hexRoomDrawHeight * 0.65f; // Mais para cima
//            batch.draw(hexPassageNorthTexture, northPassageX, northPassageY, passageWallWidth, passageWallHeight);
            batch.draw(hexPassageNorthTexture, renderX, renderY, hexRoomDrawWidth, hexRoomDrawHeight);
        }
        // Passagem Leste (canto superior direito, junto à parede direita)
        if (isValidCell(playerX + 1, playerY)) {
//            float eastPassageX = renderX + hexRoomDrawWidth * 0.70f; // Mais à direita
//            float eastPassageY = renderY + hexRoomDrawHeight * 0.65f; // Mais para cima
//            batch.draw(hexPassageEastTexture, eastPassageX, eastPassageY, passageWallWidth, passageWallHeight);
            batch.draw(hexPassageEastTexture, renderX, renderY, hexRoomDrawWidth, hexRoomDrawHeight);
        }
        // Passagem Sul (canto inferior direito)
        if (isValidCell(playerX, playerY - 1)) {
//            float southPassageX = renderX + hexRoomDrawWidth * 0.60f; // Mais à direita
//            float southPassageY = renderY + hexRoomDrawHeight * 0.05f; // Mais para baixo
//            batch.draw(hexPassageSouthTexture, southPassageX, southPassageY, passageFloorWidth, passageFloorHeight);
            batch.draw(hexPassageSouthTexture, renderX, renderY, hexRoomDrawWidth, hexRoomDrawHeight);
        }
        // Passagem Oeste (canto inferior esquerdo)
        if (isValidCell(playerX - 1, playerY)) {
//            float westPassageX = renderX + hexRoomDrawWidth * 0.15f; // Mais à esquerda
//            float westPassageY = renderY + hexRoomDrawHeight * 0.05f; // Mais para baixo
//            batch.draw(hexPassageWestTexture, westPassageX, westPassageY, passageFloorWidth, passageFloorHeight);
            batch.draw(hexPassageWestTexture, renderX, renderY, hexRoomDrawWidth, hexRoomDrawHeight);
        }
        if (isStench(playerX, playerY)){
            batch.draw(stenchTexture, renderX, renderY, hexRoomDrawWidth, hexRoomDrawHeight);
        }
        if (isBreeze(playerX, playerY)){
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
        batch.draw(playerTexture,renderX, renderY, hexRoomDrawWidth, hexRoomDrawHeight);
        /*
        // Posição do item (abaixo do jogador)
        float itemDrawX = roomFloorCenterX - itemDrawSize / 2f;
        float itemDrawY = roomFloorCenterY - itemDrawSize / 2f - (playerDrawSize * 0.1f); // Ligeiramente abaixo do jogador

        if (playerX == wumpusX && playerY == wumpusY && wumpusAlive) {
            batch.draw(wumpusTexture, itemDrawX, itemDrawY, itemDrawSize, itemDrawSize);
        }
        */
        if (playerX == goldX && playerY == goldY && !hasGold) {
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
        if (isValidCell(playerX, playerY + 1) && !isPitAt(playerX, playerY + 1) && !isBatAt(playerX, playerY + 1)) {
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
                    //appendToLog(Messages.ARROW_HIT_WUMPUS);
                    message = message.concat(Messages.ARROW_HIT_WUMPUS);
                    wumpusAlive = false;
                    score += 200;
                    world[wumpusX][wumpusY] = ' ';
                    if (hasGold && playerX == 0 && playerY == 0) {
                        gameState = GameState.GAME_WON;
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
            appendToLog(Messages.GAME_OVER_MESSAGE + score);
            //appendToLog(Messages.RESET_MESSAGE);
//        } else if (isBatAt(playerX,playerY)) {
//            teleportPlayer();
//            appendToLog("You got teleported by a bat");
        } else if (playerX == wumpusX && playerY == wumpusY && wumpusAlive) {
            gameState = GameState.GAME_OVER;
            appendToLog(Messages.AGENT_CAUGHT_BY_WUMPUS);
            score -= 1000;
            appendToLog(Messages.GAME_OVER_MESSAGE + score);
            //appendToLog(Messages.RESET_MESSAGE);
        } else if (hasGold && playerX == 0 && playerY == 0) {
            gameState = GameState.GAME_WON;
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
        if (gameState == GameState.PLAYING) {
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
        passageTexture.dispose();
        hexRoomBaseTexture.dispose();
        hexPassageNorthTexture.dispose();
        hexPassageEastTexture.dispose();
        hexPassageSouthTexture.dispose();
        hexPassageWestTexture.dispose();
*/
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
        private final float scrollSpeed;
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
}
