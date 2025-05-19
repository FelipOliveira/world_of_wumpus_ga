package com.foliveira.entities;

public class Agent extends AbstractWorldObject {

    private int x;
    private int y;

    private int direction; // 0: right, 1: down, 2: left, 3: up
    private boolean hasArrow;
    private int score;
    private boolean alive;
    private boolean hasGold;

    // percepts
    final int BREEZE = 1;
    final int STENCH = 2;
    final int GLITTER = 4;
    final int BUMP = 8;
    final int SCREAM = 16;

    public Agent(int x, int y, int direction) {
        this.x = x;
        this.y = y;
        this.direction = direction;
        this.hasArrow = true;
        this.score = 0;
        this.alive = true;
        this.hasGold = false;
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
        System.out.println("You turn to left");
    }

    public void turnRight() {
        direction = (direction + 1) % 4;
        score--;
        System.out.println("You turn to left");
    }

    public void grabGold(Gold gold) {
        if (x == gold.getX() && y == gold.getY()) {
            System.out.println("You found gold!");
            hasGold = true;
            score += 500;
            gold.setTaken(true);
        } else {
            System.out.println("You found nothing!");
        }
    }

    public int getPercepts(AbstractWorldObject[][] map, Gold gold) {
        int percepts = 0;
        // check pit's breeze (adjacent)
        if (
            map[x+1][y] instanceof Pit ||
            map[x-1][y] instanceof Pit ||
            map[x][y-1] instanceof Pit ||
            map[x][y+1] instanceof Pit
        ) percepts |= BREEZE;

        // check wumpus's stench (adjacent)
        if (
            map[x+1][y] instanceof Wumpus ||
            map[x-1][y] instanceof Wumpus ||
            map[x][y-1] instanceof Wumpus ||
            map[x][y+1] instanceof Wumpus
        ) percepts |= STENCH;

        // check gold's glitter
        if (x == gold.getX() && y == gold.getY() && !gold.isTaken()) percepts |= GLITTER;

        return percepts;
    }

    /*public void shootArrow(World world) {
        if (hasArrow) {
            var map = world.getMap();
            //hasArrow = false;
            score -= 10;
            System.out.println("You shot the arrow!");
            int newX = ;
            int newY = agentY;

            switch (direction) {
                case 0: // right
                    for (int i=newX;i<=world.getSize();i++) {
                        if (map[newX][i] instanceof Wumpus) {
                            System.out.println("You hear a [SCREAM]!");
                            world.getWumpus = false;
                            map[newX][i] = new Empty();
                            score += 100;
                            return;
                        }
                        if (map[newX][i] instanceof Wall) {
                            System.out.println("Your arrow hit the wall!");
                            if (map[newX][i-1] == EMPTY){
                                map[newX][i-1] = ARROW;
                            }
                            return;
                        }
                    }
                    break;
                case 1: // down
                    *//*for (int i=y;i<worldSize;i++) {
                        if (world[i][y] == WUMPUS) {
                            System.out.println("You hear a [SCREAM]!");
                            wumpusAlive = false;
                            world[i][y] = EMPTY;
                            score += 500;
                            return;
                        }
                        if (world[i][y] == WALL) {
                            System.out.println("Your arrow hit the wall!");
                            if (world[i-1][y] == EMPTY){
                                world[i-1][y] = ARROW;
                            }
                            return;
                        }
                    }*//*
                    for (int i=y;i<worldSize;i++) {
                        if (world[i][y] == WUMPUS) {
                            System.out.println("You hear a [SCREAM]!");
                            wumpusAlive = false;
                            world[i][y] = EMPTY;
                            score += 500;
                            return;
                        }
                        if (world[i][y] == WALL) {
                            System.out.println("Your arrow hit the wall!");
                            if (world[i-1][y] == EMPTY){
                                world[i-1][y] = ARROW;
                            }
                            return;
                        }
                    }
                    break;
                case 2: // left
                    for (int i=newX;i>=0;i--) {
                        if (world[newX][i] == WUMPUS) {
                            System.out.println("You hear a [SCREAM]!");
                            wumpusAlive = false;
                            world[newX][i] = EMPTY;
                            score += 500;
                            return;
                        }
                        if (world[newX][i] == WALL) {
                            System.out.println("Your arrow hit the wall!");
                            if (world[newX][i+1] == EMPTY){
                                world[newX][i+1] = ARROW;
                            }
                            return;
                        }
                    }
                    break;
                case 3: // up
                    for (int i=y;i>=0;i--) {
                        if (world[i][y] == WUMPUS) {
                            System.out.println("You hear a [SCREAM]!");
                            wumpusAlive = false;
                            world[i][y] = EMPTY;
                            score += 500;
                            return;
                        }
                        if (world[i][y] == WALL) {
                            System.out.println("Your arrow hit the wall!");
                            if (world[i+1][y] == EMPTY){
                                world[i+1][y] = ARROW;
                            }
                            return;
                        }
                    }
                    break;
            }
            System.out.println("You lose your arrow!");
        } else {
            System.out.println("Your have no arrow left");
        }
    }*/

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
}
