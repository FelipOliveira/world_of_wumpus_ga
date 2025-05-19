package com.foliveira.entities;

public class Wumpus extends AbstractWorldObject {
    private boolean alive;

    public Wumpus(boolean alive) {
        this.alive = alive;
    }

    @Override
    public void display() {
        System.out.print(alive ? " W " : "   ");
    }

    public void setAlive(boolean alive) {
        this.alive = alive;
    }

    public boolean isAlive() {
        return alive;
    }
}
