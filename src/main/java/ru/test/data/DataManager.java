package ru.test.data;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import ru.test.domain.Profile;
import ru.test.domain.exception.CsvParserException;

import java.io.File;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class DataManager {

    private final ObservableList<Profile> profiles = FXCollections.observableArrayList();
    private final CsvLoader csvLoader = new CsvLoader();
    private final AtomicInteger idCounter = new AtomicInteger(0);


    public List<Profile> parseFile(File file) throws CsvParserException {
        return csvLoader.load(file);
    }


    public void setAllProfiles(List<Profile> newProfiles) {
        profiles.clear();
        if (newProfiles != null && !newProfiles.isEmpty()) {
            profiles.addAll(newProfiles);
            int max = 0;
            for (Profile p : newProfiles) {
                if (p.getId() > max) {
                    max = p.getId();
                }
            }
            idCounter.set(max);
        }
    }


    public void addAllProfiles(List<Profile> newProfiles) {
        if (newProfiles != null && !newProfiles.isEmpty()) {
            profiles.addAll(newProfiles);
        }
    }

    public ObservableList<Profile> getProfiles() {
        return profiles;
    }

    public void addProfile(Profile profile) {
        if (profile != null) {
            idCounter.updateAndGet(curr -> Math.max(curr, profile.getId()));
            profiles.add(profile);
        }
    }

    public int generateId() {
        return idCounter.incrementAndGet();
    }
}