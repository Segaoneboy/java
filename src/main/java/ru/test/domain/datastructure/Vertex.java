package ru.test.domain.datastructure;

import ru.test.domain.Profile;

public class Vertex {
    private final Profile profile;
    private Vertex[] neighbors;
    int neighborCount;

    public Vertex(Profile profile, int initialCapacity){
        this.profile = profile;
        this.neighbors = new Vertex[initialCapacity];
        this.neighborCount = 0;
    }

    public void addNeighbor(Vertex neighbor){
        for (int i = 0; i < neighborCount; i++){
            if (neighbors[i] == neighbor) return;
        }

        if (neighborCount >= neighbors.length){
            Vertex[] newNeighbors = new Vertex[neighbors.length * 2];
            System.arraycopy(neighbors, 0, newNeighbors, 0, neighborCount);
            neighbors = newNeighbors;
        }

        neighbors[neighborCount++] = neighbor;
    }
    public Profile getProfile() {
        return profile;
    }

    public Vertex[] getNeighbors() {
        return neighbors;
    }

    public int getNeighborCount() {
        return neighborCount;
    }
}
