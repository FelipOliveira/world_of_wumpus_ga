package com.foliveira.entities;

public class Gold extends AbstractWorldObject{
    private int x, y;
    private boolean taken;

    public Gold(int x, int y, boolean taken) {
        this.x = x;
        this.y = y;
        this.taken = taken;
    }

    @Override
    public void display() {
        System.out.print(" G ");
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public boolean isTaken() {
        return taken;
    }

    public void setTaken(boolean taken) {
        this.taken = taken;
    }
}
