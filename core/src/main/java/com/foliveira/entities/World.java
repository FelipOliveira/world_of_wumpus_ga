package com.foliveira.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class World {

    private final int size;
    private final List<WorldObject> objects;
    private Agent agent;
    private Wumpus wumpus;
    private Gold gold;
    private Arrow arrow;
    private final Random random = new Random();
    private AbstractWorldObject[][] map;

    public World(int size) {
        this.size = size;
        this.objects = new ArrayList<>();
        init();
    }

    private void init(){
        map = new AbstractWorldObject[size][size];
        for (int i=0;i<size;i++){
            for (int j=0;j<size;j++){
                if (i==0 || i==size-1 || j==0 || j==size-1) map[i][j] = new Wall();
                else map[i][j] = new Empty();
            }
        }

        wumpus = new Wumpus(true);
        do {
            int x = random.nextInt(size);
            int y = random.nextInt(size);
            if (map[x][y] instanceof Empty && (x != 1 && y != 1)){
                map[x][y] = wumpus;
                break;
            }
        } while (true);

        // put the pits
        int numPits = 3;
        for (int i=0;i<numPits;i++){
            do {
                int x = random.nextInt(size);
                int y = random.nextInt(size);
                if (map[x][y] instanceof Empty && (x != 1 && y != 1)){
                    map[x][y] = new Pit();
                    break;
                }
            } while (true);
        }

        // put the gold
        do {
            int x = random.nextInt(size);
            int y = random.nextInt(size);
            if (map[x][y] instanceof Empty && (x != 1 && y != 1)){
                gold = new Gold(x, y, false);
                break;
            }
        } while (true);

        agent = new Agent(1,1,0);
        arrow = new Arrow(agent.getX(),agent.getY());
        map[agent.getX()][agent.getY()] = agent;
        display();
    }

    public void display() {
        for (int i=0;i<size;i++){
            for (int j=0;j<size;j++){
                if (i == gold.getX() && j == gold.getY() && !gold.isTaken()) {
                    System.out.print(" G ");
                } else {
                    map[i][j].display();
                }
            }
            if (i == 0) System.out.print("\tActions");
            if (i == 1) System.out.print("\t[LEFT] TURN LEFT");
            if (i == 2) System.out.print("\t[RIGHT] TURN RIGHT");
            if (i == 3) System.out.print("\t[UP] MOVE FORWARD");
            else if (i == 4) System.out.print("\t[Z] SHOOT ARROW");
            else if (i == 5) System.out.print("\t[X] GRAB GOLD");
            System.out.println();
        }
    }

    public void displayPercepts() {
        int percepts = agent.getPercepts(map, gold);
        System.out.print("You feel...");
        if ((percepts & agent.BREEZE) != 0) System.out.print("[BREEZE] ");
        if ((percepts & agent.STENCH) != 0) System.out.print("[STENCH] ");
        if ((percepts & agent.BUMP) != 0) System.out.print("[BUMP] ");
        if ((percepts & agent.GLITTER) != 0) System.out.print("[GLITTER] ");
        if ((percepts & agent.SCREAM) != 0) System.out.print("[SCREAM] ");
        if (percepts == 0) System.out.print("NOTHING");
        System.out.println(".");
    }

    public void processAction() {
        if (!isGameOver()) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) {
                agent.turnLeft();
                tick();
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
                agent.turnRight();
                tick();
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
                move(agent.getX(), agent.getY(), agent.getDirection());
                tick();
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.Z)) {
                shootArrow();
                tick();
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.X)) {
                agent.grabGold(gold);
                tick();
            }
        } else {
            if (Gdx.input.isKeyJustPressed(Input.Keys.R)){
                init();
            }
        }
    }

    private void tick() {
        if (isAgentWon() || !agent.isAlive()) gameOver();
        if (agent.isAlive()) {
            //displayWorld();
            display();
            displayPercepts();
        }
    }

    private boolean isAgentWon(){
        return agent.getX() == 1 && agent.getY() == 1 && agent.isAlive() && agent.isHasGold();
    }

    private void gameOver() {
        System.out.println("Game Over. Your score is " + agent.getScore() + "\nPress [R] to reset");
    }

    public void move(int x, int y, int direction) {
        int newX = x;
        int newY = y;

        switch (direction) {
            case 0: // right
                newY++;
                break;
            case 1: // down
                newX++;
                break;
            case 2: // left
                newY--;
                break;
            case 3: // up
                newX--;
                break;
        }

        if (map[newX][newY] instanceof Wall) {
            System.out.println("You hit a wall");
        }else {
            if (map[newX][newY] instanceof Gold) {
                System.out.println("You found gold!");
            } else if (map[newX][newY] instanceof Pit) {
                System.out.println("You fell on a pit!");
                agent.setAlive(false);
            } else if (map[newX][newY] instanceof Wumpus) {
                System.out.println("You got caught by the Wumpus!");
                agent.setAlive(false);
            }
            map[agent.getX()][agent.getY()] = new Empty();
            agent.setX(newX);
            agent.setY(newY);
            map[agent.getX()][agent.getY()] = agent;
        }
    }

    public void shootArrow() {
        if (agent.isHasArrow()) {
            agent.setHasArrow(false);
            agent.setScore(agent.getScore()-10);
            System.out.println("You shot the arrow!");
            int newX = agent.getX();
            int newY = agent.getY();

            arrow.setDirection(agent.getDirection());
            switch (arrow.getDirection()) {
                case 0: // right
                    for (int i=newY;i<=size;i++) {
                        System.out.println("x: " + newX + " y: " + i);
                        if (map[newX][i] instanceof Wumpus) {
                            System.out.println("You hear a [SCREAM]!");
                            wumpus.setAlive(false);
                            map[newX][i] = new Empty();
                            agent.setScore(agent.getScore()+100);
                            return;
                        }
                        if (map[newX][i] instanceof Wall) {
                            System.out.println("Your arrow hit the wall!");
                            if (map[newX][i-1] instanceof Empty){
                                arrow.setX(newX);
                                arrow.setY(i-1);
                            }
                            return;
                        }
                    }
                    break;
                case 1: // down
                    for (int i=newX;i<=size;i++) {
                        System.out.println("x: " + i + " y: " + newY);
                        if (map[i][newY] instanceof Wumpus) {
                            System.out.println("You hear a [SCREAM]!");
                            wumpus.setAlive(false);
                            map[i][newY] = new Empty();
                            agent.setScore(agent.getScore()+100);
                            return;
                        }
                        if (map[i][newY] instanceof Wall) {
                            System.out.println("Your arrow hit the wall!");
                            if (map[i][newY] instanceof Empty){
                                arrow.setX(i-1);
                                arrow.setY(newY);
                            }
                            return;
                        }
                    }
                    break;

                case 2: // left
                    for (int i=newY;i>=0;i--) {
                        System.out.println("x: " + newX + " y: " + i);
                        if (map[newX][i] instanceof Wumpus) {
                            System.out.println("You hear a [SCREAM]!");
                            wumpus.setAlive(false);
                            map[newX][i] = new Empty();
                            agent.setScore(agent.getScore()+100);
                            return;
                        }
                        if (map[newX][i] instanceof Wall) {
                            System.out.println("Your arrow hit the wall!");
                            if (map[newX][i+1] instanceof Empty){
                                arrow.setX(newX);
                                arrow.setY(i+1);
                            }
                            return;
                        }
                    }
                    break;
                case 3: // up
                    for (int i=newX;i>=0;i--) {
                        System.out.println("x: " + i + " y: " + newY);
                        if (map[i][newY] instanceof Wumpus) {
                            System.out.println("You hear a [SCREAM]!");
                            wumpus.setAlive(false);
                            map[i][newY] = new Empty();
                            agent.setScore(agent.getScore()+100);
                            return;
                        }
                        if (map[i][newY] instanceof Wall) {
                            System.out.println("Your arrow hit the wall!");
                            if (map[i+1][newY] instanceof Empty){
                                arrow.setX(i+1);
                                arrow.setY(newY);
                            }
                            return;
                        }
                    }
                    break;
            }
        } else {
            System.out.println("Your have no arrow left");
        }
    }

    public boolean isGameOver() {
        return !agent.isAlive() || isAgentWon();
    }

    public int getSize() {
        return this.size;
    }

    public List<WorldObject> getObjects() {
        return objects;
    }

    public void removeObject(WorldObject obj) {
        objects.remove(obj);
    }

    public Agent getAgent() {
        return agent;
    }

    public AbstractWorldObject[][] getMap() {
        return map;
    }

    public Wumpus getWumpus() {
        return wumpus;
    }
}
