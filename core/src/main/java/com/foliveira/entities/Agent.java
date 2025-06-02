package com.foliveira.entities;

import com.foliveira.config.GameConfig;

public class Agent extends AbstractWorldObject {

    private int x;
    private int y;

    private int direction; // 0: right, 1: down, 2: left, 3: up
    private boolean hasArrow;
    private int score;
    private boolean alive;
    private boolean hasGold;
    private int percepts;

    public String actionSense;

    public Agent(int x, int y, int direction) {
        this.x = x;
        this.y = y;
        this.direction = direction;
        this.hasArrow = true;
        this.score = 0;
        this.alive = true;
        this.hasGold = false;
        this.percepts = 0;
        this.actionSense = "";
    }

    @Override
    public void display() {
        switch (direction) {
            case 0:
                System.out.print(" → ");
                break;
            case 1:
                System.out.print(" ↓ ");
                break;
            case 2:
                System.out.print(" ← ");
                break;
            case 3:
                System.out.print(" ↑ ");
                break;
        }
    }

    public void turnLeft() {
        direction = (direction + 3) % 4;
        score--;
        System.out.println("You turned to left");
        actionSense = "You turned to left";

    }

    public void turnRight() {
        direction = (direction + 1) % 4;
        score--;
        System.out.println("You turned to right");
        actionSense = "You turned to right";
    }

    public void grabGold(Gold gold) {
        if (x == gold.getX() && y == gold.getY() && !hasGold) {
            System.out.println("You found gold!");
            actionSense = "You found gold!" ;
            hasGold = true;
            score += 500;
            gold.setTaken(true);
        } else {
            System.out.println("You found nothing...");
            actionSense = "You found nothing...";
            score -= 10;
        }
    }

    public int getPassivePercepts(AbstractWorldObject[][] map, Gold gold) {
        percepts = 0;
        // check pit's breeze (adjacent)
        if (
            map[x+1][y] instanceof Pit ||
            map[x-1][y] instanceof Pit ||
            map[x][y-1] instanceof Pit ||
            map[x][y+1] instanceof Pit
        ) percepts |= GameConfig.BREEZE;

        // check wumpus's stench (adjacent)
        if (
            map[x+1][y] instanceof Wumpus ||
            map[x-1][y] instanceof Wumpus ||
            map[x][y-1] instanceof Wumpus ||
            map[x][y+1] instanceof Wumpus
        ) percepts |= GameConfig.STENCH;

        // check gold's glitter
        if (x == gold.getX() && y == gold.getY() && !gold.isTaken()) percepts |= GameConfig.GLITTER;

        return percepts;
    }

    public void move(AbstractWorldObject[][] map) {
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
            getBump();
            score -= 2;
        }else {
            if (map[newX][newY] instanceof Pit) {
                System.out.println("You fell on a pit!");
                actionSense = "You fell on a pit!";
                setAlive(false);
            } else if (map[newX][newY] instanceof Wumpus) {
                System.out.println("You got caught by the Wumpus!");
                actionSense = "You got caught by the Wumpus!";
                setAlive(false);
            }
            map[getX()][getY()] = new Empty();
            setX(newX);
            setY(newY);
            map[getX()][getY()] = this;
            if (isAlive())actionSense = "You moved forward";
            score--;
        }
    }

    public void shootArrow(Arrow arrow, Wumpus wumpus, AbstractWorldObject[][] map) {
        if (isHasArrow()) {
            setHasArrow(false);
            setScore(getScore()-10);
            System.out.println("You shot the arrow!");
            actionSense = "You shot the arrow!";
            int newX = getX();
            int newY = getY();

            arrow.setDirection(getDirection());
            switch (arrow.getDirection()) {
                case 0: // right
                    for (int i = newY; i<=GameConfig.MAP_SIZE; i++) {
                        System.out.println("x: " + newX + " y: " + i);
                        if (map[newX][i] instanceof Wumpus) {
                            System.out.println("You hear a [SCREAM]!");
                            getScream();
                            wumpus.setAlive(false);
                            map[newX][i] = new Empty();
                            setScore(getScore()+100);
                            return;
                        }
                        if (map[newX][i] instanceof Wall) {
                            System.out.println("Your arrow hit the wall!");
                            actionSense = actionSense.concat("\nYour arrow hit the wall!");
                            if (map[newX][i-1] instanceof Empty){
                                arrow.setX(newX);
                                arrow.setY(i-1);
                            }
                            return;
                        }
                    }
                    break;
                case 1: // down
                    for (int i = newX; i<= GameConfig.MAP_SIZE; i++) {
                        System.out.println("x: " + i + " y: " + newY);
                        if (map[i][newY] instanceof Wumpus) {
                            System.out.println("You hear a [SCREAM]!");
                            getScream();
                            wumpus.setAlive(false);
                            map[i][newY] = new Empty();
                            setScore(getScore()+100);
                            return;
                        }
                        if (map[i][newY] instanceof Wall) {
                            System.out.println("Your arrow hit the wall!");
                            actionSense = actionSense.concat("\nYour arrow hit the wall!");
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
                            getScream();
                            wumpus.setAlive(false);
                            map[newX][i] = new Empty();
                            setScore(getScore()+100);
                            return;
                        }
                        if (map[newX][i] instanceof Wall) {
                            System.out.println("Your arrow hit the wall!");
                            actionSense = actionSense.concat("\nYour arrow hit the wall!");
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
                            getScream();
                            wumpus.setAlive(false);
                            map[i][newY] = new Empty();
                            setScore(getScore()+100);
                            return;
                        }
                        if (map[i][newY] instanceof Wall) {
                            System.out.println("Your arrow hit the wall!");
                            actionSense = actionSense.concat("\nYour arrow hit the wall!");
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
            actionSense = "Your have no arrows left";
        }
    }

    public void getBump() {
        percepts |= GameConfig.BUMP;
        System.out.println("You hit a wall");
        actionSense = "You hit a wall";
    }

    public void getScream() {
        percepts |= GameConfig.SCREAM;
        actionSense = actionSense.concat("\nYou hear a [SCREAM]!");
    }

    public int getDirection() {
        return direction;
    }

    public int getScore() {
        return score;
    }

    public boolean isAlive() {
        return alive;
    }

    public boolean isHasGold() {
        return hasGold;
    }

    public void setHasArrow(boolean hasArrow) {
        this.hasArrow = hasArrow;
    }

    public boolean isHasArrow() {
        return hasArrow;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public void setAlive(boolean alive) {
        this.alive = alive;
    }

    public void setHasGold(boolean hasGold) {
        this.hasGold = hasGold;
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

    public int getPercepts() {
        return percepts;
    }

    public void setPercepts(int percepts) {
        this.percepts = percepts;
    }

    public String getActionSense() {
        return actionSense;
    }

    public void setActionSense(String actionSense) {
        actionSense = actionSense;
    }
}
