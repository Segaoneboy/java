package ru.test.ui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ru.test.data.DataManager;
import ru.test.ui.components.MainView;

public class App extends Application {

    @Override
    public void start(Stage stage) {
        DataManager dataManager = new DataManager();
        MainView mainView = new MainView(dataManager);

        Scene scene = new Scene(mainView, 750, 450);
        stage.setScene(scene);
        stage.setTitle("Социальная сеть");
        stage.show();
    }

}