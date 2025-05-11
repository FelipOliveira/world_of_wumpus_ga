package com.foliveira.screens.loading;

import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.foliveira.WumpusGame;
import com.foliveira.assets.AssetDescriptors;
import com.foliveira.config.GameConfig;
import com.foliveira.screens.game.GameScreen;
import com.foliveira.utils.GdxUtils;

public class LoadingScreen extends ScreenAdapter {
    private static final float PROGRESS_BAR_WIDTH = GameConfig.HUD_WIDTH / 2f;
    private static final float PROGRESS_BAR_HEIGHT = 60;

    // Attributes

    private OrthographicCamera camera;
    private Viewport viewport;
    private ShapeRenderer renderer;

    private float progress;
    private float waitTime = 0.75f;

    private final WumpusGame game;
    private final AssetManager assetManager;

    private float progressBarX = (GameConfig.HUD_WIDTH - PROGRESS_BAR_WIDTH) / 2f;
    private float progressBarY = (GameConfig.HUD_HEIGHT - PROGRESS_BAR_HEIGHT) / 2f;

    // Constructor
    public LoadingScreen(WumpusGame game) {
        this.game = game;
        assetManager = game.getAssetManager();
    }

    // Public methods

    @Override
    public void show() {
        camera = new OrthographicCamera();
        viewport = new FitViewport(GameConfig.HUD_WIDTH, GameConfig.HUD_HEIGHT, camera);
        renderer = new ShapeRenderer();

        assetManager.load(AssetDescriptors.FONT);
        assetManager.load(AssetDescriptors.GAME_ATLAS);
        assetManager.load(AssetDescriptors.UI_SKIN);
        assetManager.load(AssetDescriptors.HIT_SOUND);
    }

    @Override
    public void render(float delta) {
        update(delta);
        GdxUtils.clearScreen();
        viewport.apply();
        renderer.setProjectionMatrix(camera.combined);
        renderer.begin(ShapeRenderer.ShapeType.Filled);

        draw();

        renderer.end();
        drawProgressBar();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void hide() {
        dispose();
    }

    @Override
    public void dispose() {
        renderer.dispose();
        //assetManager.dispose();
    }

    // private methods

    private void update(float delta) {
        progress = assetManager.getProgress();
        if (assetManager.update()) {
            waitTime -= delta;
            if (waitTime <= 0) {
                //game.setScreen(new MenuScreen(game));
                game.setScreen(new GameScreen(game));
            }
        }
    }

    private void draw() {
        renderer.rect(
            progressBarX,
            progressBarY,
            progress * PROGRESS_BAR_WIDTH,
            PROGRESS_BAR_HEIGHT
        );
    }

    private void drawProgressBar() {
        viewport.apply();
        renderer.setProjectionMatrix(camera.combined);
        renderer.begin(ShapeRenderer.ShapeType.Line);
        renderer.rect(
            progressBarX,
            progressBarY,
            PROGRESS_BAR_WIDTH,
            PROGRESS_BAR_HEIGHT
        );
        renderer.end();
    }
}
