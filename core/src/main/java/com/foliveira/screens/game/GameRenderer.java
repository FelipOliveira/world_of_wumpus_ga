package com.foliveira.screens.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.foliveira.config.GameConfig;
import com.foliveira.config.Messages;
import com.foliveira.entities.GameStatus;
import com.foliveira.utils.GdxUtils;
import com.foliveira.utils.ViewportUtils;
import com.foliveira.utils.debug.DebugCameraController;

public class GameRenderer {
    private static final float PADDING = 25.0f;
    private OrthographicCamera camera;
    private Viewport viewport;
    private ShapeRenderer renderer;
    private OrthographicCamera hudCamera;
    private Viewport hudViewport;
    private BitmapFont font;
    private AssetManager assetManager;
    private final SpriteBatch batch;
    private final GlyphLayout layout = new GlyphLayout();
    private DebugCameraController debugCameraController;
    private final GameController gameController;
    private TextureRegion playerRegion;
    private TextureRegion obstacleRegion;
    private TextureRegion backgroundRegion;
    private Texture texture;

    private Stage stage;
    private Skin skin;
    private Label logLabel;
    private ScrollPane logScrollPane;
    private ScrollingLabel infoBarLabel;

    private Button restartButton;

    public GameRenderer(SpriteBatch batch, AssetManager assetManager, GameController gameController) {
        this.assetManager = assetManager;
        this.gameController = gameController;
        this.batch = batch;
        init();
    }

    public void init() {

        camera = new OrthographicCamera();
        viewport = new FitViewport(GameConfig.VIRTUAL_WIDTH, GameConfig.VIRTUAL_HEIGHT, camera);
        //renderer = new ShapeRenderer();

        viewport.apply();

        camera.position.set(viewport.getWorldWidth()/2, viewport.getWorldHeight()/2, 0);
        camera.update();

        hudCamera = new OrthographicCamera();
        hudViewport = new FitViewport(GameConfig.VIRTUAL_WIDTH, GameConfig.VIRTUAL_HEIGHT, hudCamera);
        //batch = new SpriteBatch();
        //font = assetManager.get(AssetDescriptors.FONT);

        // create debug camera controller
        debugCameraController = new DebugCameraController();
        debugCameraController.setStartPosition((float) GameConfig.VIRTUAL_WIDTH /2, (float) GameConfig.VIRTUAL_HEIGHT /2);

        //TextureAtlas gameAtlas = assetManager.get(AssetDescriptors.GAME_ATLAS);
        // create player and obstacles textures
        //playerRegion = gameAtlas.findRegion(RegionNames.PLAYER);
        //obstacleRegion = gameAtlas.findRegion(RegionNames.OBSTACLE);

        // create background texture
        //backgroundRegion = gameAtlas.findRegion(RegionNames.BACKGROUND);

        /*=======================SCENE2D MODE=======================*/

        /*Texture texture = new Texture(Gdx.files.internal("texture/iso.jpg"));

        Image background = new Image(texture);

        background.setSize(GameConfig.VIRTUAL_WIDTH, GameConfig.VIRTUAL_HEIGHT);
        stage.addActor(background);*/

        font = new BitmapFont(Gdx.files.internal("ui/press_start_2.fnt"));
        font.setColor(Color.WHITE);
        font.getData().setScale(0.5f);

        skin = new Skin(Gdx.files.internal("ui/uiskin.json"));

        stage = new Stage(viewport, batch);
        Gdx.input.setInputProcessor(stage);

        setupUI();

        appendToLog(Messages.WELCOME_LOG);
        //updatePerceptions();
        updateInfoBar(Messages.INITIAL_MESSAGE_INFO);
        gameController.displayPercepts();
        appendToLog(gameController.statusMessage);
    }

    public void render(float delta) {
        debugCameraController.inputDebugHandle(delta);
        debugCameraController.applyToCamera(camera);

        GdxUtils.clearScreen();

        stage.act(delta);
        stage.draw();

        hudViewport.apply();
        //renderUI();

        processKeyboardInput();
    }

    private void processKeyboardInput() {
        if (gameController.status == GameStatus.PLAYING) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) {
                gameController.processAction(GameConfig.ROTATE_LEFT);

                appendToLog(gameController.agent.actionSense);
                appendToLog(gameController.statusMessage);
                updateInfoBar(gameController.infoBarMessage);
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
                gameController.processAction(GameConfig.ROTATE_RIGHT);

                appendToLog(gameController.agent.actionSense);
                appendToLog(gameController.statusMessage);
                updateInfoBar(gameController.infoBarMessage);
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
                gameController.processAction(GameConfig.MOVE_FORWARD);

                appendToLog(gameController.agent.actionSense);
                appendToLog(gameController.statusMessage);
                updateInfoBar(gameController.infoBarMessage);
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.Z)) {
                gameController.processAction(GameConfig.SPECIAL);

                appendToLog(gameController.agent.actionSense);
                appendToLog(gameController.statusMessage);
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.X)) {
                gameController.processAction(GameConfig.SEARCH);

                if (gameController.agent.isHasGold()) {
                    updateInfoBar(Messages.GOT_GOLD_INFO);
                }
                appendToLog(gameController.agent.actionSense);
                appendToLog(gameController.statusMessage);
            }
        } else {
            if (Gdx.input.isKeyJustPressed(Input.Keys.R)){
                gameController.restart();
                logLabel.setText(Messages.WELCOME_LOG);
                updateInfoBar(Messages.INITIAL_MESSAGE_INFO);
            }
        }
    }

    public void resize(int width, int height) {
        viewport.update(width, height, true);
        hudViewport.update(width, height, true);
        ViewportUtils.debugPixelPerUnit(viewport);
    }

    public void dispose() {
        renderer.dispose();
        batch.dispose();
        font.dispose();
        stage.dispose();
        skin.dispose();
    }

    private void renderUI() {
        batch.setProjectionMatrix(hudCamera.combined);
        batch.begin();

        // draw senses
        String statusMessage = gameController.statusMessage;
        layout.setText(font, statusMessage);
        font.draw(
            batch,
            layout,
            PADDING,
            GameConfig.HUD_HEIGHT - layout.height
        );

        // draw score
        String scoreText = "SCORE: " + gameController.agent.getScore();
        layout.setText(font, scoreText);
        font.draw(
            batch,
            layout,
            GameConfig.HUD_WIDTH - layout.width - PADDING,
            GameConfig.HUD_HEIGHT - layout.height- PADDING
        );

        // draw senses
        String agentSenses = gameController.agent.getActionSense();
        layout.setText(font, agentSenses);
        font.draw(
            batch,
            layout,
            PADDING,
            PADDING
        );

        batch.end();
    }

    private void setupUI() {
        Table rootTable = new Table(skin);
        rootTable.setFillParent(true);
        rootTable.setDebug(true, true);

        //==========LOG-WINDOW==========
        Table logTable = new Table(skin);
        //logTable.setBackground("default-rect");
        logLabel = new Label("", skin);
        logLabel.setWrap(true);
        logScrollPane = new ScrollPane(logLabel, skin);
        logScrollPane.setFadeScrollBars(true);
        logScrollPane.setScrollingDisabled(true, false);
        logTable.add(logScrollPane).expand().fill().pad(2);
        rootTable.add(logTable).height(GameConfig.VIRTUAL_HEIGHT * 0.25f).expandX().fillX()/*.row()*/;

        //==========GAMEPLAY-AREA==========
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
                if (gameController.status == GameStatus.PLAYING) {
                    gameController.processAction(GameConfig.MOVE_FORWARD);
                    appendToLog(gameController.agent.actionSense);
                    if (gameController.status == GameStatus.WON) appendToLog(Messages.AGENT_WON_LOG);
                    appendToLog(gameController.statusMessage);
                    updateInfoBar(gameController.infoBarMessage);
                }
            }
        });
        TextButton turnLeftButton = new TextButton("TURN LEFT", skin);
        turnLeftButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (gameController.status == GameStatus.PLAYING) {
                    gameController.processAction(GameConfig.ROTATE_LEFT);
                    appendToLog(gameController.agent.actionSense);
                    appendToLog(gameController.statusMessage);
                    updateInfoBar(gameController.infoBarMessage);
                }
            }
        });
        TextButton turnRightButton = new TextButton("TURN RIGHT", skin);
        turnRightButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (gameController.status == GameStatus.PLAYING) {
                    gameController.processAction(GameConfig.ROTATE_RIGHT);
                    appendToLog(gameController.agent.actionSense);
                    appendToLog(gameController.statusMessage);
                    updateInfoBar(gameController.infoBarMessage);
                }
            }
        });
        leftButtons.add("").row();
        leftButtons.add(moveButton).row();
        leftButtons.add(turnLeftButton).padRight(5).row();
        leftButtons.add(turnRightButton).padLeft(5).row();
        leftButtons.add("").row();

        centerTable.add(leftButtons).width(GameConfig.VIRTUAL_WIDTH * 0.2f).expandY().fillY();
        centerTable.add().expand().fill();

        Table rightButtons = new Table(skin);
        rightButtons.defaults().pad(2).width(60).height(40);
        rightButtons.align(Align.center);
        TextButton searchButton = new TextButton("SEARCH", skin);
        searchButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (gameController.status == GameStatus.PLAYING) {
                    gameController.processAction(GameConfig.SEARCH);

                    if (gameController.agent.isHasGold()) {
                        updateInfoBar(Messages.GOT_GOLD_INFO);
                    }
                    appendToLog(gameController.agent.actionSense);
                    appendToLog(gameController.statusMessage);
                }
            }
        });
        TextButton specialButton = new TextButton("SPECIAL", skin);
        specialButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (gameController.status == GameStatus.PLAYING) {
                    gameController.processAction(GameConfig.SPECIAL);

                    appendToLog(gameController.agent.actionSense);
                    appendToLog(gameController.statusMessage);
                }
            }
        });
        rightButtons.add("").row();
        rightButtons.add(specialButton).row();
        rightButtons.add(searchButton).row();
        rightButtons.add("").row();

        centerTable.add(rightButtons).width(GameConfig.VIRTUAL_WIDTH * 0.2f).expandY().fillY();

        //==========INFO-WINDOW==========
        infoBarLabel = new ScrollingLabel(Messages.INFO, font, Color.WHITE, 20f);
        rootTable.add(infoBarLabel).height(GameConfig.VIRTUAL_HEIGHT * 0.1f).expandX().fillX().pad(2).row();

        rootTable.add(restartButton).pad(10).align(Align.bottomRight);
        stage.addActor(rootTable);
    }

    private  void appendToLog(String message) {
        logLabel.setText(logLabel.getText().toString() + "\n- " + message);
        logScrollPane.scrollTo(0,0,0,0);
        //TODO: write the log to a text file
    }

    private void updateInfoBar(String message) {
        infoBarLabel.setText(Messages.INFO + message);
    }

    private static class ScrollingLabel extends Actor {
        private final BitmapFont font;
        private final Color color;
        private String text;
        private float textWidth;
        private final float scrollSpeed;
        private float currentXOffset;
        private final GlyphLayout layout;
        private final float padding = 50;

        public ScrollingLabel(String text, BitmapFont font, Color color, float scrollSpeed) {
            this.font = font;
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
            super.act(delta);
            if (textWidth > getWidth()) {
                currentXOffset -= scrollSpeed * delta;
                if (currentXOffset <= -(textWidth + padding)) {
                    currentXOffset = 0;
                }
            }
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            font.setColor(color);
            font.draw(batch, text, getX() + currentXOffset, getY() + getHeight() / 2 + layout.height / 2);
            if (textWidth > getWidth()) {
                font.draw(batch, text, getX() + currentXOffset + textWidth + padding, getY() + getHeight() / 2 + layout.height / 2);
            }
        }
    }

}
