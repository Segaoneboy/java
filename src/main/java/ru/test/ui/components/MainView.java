package ru.test.ui.components;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import ru.test.data.DataManager;
import ru.test.ui.controllers.Controller;


public class MainView extends BorderPane{
    public MainView(DataManager dataManager){
        ProfileTableView tableView = new ProfileTableView();

        Button loadBtn = new Button("Загрузить CSV");
        Button editBtn = new Button("Редактировать");
        Button saveBtn = new Button("Сохранить CSV");
        Button addProfileBtn = new Button("Создать профиль");
        editBtn.setDisable(true);

        Controller controller = new Controller(tableView, editBtn,loadBtn,saveBtn,addProfileBtn, dataManager);

        loadBtn.setOnAction(e -> controller.handleLoadFile());

        HBox topBar = new HBox(10, loadBtn, editBtn, addProfileBtn, saveBtn);
        topBar.setPadding(new Insets(10));

        this.setTop(topBar);
        this.setCenter(tableView);

    }

}

