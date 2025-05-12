package com.foliveira.entities;

public class Player {

    private int locX;
    private int locY;
    private boolean isAlive;
    private boolean hasArrow;
    private boolean hasGold;

    public Player() {
        this.locX = 0;
        this.locY = 0;
        this.isAlive = true;
        this.hasArrow = true;
        this.hasGold = false;
    }

    public void moveDown() {

        setLocY(getLocY() - 1);
    }

    public void moveUp() {

        setLocY(getLocY() + 1);
    }

    public void moveLeft() {

        setLocX(getLocX() - 1);
    }

    public void moveRight() {

        setLocX(getLocX() + 1);
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
