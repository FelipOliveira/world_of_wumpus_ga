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
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
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
        stage = new Stage(viewport, batch);
        Texture texture = new Texture(Gdx.files.internal("texture/iso.jpg"));

        Image background = new Image(texture);

        background.setSize(GameConfig.WORLD_WIDTH, GameConfig.WORLD_HEIGHT);
        //stage.addActor(background);

        font = new BitmapFont(Gdx.files.internal("ui/press_start_2.fnt"));

    }

    public void render(float delta) {
        debugCameraController.inputDebugHandle(delta);
        debugCameraController.applyToCamera(camera);

        GdxUtils.clearScreen();

        stage.act();
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

}
