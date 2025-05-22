package com.foliveira.commom;

import java.util.Objects;

public class Node {
    int x, y;
    int gCost, hCost;
    Node parent;

    public Node(int x, int y) {
        this.x = x;
        this.y = y;
        this.gCost = 0;
        this.hCost = 0;
        this.parent = null;
    }

    public int fCost() {
        return gCost + hCost;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Node node = (Node) obj;
        return x == node.x && y == node.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }
}
