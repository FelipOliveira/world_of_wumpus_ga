package com.foliveira.entities;

public class Empty extends AbstractWorldObject {

    public Empty() {
    }

    @Override
    public void display() {
        System.out.print("   ");
    }

}
