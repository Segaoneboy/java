package ru.test.domain.datastructure;

import ru.test.domain.Profile;

public class Graph {
    private Vertex[] vertices;
    private int vertexCount;

    public Graph(int initialCapacity){
        vertices = new Vertex[initialCapacity];
        vertexCount = 0;
    }

    public void addVertex(Profile profile){
        if (findVertexById(profile.getId()) != null){
            return;
        }

        if (vertexCount >= vertices.length){
            Vertex[] newVertices = new Vertex[vertices.length * 2];
            System.arraycopy(vertices, 0, newVertices, 0, vertices.length);
            vertices = newVertices;
        }

        vertices[vertexCount++] = new Vertex(profile, 4);
    }

    public void addEdge(int id1, int id2){
        Vertex v1 = findVertexById(id1);
        Vertex v2 = findVertexById(id2);

        if (v1 != null && v2 != null){
            v1.addNeighbor(v2);
            v2.addNeighbor(v1);
        }
    }

    public Vertex findVertexById(int id){
        for (int i = 0; i < vertexCount; i++){
            if (vertices[i].getProfile().getId() == id){
                return vertices[i];
            }
        }
        return null;
    }

    public Vertex[] getVertices() {
        return vertices;
    }

    public int getVertexCount() {
        return vertexCount;
    }
}