package ru.test.domain;

import java.util.List;
import java.util.UUID;

public class DeletedProfile extends Profile {
    public DeletedProfile(){
        super();
    }
    public DeletedProfile(UUID id, String name, String city, int birthYear, List<Connections> friendshipList){
        super(id, name, city, birthYear, friendshipList);

    }
}
