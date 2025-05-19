package com.foliveira.entities;

public class Wall extends AbstractWorldObject {

    public Wall() {

    }

    @Override
    public void display() {
        System.out.print(" # ");
    }
}
