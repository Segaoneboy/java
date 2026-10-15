package ru.test.data;

import javafx.concurrent.Task;
import ru.test.data.DataManager;
import ru.test.domain.*;
import ru.test.domain.datastructure.Connections;
import ru.test.domain.datastructure.Graph;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DataGeneratorTask extends Task<Void> {

    private final Graph graph;
    private final DataManager dataManager;
    private final int totalCount;

    private static final String[] NAMES = {
            "Александр", "Дмитрий", "Максим", "Иван", "Артем",
            "Анна", "Мария", "Елена", "Ольга", "Татьяна"
    };

    private static final String[] CITIES = {
            "Москва", "Санкт-Петербург", "Новосибирск", "Екатеринбург",
            "Казань", "Нижний Новгород", "Челябинск", "Самара"
    };

    private static final String[] GROUPS = {
            "IT-Club", "Спорт", "Музыка", "Кино", "Наука", "Книги"
    };

    public DataGeneratorTask(Graph graph, DataManager dataManager, int totalCount) {
        this.graph = graph;
        this.dataManager = dataManager;
        this.totalCount = totalCount;
    }

    @Override
    protected Void call() throws Exception {
        Random random = new Random();
        updateMessage("Инициализация генерации...");
        updateProgress(0, totalCount);

        List<Profile> generatedProfiles = new ArrayList<>();

        for (int i = 1; i <= totalCount; i++) {
            if (isCancelled()) {
                updateMessage("Генерация отменена пользователем.");
                return null;
            }

            int id = dataManager.generateId();
            String name = NAMES[random.nextInt(NAMES.length)] + "_" + id;
            String city = CITIES[random.nextInt(CITIES.length)];
            int birthYear = 1975 + random.nextInt(33); // 1975–2007

            Profile profile;
            int typeRoll = random.nextInt(10);

            if (typeRoll == 0) {
                profile = new DeletedProfile(id, name, city, birthYear, new Connections[0]);
            } else if (typeRoll == 1) {
                List<String> adminGroups = new ArrayList<>();
                adminGroups.add(GROUPS[random.nextInt(GROUPS.length)]);
                profile = new AdminProfile(id, name, city, birthYear, new Connections[0], adminGroups);
            } else {
                profile = new UserProfile(id, name, city, birthYear, new Connections[0]);
            }

            graph.addVertex(profile);
            generatedProfiles.add(profile);

            if (i > 1 && random.nextDouble() < 0.6) {
                int targetIndex = random.nextInt(generatedProfiles.size() - 1);
                int friendId = generatedProfiles.get(targetIndex).getId();

                int strength = 1 + random.nextInt(10);
                graph.addEdge(id, friendId, strength);
            }

            if (i % 50 == 0 || i == totalCount) {
                updateProgress(i, totalCount);
                updateMessage(String.format("Сгенерировано %d из %d записей", i, totalCount));
            }

            if (totalCount <= 2000) {
                Thread.sleep(15);
            }
        }

        dataManager.getProfiles().addAll(generatedProfiles);

        updateMessage("Генерация успешно завершена!");
        updateProgress(totalCount, totalCount);
        return null;
    }
}