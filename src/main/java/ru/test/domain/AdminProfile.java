package ru.test.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AdminProfile extends Profile implements Editable {
    private List<Community> groups = new ArrayList<Community>();

    public AdminProfile(){
        super();
    }
    public AdminProfile(UUID id, String name, String city, int birthYear, List<Connections> friendshipList, List<Community> groups){
        super(id, name, city, birthYear, friendshipList);
        this.groups = groups;
    }

    public List<Community> getGroups(){
        return groups;
    }
    public void setGroups(List<Community> groups){
        this.groups = groups;
    }

}
