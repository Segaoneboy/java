package ru.test.ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class App extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ru/test/view/main-view.fxml"));
        Parent root = loader.load();

        stage.setTitle("Социальная сеть");
        stage.setScene(new Scene(root, 1050,650));
        stage.show();
    }

}