package com.foliveira.screens.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.foliveira.config.GameConfig;
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

    private Stage stage;
    private Skin skin;
    private Label statusLabel;
    private Button restartButton;

    public GameRenderer(SpriteBatch batch, AssetManager assetManager, GameController gameController) {
        this.assetManager = assetManager;
        this.gameController = gameController;
        this.batch = batch;
        init();
    }

    public void init() {
        camera = new OrthographicCamera();
        viewport = new FitViewport(GameConfig.WORLD_WIDTH, GameConfig.WORLD_HEIGHT, camera);
        renderer = new ShapeRenderer();

        viewport.apply();

        camera.position.set(viewport.getWorldWidth()/2, viewport.getWorldHeight()/2, 0);
        camera.update();

        hudCamera = new OrthographicCamera();
        hudViewport = new FitViewport(GameConfig.HUD_WIDTH, GameConfig.HUD_HEIGHT, hudCamera);
        //batch = new SpriteBatch();
        //font = assetManager.get(AssetDescriptors.FONT);

        // create debug camera controller
        debugCameraController = new DebugCameraController();
        debugCameraController.setStartPosition(GameConfig.WORLD_CENTER_X, GameConfig.WORLD_CENTER_Y);

        //TextureAtlas gameAtlas = assetManager.get(AssetDescriptors.GAME_ATLAS);
        // create player and obstacles textures
        //playerRegion = gameAtlas.findRegion(RegionNames.PLAYER);
        //obstacleRegion = gameAtlas.findRegion(RegionNames.OBSTACLE);

        // create background texture
        //backgroundRegion = gameAtlas.findRegion(RegionNames.BACKGROUND);

        /*=======================SCENE2D MODE=======================*/
        skin = new Skin(Gdx.files.internal("ui/uiskin.json"));

        stage = new Stage(viewport, batch);
        Gdx.input.setInputProcessor(stage);

        Texture texture = new Texture(Gdx.files.internal("texture/iso.jpg"));

        Image background = new Image(texture);

        background.setSize(GameConfig.WORLD_WIDTH, GameConfig.WORLD_HEIGHT);
        stage.addActor(background);

        font = new BitmapFont(Gdx.files.internal("ui/press_start_2.fnt"));
        font.setColor(Color.WHITE);

        setupUI();
    }

    public void render(float delta) {
        debugCameraController.inputDebugHandle(delta);
        debugCameraController.applyToCamera(camera);

        GdxUtils.clearScreen();

        stage.act(delta);
        stage.draw();

        hudViewport.apply();
        renderUI();
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
        Table uiTable = new Table();
        uiTable.setFillParent(true);
        uiTable.align(Align.bottomLeft);

        statusLabel = new Label("starting game", skin);
        statusLabel.setFontScale(1.2f);
        statusLabel.setColor(Color.YELLOW);
        uiTable.add(statusLabel).pad(10).expandX().align(Align.left);
        uiTable.row();

        Table movementButtons = new Table(skin);
        movementButtons.defaults().pad(5).width(80).height(60);
        TextButton moveButton = new TextButton("move", skin);
        moveButton.addListener(new ClickListener() {
           @Override
           public void clicked(InputEvent event, float x, float y) {
               gameController.action = GameConfig.MOVE_FORWARD;
               gameController.validate();
           }
        });

        TextButton turnLeftButton = new TextButton("turn left", skin);
        turnLeftButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                gameController.action = GameConfig.ROTATE_LEFT;
                gameController.validate();
            }
        });

        TextButton turnRightButton = new TextButton("turn right", skin);
        turnRightButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                gameController.action = GameConfig.ROTATE_RIGHT;
                gameController.validate();
            }
        });

        movementButtons.add("").row();
        movementButtons.add(moveButton).center().row();
        movementButtons.add(turnLeftButton).left().row();
        movementButtons.add(turnRightButton).right().row();

        uiTable.add(movementButtons).pad(10).align(Align.bottomLeft);

        Table actionButtons = new Table(skin);
        actionButtons.defaults().pad(5).width(80).height(60);

        TextButton searchButton = new TextButton("search", skin);
        searchButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                gameController.action = GameConfig.SEARCH;
                gameController.validate();
            }
        });

        TextButton specialButton = new TextButton("special", skin);
        specialButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                gameController.action = GameConfig.SPECIAL;
                gameController.validate();
            }
        });

        actionButtons.add("").row();
        actionButtons.add(searchButton).left().row();
        actionButtons.add(specialButton).right().row();

        uiTable.add(actionButtons).pad(10).align(Align.bottomRight);

        restartButton = new TextButton("restart", skin);
        restartButton.setVisible(false);
        restartButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                gameController.restart();
                restartButton.setVisible(false);
                updateStatusLabel();
            }
        });
        uiTable.add(restartButton).pad(10).align(Align.bottomRight);
        stage.addActor(uiTable);
    }

    private void updateStatusLabel() {

    }
}
