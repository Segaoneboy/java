package ru.test.ui.controllers;

import javafx.scene.control.Alert;
import javafx.concurrent.Task;
import javafx.scene.control.Button;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import ru.test.data.CsvSaver;
import ru.test.data.DataManager;
import ru.test.domain.AdminProfile;
import ru.test.domain.Editable;
import ru.test.domain.Profile;
import ru.test.domain.exception.CsvParserException;
import ru.test.domain.exception.ErrorCode;
import ru.test.ui.components.ProfileEditDialog;
import ru.test.ui.components.ProfileTableView;

import java.io.File;
import java.util.ArrayList;

public class Controller {

    private final DataManager dataManager;
    private final ProfileTableView tableView;
    private final Button editButton;
    private final Button loadButton;
    private final Button saveButton;
    private final Button addProfileButton;

    public Controller(ProfileTableView tableView, Button editButton,Button loadButton, Button saveButton, Button addProfileButton, DataManager dataManager) {
        this.tableView = tableView;
        this.editButton = editButton;
        this.dataManager = dataManager;
        this.loadButton = loadButton;
        this.saveButton = saveButton;
        this.addProfileButton = addProfileButton;

        initListeners();
    }

    private void initListeners() {
        tableView.setItems(dataManager.getProfiles());

        tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, selectedProfile) -> {
            updateEditButtonState();
        });
        loadButton.setOnAction(e -> handleLoadFile());
        saveButton.setOnAction(e -> handleSaveFile());
        addProfileButton.setOnAction(e -> handleAddProfile());

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
    public void handleSaveFile(){
        Window window = editButton.getScene().getWindow();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Сохранить CSV");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Csv files", "*.csv"));

        File file = fileChooser.showSaveDialog(window);
        if(file != null){
            try{
                CsvSaver saver = new CsvSaver();
                saver.save(file, dataManager.getProfiles());

                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Файл успешно сохранен");
                alert.setHeaderText("Успех");
                alert.showAndWait();
            } catch (CsvParserException e){
                Alert alert = new Alert(Alert.AlertType.ERROR, "Ошибка сохранения:" + e.getMessage());
                alert.showAndWait();
            }
        }
    }

    public void handleAddProfile(){
        AdminProfile newAdmin = new AdminProfile(0,"","",2000, new ArrayList<>(), new ArrayList<>());

        boolean created = ProfileEditDialog.showEditDialog(newAdmin);
        if(created){
            dataManager.addProfile(newAdmin);
            tableView.refresh();
        }
    }
}