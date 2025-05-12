package com.foliveira.entities;

public class Wumpus extends CaveObject {
    private boolean alive;

    public Wumpus(int locX, int locY) {
        super(locX, locY);
        this.alive = true;
    }

    public boolean isAlive() {
        return alive;
    }

    public void setAlive(boolean alive) {
        this.alive = alive;
    }
}
