package com.foliveira.commom;

import com.badlogic.gdx.utils.Array;
import com.foliveira.config.GameConfig;
import com.foliveira.entities.AbstractWorldObject;
import com.foliveira.entities.Pit;
import com.foliveira.entities.Wumpus;

import java.util.Comparator;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Set;

public class PathFinder {

    private static boolean findPath(int startX, int startY, int endX, int endY, AbstractWorldObject[][] map) {
        PriorityQueue<Node> openList = new PriorityQueue<>(Comparator.comparingInt(Node::fCost));
        Set<Node> closedList = new HashSet<>();
        Node startNode = new Node(startX, startY);
        startNode.hCost = calculateHeuristic(startX, startY, endX, endY);
        openList.add(startNode);
        while (!openList.isEmpty()) {
            Node currentNode = openList.poll();
            if (currentNode.x == endX && currentNode.y == endY) return true;
            closedList.add(currentNode);
            Array<Node> neighbors = getNeighbors(currentNode.x, currentNode.y, map);
            for (Node neighbor : neighbors) {
                if (closedList.contains(neighbor)) continue;
                int newGCost = currentNode.gCost + 1;
                if (!openList.contains(neighbor) || newGCost < neighbor.gCost) {
                    neighbor.gCost = newGCost;
                    neighbor.hCost = calculateHeuristic(neighbor.x, neighbor.y, endX, endY);
                    neighbor.parent = currentNode;
                    if (!openList.contains(neighbor)) {
                        openList.add(neighbor);
                    } else {
                        openList.remove(neighbor);
                        openList.add(neighbor);
                    }
                }
            }
        }
        return false;
    }

    private static Array<Node> getNeighbors(int x, int y, AbstractWorldObject[][] map) {
        Array<Node> neighbors = new Array<>();
        int[] dx = {0, 0, -1, 1};
        int[] dy = {-1, 1, 0, 0};

        for (int i=0;i<4;i++) {
            int newX = x + dx[i];
            int newY = y + dy[i];
            if ((newX>=0 && newX<GameConfig.WORLD_SIZE &&
                newY>=0 && newY<GameConfig.WORLD_SIZE) &&
                (map[newX][newY] instanceof Pit ||
                map[newX][newY] instanceof Wumpus)) {
                neighbors.add(new Node(newX, newY));
            }
        }
        return neighbors;
    }

    private static int calculateHeuristic(int startX, int startY, int endX, int endY) {
        return Math.abs(endX - startX) + Math.abs(endY - startY);
    }

    public static boolean hasValidPathToGold(int startX, int startY, int endX, int endY, AbstractWorldObject[][] map) {
        return findPath(startX, startY, endX, endY, map);
    }

    public static boolean hasValidPathToWumpus(int startX, int startY, int endX, int endY, AbstractWorldObject[][] map) {
        return findPath(startX, startY, endX, endY, map);
    }
}
