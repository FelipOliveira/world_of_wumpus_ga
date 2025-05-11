package com.foliveira.commom;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.foliveira.WumpusGame;

public class GameManager {
    public static final GameManager INSTANCE = new GameManager();

    private static final String HIGH_SCORE_KEY = "highscore";
    private Preferences PREFS;
    private int highScore = 0;

    private GameManager() {
        PREFS = Gdx.app.getPreferences(WumpusGame.class.getSimpleName());
        highScore = PREFS.getInteger(HIGH_SCORE_KEY, 0);
    }

    public void updateHighScore(int score) {
        if (score < highScore) return;
        highScore = score;
        PREFS.putInteger(HIGH_SCORE_KEY, highScore);
        PREFS.flush();
    }

    public String getHighScoreString() {
        return String.valueOf(highScore);
    }

}
