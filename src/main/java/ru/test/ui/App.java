package ru.test.ui;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import ru.test.data.DataManager;

public class App extends Application {

    @Override
    public void start(Stage stage) {
        ProfileTableView tableView = new ProfileTableView();

        Button loadButton = new Button("Загрузить CSV");
        Button editButton = new Button("Редактировать");
        editButton.setDisable(true);

        DataManager dataManager = new DataManager();
        Controller controller = new Controller(tableView, editButton, dataManager);

        loadButton.setOnAction(e -> controller.handleLoadFile(stage));

        HBox topBar = new HBox(10, loadButton, editButton);
        topBar.setPadding(new Insets(10));

        BorderPane root = new BorderPane();
        root.setTop(topBar);
        root.setCenter(tableView);

        Scene scene = new Scene(root, 750, 450);
        stage.setScene(scene);
        stage.setTitle("Социальная сеть");
        stage.show();
    }

}