package ru.test.domain;


public class Connections {
    private int friendId;
    private int connection;

    public Connections(){
    }
    public Connections(int friendId, int connection ){
        this.friendId = friendId;
        this.connection = connection;
    }
    public int getFriendId(){
        return friendId;
    }
    public int getConnection(){
        return connection;
    }

    public void setId(int friendId){
        this.friendId = friendId;
    }
    public void setConnection(int connection){
        this.connection = connection;
    }

}
