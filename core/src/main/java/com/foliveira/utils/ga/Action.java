package com.foliveira.utils.ga;

import java.util.Random;
public enum Action {
    MOVE_FORWARD, TURN_LEFT, TURN_RIGHT, SHOOT, GRAB;

    public static Action getRandomAction() {
        Random random = new Random();
        Action[] actions = Action.values();
        return actions[random.nextInt(actions.length)];
    }
}
