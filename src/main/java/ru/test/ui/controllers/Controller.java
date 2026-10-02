package ru.test.ui.controllers;

import javafx.scene.control.Alert;
import javafx.concurrent.Task;
import javafx.scene.control.Button;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import ru.test.data.DataManager;
import ru.test.domain.Editable;
import ru.test.domain.Profile;
import ru.test.domain.exception.CsvParserException;
import ru.test.domain.exception.ErrorCode;
import ru.test.ui.components.ProfileEditDialog;
import ru.test.ui.components.ProfileTableView;

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
        tableView.setItems(dataManager.getProfiles());

        tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, selectedProfile) -> {
            updateEditButtonState();
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

    public void handleLoadFile() {
        Window window = editButton.getScene().getWindow();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Выберите CSV файл");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));

        File file = fileChooser.showOpenDialog(window);
        if (file != null) {
            asyncLoader(file);
        }
    }
    private void updateEditButtonState() {
        Profile selected = tableView.getSelectionModel().getSelectedItem();
        boolean isEditable = selected instanceof Editable;
        editButton.setDisable(!isEditable);
    }

    private void asyncLoader(File file){
        editButton.setDisable(true);

        Task<Void> loadTask = new Task<>(){
            @Override
            protected Void call() throws Exception{
                dataManager.loadFromFile(file);
                return null;
            }
        };

        loadTask.setOnSucceeded(event -> {
           tableView.refresh();
           updateEditButtonState();

           Alert alert = new Alert(
                   Alert.AlertType.INFORMATION,
                   "Успешно загружено записей: " + dataManager.getProfiles().size()
           );
           alert.setHeaderText("Загрузка завершена");
           alert.showAndWait();
        });
        loadTask.setOnFailed(event ->{
            Throwable exception = loadTask.getException();
            String contentText;

            if (exception instanceof CsvParserException csvEx){
                contentText = String.format("Код ошибки: %s\nСтрока: %d\n Причина: %s",
                    csvEx.getErrorCode(),
                    csvEx.getLineNumber(),
                    csvEx.getMessage());
            } else{
                String msg = (exception != null && exception.getMessage() != null)
                        ? exception.getMessage()
                        : "Неизвестная ошибка";
                contentText = String.format("Код ошибки: %s", ErrorCode.UNKNOWN_ERROR, msg);
            }

            Alert alert = new Alert(Alert.AlertType.ERROR, contentText );
            alert.setHeaderText("Ошибка загрузки CSV");
            alert.showAndWait();

            updateEditButtonState();
        });

        Thread loadThread = new Thread(loadTask);
        loadThread.setDaemon(true);
        loadThread.start();
    }
}