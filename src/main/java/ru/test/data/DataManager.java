package ru.test.data;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import ru.test.domain.Profile;

import java.io.File;
import java.util.List;

public class DataManager {
    private final ObservableList<Profile> profiles = FXCollections.observableArrayList();
    private final CsvLoader csvLoader = new CsvLoader();

    public void loadFromFile(File file){
        List<Profile> loaded = csvLoader.load(file);

        profiles.clear();
        profiles.addAll(loaded);
    }
    public ObservableList<Profile> getProfiles (){
        return profiles;
    }
    public void addProfile(Profile profile){
        if(profile != null){
            profiles.add(profile);
        }
    }
    public void removeProfile(Profile profile){
        profiles.remove(profile);
    }
}
