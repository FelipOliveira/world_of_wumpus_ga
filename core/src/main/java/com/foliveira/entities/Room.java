package com.foliveira.entities;

import com.badlogic.gdx.utils.Array;

public class Room {
    private int locX;
    private int locY;

    private final Array<Sense> senses = new Array<>();

    public Room(int locX, int locY) {
        this.locX = locX;
        this.locY = locY;
    }

    public int getLocX() {
        return locX;
    }

    public void setLocX(int locX) {
        this.locX = locX;
    }

    public int getLocY() {
        return locY;
    }

    public void setLocY(int locY) {
        this.locY = locY;
    }

    public Array<Sense> getSenses() {
        return senses;
    }

    public void addSense(Sense sense) {
        this.senses.add(sense);
    }

    public void removeSense(Sense sense) {
        this.senses.removeValue(sense,true);
    }
}
