package ru.test.data;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import ru.test.domain.Profile;
import ru.test.domain.exception.CsvParserException;

import java.io.File;
import java.util.List;

public class DataManager {
    private final ObservableList<Profile> profiles = FXCollections.observableArrayList();
    private final CsvLoader csvLoader = new CsvLoader();

    public void loadFromFile(File file) throws CsvParserException {
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

    public int generateId(){
        return profiles.stream()
                .mapToInt(Profile::getId)
                .max()
                .orElse(0)+1;
    }
}
