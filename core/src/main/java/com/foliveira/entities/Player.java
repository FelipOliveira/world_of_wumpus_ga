package com.foliveira.entities;

public class Player {
    private int locX;
    private int locY;
    private boolean isAlive;
    private boolean hasGold;
    private boolean hasArrow;

    public Player(int locX, int locY, boolean isAlive, boolean hasGold, boolean hasArrow) {
        this.locX = locX;
        this.locY = locY;
        this.isAlive = isAlive;
        this.hasGold = hasGold;
        this.hasArrow = hasArrow;
    }

    public Player() {
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

    public boolean isAlive() {
        return isAlive;
    }

    public void setAlive(boolean alive) {
        isAlive = alive;
    }

    public boolean isHasGold() {
        return hasGold;
    }

    public void setHasGold(boolean hasGold) {
        this.hasGold = hasGold;
    }

    public boolean isHasArrow() {
        return hasArrow;
    }

    public void setHasArrow(boolean hasArrow) {
        this.hasArrow = hasArrow;
    }
}
