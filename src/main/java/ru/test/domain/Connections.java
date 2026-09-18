package ru.test.domain;

import java.util.UUID;

public class Connections {
    private UUID friendId;
    private int connection;

    public Connections(){
    }
    public Connections(UUID friendId, int connection ){
        this.friendId = friendId;
        this.connection = connection;
    }
    public UUID getFriendId(){
        return friendId;
    }
    public int getConnection(){
        return connection;
    }

    public void setId(UUID friendId){
        this.friendId = friendId;
    }
    public void setConnection(int connection){
        this.connection = connection;
    }

}
