package ru.test.data;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import ru.test.domain.Profile;
import ru.test.domain.exception.CsvParserException;

import java.util.concurrent.atomic.AtomicInteger;
import java.io.File;
import java.util.List;

public class DataManager {
    private final ObservableList<Profile> profiles = FXCollections.observableArrayList();
    private final CsvLoader csvLoader = new CsvLoader();
    private final AtomicInteger idCounter = new AtomicInteger(0);

    public void loadFromFile(File file) throws CsvParserException {
        List<Profile> loaded = csvLoader.load(file);

        profiles.clear();
        profiles.addAll(loaded);

        int max = 0;
        for (Profile p: loaded){
            if(p.getId() > max){
                max = p.getId();
            }
        }
        idCounter.set(max);
    }
    public ObservableList<Profile> getProfiles (){
        return profiles;
    }
    public void addProfile(Profile profile){
        if(profile != null){
            idCounter.updateAndGet(curr-> Math.max(curr,profile.getId()));
            profiles.add(profile);
        }
    }

    public int generateId(){
        return idCounter.incrementAndGet();
    }
}
