package ru.test.domain;

import java.util.ArrayList;
import java.util.List;

public class Profile {
    private int id;
    private String name;
    private String city;
    private int birthYear;
    private List<Connections> friendshipList = new ArrayList<>();

    public Profile(){}

    public Profile(int id, String name, String city, int birthYear, List<Connections> friendshipList){
        this.id = id;
        this.name = name;
        this.city = city;
        this.birthYear = birthYear;
        this.friendshipList = friendshipList;
    }
    public int getId(){
        return this.id;
    }
    public String getName(){
        return this.name;
    }
    public String getCity(){
        return this.city;
    }
    public int getBirthYear(){
        return this.birthYear;
    }
    public List<Connections> getFriendshipList(){
        return this.friendshipList;
    }

    public void setId(int id){
        this.id = id;
    }
    public void setName(String name){
        this.name = name;
    }
    public void setCity(String city){
        this.city = city;
    }
    public void setBirthYear(int birthYear){
        this.birthYear = birthYear;
    }
    public void setFriendshipList(List<Connections> friendshipList){
        this.friendshipList = friendshipList;
    }


}

