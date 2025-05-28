package com.foliveira.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.foliveira.commom.PathFinder;
import com.foliveira.config.GameConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class World {

    private final int size;

    private AbstractWorldObject[][] map;

    public World(int size) {
        this.size = size;
        //this.objects = new ArrayList<>();
    }

    public AbstractWorldObject[][] getMap() {
        return this.map;
    }
}
