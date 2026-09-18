package ru.test.domain;

import java.util.List;

public class DeletedProfile extends Profile {
    public DeletedProfile(){
        super();
    }
    public DeletedProfile(int id, String name, String city, int birthYear, List<Connections> friendshipList){
        super(id, name, city, birthYear, friendshipList);

    }
}
