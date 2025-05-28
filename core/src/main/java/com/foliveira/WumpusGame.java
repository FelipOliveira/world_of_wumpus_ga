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
import com.foliveira.config.GameConfig;
import com.foliveira.entities.World;
import com.foliveira.entities.WorldObject;
import com.foliveira.screens.game.GameScreen;
import com.foliveira.screens.loading.LoadingScreen;
import com.foliveira.utils.GdxUtils;
import com.foliveira.utils.manager.GameState;
import com.foliveira.utils.manager.GameStateManager;

import java.util.List;
import java.util.Random;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class WumpusGame extends Game {
    //private Stage stage;
    //private Skin skin;

    private AssetManager assetManager;
    private SpriteBatch batch;

    //=========================================================================

    @Override
    public void create() {
        Gdx.app.setLogLevel(Application.LOG_DEBUG);

        assetManager = new AssetManager();
        assetManager.getLogger().setLevel(Logger.DEBUG);
        batch = new SpriteBatch();

        //setScreen(new LoadingScreen(this));
        setScreen(new GameScreen(this));



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

    }

    /*@Override
    public void render() {
        GdxUtils.clearScreen();
        //=========================================================================
        //objWorld.processAction();

        //=========================================================================

        *//*stage.act(Gdx.graphics.getDeltaTime());
        stage.draw();*//*
    }*/

   /* @Override
    public void resize(int width, int height) {
        *//*stage.getViewport().update(width, height);*//*
    }*/

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
/*
    @Override
    public void onStageChanged(GameState newState) {
        switch (manager.getCurrentState()){
            case PERCEPTION:
                objWorld.displayPercepts();
                manager.enterAction();
                break;
            case ACTION:
                //objWorld.processAction();
                manager.enterValidation();
                break;
            case VALIDATION:
                //objWorld.validate();
                manager.enterPerception();
                break;
        }
    }*/

}
