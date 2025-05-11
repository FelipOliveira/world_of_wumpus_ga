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

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class WumpusGame extends Game {
    //private Stage stage;
    //private Skin skin;

    private AssetManager assetManager;
    private SpriteBatch batch;

    //=========================================================================
    int[][] cave;
    int[] player = new int[] {0,0};
    int[] wumpus;
    int[] gold;
    int[] pit1;
    int[] pit2;
    int[] pit3;
    Array<int[]> breeze = new Array<>();
    Array<int[]> stench = new Array<>();

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
        cave = new int[][]{
            {0, 0, 0, 0},
            {0, 0, 0, 0},
            {0, 0, 0, 0},
            {0, 0, 0, 0}
        };

        //player = new int[] {0,0};
        //gold = new int[] {2,1};
        //wumpus = new int[] {2,0};
        addGold();
        addWumpus();
        stench.add(
            new int[] {wumpus[0] - 1, wumpus[1]},
            new int[] {wumpus[0], wumpus[1] - 1},
            new int[] {wumpus[0] + 1, wumpus[1]},
            new int[] {wumpus[0], wumpus[1] + 1}
        );

        pit1 = addPit();
        pit2 = addPit();
        pit3 = addPit();

        printCave();
        //=========================================================================
    }

    @Override
    public void render() {
        ScreenUtils.clear(0f, 0f, 0f, 1f);
        //=========================================================================
        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN) && player[0] + 1 < cave.length) {
            player[0] += 1;
            printCave();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP) && player[0] - 1 >= 0) {
            player[0] -= 1;
            printCave();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT) && player[1] + 1 < cave.length) {
            player[1] += 1;
            printCave();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT) && player[1] - 1 >= 0) {
            player[1] -= 1;
            printCave();
        }
        //=========================================================================

        /*stage.act(Gdx.graphics.getDeltaTime());
        stage.draw();*/
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
                else if (gold[0] == lin && gold[1] == col) System.out.print(" G ");
                else if (wumpus[0] == lin && wumpus[1] == col) System.out.print(" W ");
                else if (wumpus[0] - 1 == lin && wumpus[1] == col) System.out.print(" S ");
                else if (wumpus[0] == lin && wumpus[1] -1 == col) System.out.print(" S ");
                else if (wumpus[0] + 1 == lin && wumpus[1] == col) System.out.print(" S ");
                else if (wumpus[0] == lin && wumpus[1] + 1 == col) System.out.print(" S ");

                else System.out.print(" " + 0 + " ");
            }
            System.out.print("\n");
        }
        System.out.print("player:[" + player[0] + "][" + player[1] + "]");
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

}
