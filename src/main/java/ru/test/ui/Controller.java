package ru.test.ui;

import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import ru.test.data.DataManager;
import ru.test.domain.Editable;
import ru.test.domain.Profile;

import java.io.File;

public class Controller {

    private final DataManager dataManager;
    private final ProfileTableView tableView;
    private final Button editButton;

    public Controller(ProfileTableView tableView, Button editButton, DataManager dataManager) {
        this.tableView = tableView;
        this.editButton = editButton;
        this.dataManager = dataManager;

        initListeners();
    }

    private void initListeners() {
        // Подключаем данные
        tableView.setItems(dataManager.getProfiles());

        // Четкая проверка Editable при клике на строку
        tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, selectedProfile) -> {
            if (selectedProfile == null) {
                editButton.setDisable(true);
            } else {
                // Включаем кнопку ТОЛЬКО если объект реализует Editable
                boolean isEditable = selectedProfile instanceof Editable;
                editButton.setDisable(!isEditable);
            }
        });

        editButton.setOnAction(e -> {
            Profile selected = tableView.getSelectionModel().getSelectedItem();
            if (selected instanceof Editable editable) {
                boolean saved = ProfileEditDialog.showEditDialog(editable);
                if (saved) {
                    tableView.refresh();
                }
            } else if(selected != null){
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Этот профиль нельзя редактировать");
                alert.showAndWait();
            }
        });
    }

    public void handleLoadFile(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Выберите CSV файл");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));

        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            dataManager.loadFromFile(file);
            System.out.println("Загружено из CSV: " + dataManager.getProfiles().size() + " строк.");
        }
    }
}