package ru.test.ui;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.text.Text;

public class App extends Application {

    @Override
    public void start(Stage stage){
        Text text = new Text("test");

        text.setLayoutX(80);
        text.setLayoutY(80);

        Group group = new Group(text);

        Scene scene = new Scene(group);
        stage.setScene(scene);
        stage.setTitle("Test");
        stage.setWidth(500);
        stage.setHeight(500);
        stage.show();
    }
}
