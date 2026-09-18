package ru.test.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Community{
    private UUID id;
    private String name;
    private List<Connections> followersList = new ArrayList<>();
    private Profile admin;

    public Community(){
    }
    public Community(UUID id, String name, List<Connections> followersList, Profile admin){
        this.id = id;
        this.name = name;
        this.followersList = followersList;
        this.admin = admin;
    }
    public UUID getId(){
        return this.id;
    }
    public String getName(){
        return this.name;
    }
    public List<Connections> getFollowersList(){
        return this.followersList;
    }
    public Profile getAdmin(){
        return this.admin;
    }

    public void setId(UUID id){
        this.id = id;
    }
    public void setName(String name){
        this.name = name;
    }
    public void setFollowersList(List<Connections> followersList){
        this.followersList = followersList;
    }
    public void setAdmin(Profile admin){
        this.admin = admin;
    }
}
