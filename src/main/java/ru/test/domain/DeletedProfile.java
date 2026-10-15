package ru.test.domain;

import ru.test.domain.datastructure.Connections;

public class DeletedProfile extends Profile {
    public DeletedProfile() {
        super();
    }

    public DeletedProfile(int id, String name, String city, int birthYear, Connections[] friendshipList) {
        super(id, name, city, birthYear, friendshipList);
    }
}