package com.foliveira.entities;

public abstract class CaveObject {
    private int locX;
    private int locY;

    public CaveObject(int locX, int locY) {
        this.locX = locX;
        this.locY = locY;
    }

    public CaveObject() {
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
}
