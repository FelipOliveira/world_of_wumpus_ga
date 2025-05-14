package com.foliveira.utils.manager;

import com.badlogic.gdx.utils.Array;

public class GameStateManager {
    private GameState currentState;
    private final Array<StateListener> listeners = new Array<>();

    public GameStateManager(GameState initialState) {
        this.currentState = initialState;
    }

    public GameStateManager() {
    }

    public GameState getCurrentState() {
        return currentState;
    }

    public void setCurrentState(GameState newState) {
        if (this.currentState != newState) {
            this.currentState = newState;
            notifyStateChanged(newState);
        }
    }

    public interface StateListener {
        void onStageChanged(GameState newState);
    }

    public void addListener(StateListener listener) {
        listeners.add(listener);
    }

    public void removeListener(StateListener listener){
        listeners.removeValue(listener, true);
    }

    private void notifyStateChanged(GameState newState) {
        for (StateListener listener : listeners) {
            listener.onStageChanged(newState);
        }
    }

    public void enterSense() {
        setCurrentState(GameState.SENSE);
    }

    public void enterTrap() {
        setCurrentState(GameState.TRAP);
    }

    public void enterAction() {
        setCurrentState(GameState.ACTION);
    }

    public void enterMove() {
        setCurrentState(GameState.MOVE);
    }

    public void enterDisplay() {
        setCurrentState(GameState.DISPLAY);
    }
}
