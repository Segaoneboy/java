package ru.test.ui.controllers;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import ru.test.data.DataGeneratorTask;
import ru.test.data.CsvSaver;
import ru.test.data.DataManager;
import ru.test.domain.AdminProfile;
import ru.test.domain.Editable;
import ru.test.domain.Profile;
import ru.test.domain.UserProfile;
import ru.test.domain.datastructure.Connections;
import ru.test.domain.datastructure.Graph;
import ru.test.domain.exception.CsvParserException;
import ru.test.ui.components.ProfileEditDialog;

import java.io.File;
import java.util.ArrayList;

public class ToolbarController {

    @FXML private Button loadBtn;
    @FXML private Button saveBtn;
    @FXML private Button addProfileBtn;
    @FXML private Button editBtn;
    @FXML private TextField generateCountField;
    @FXML private Button generateBtn;
    @FXML private Button cancelBtn;

    private DataManager dataManager;
    private Graph graph;
    private TableController tableController;
    private StatusController statusController;
    private GraphViewController graphViewController;
    private Task<?> activeTask;

    public void init(DataManager dataManager,
                     Graph graph,
                     TableController tableController,
                     StatusController statusController,
                     GraphViewController graphViewController) {
        this.dataManager = dataManager;
        this.graph = graph;
        this.tableController = tableController;
        this.statusController = statusController;
        this.graphViewController = graphViewController;
    }

    public void setEditButtonDisabled(boolean disabled) {
        editBtn.setDisable(disabled);
    }

    @FXML
    private void onLoadCsv() {
        Window window = loadBtn.getScene().getWindow();
        FileChooser fc = new FileChooser();
        fc.setTitle("Выберите CSV файл");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = fc.showOpenDialog(window);

        if (file != null) {
            editBtn.setDisable(true);
            Task<Void> loadTask = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    dataManager.loadFromFile(file);
                    for (Profile p : dataManager.getProfiles()) {
                        graph.addVertex(p);
                    }
                    return null;
                }
            };

            statusController.bindTask(loadTask);

            loadTask.setOnSucceeded(e -> {
                statusController.unbindTask();
                tableController.refresh();
                graphViewController.redraw();
                statusController.setMessage("Загрузка завершена.");

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Успех");
                alert.setHeaderText("Загрузка завершена");
                alert.setContentText("Успешно загружено записей: " + dataManager.getProfiles().size());
                alert.showAndWait();
            });

            loadTask.setOnFailed(e -> {
                statusController.unbindTask();
                Throwable ex = loadTask.getException();
                String msg = ex instanceof CsvParserException csvEx
                        ? String.format("Код: %s, Строка: %d, Причина: %s", csvEx.getErrorCode(), csvEx.getLineNumber(), csvEx.getMessage())
                        : (ex != null ? ex.getMessage() : "Неизвестная ошибка");

                Alert alert = new Alert(Alert.AlertType.ERROR, msg);
                alert.setHeaderText("Ошибка чтения CSV");
                alert.showAndWait();
            });

            Thread th = new Thread(loadTask);
            th.setDaemon(true);
            th.start();
        }
    }

    @FXML
    private void onSaveCsv() {
        Window window = saveBtn.getScene().getWindow();
        FileChooser fc = new FileChooser();
        fc.setTitle("Сохранить CSV");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = fc.showSaveDialog(window);

        if (file != null) {
            try {
                new CsvSaver().save(file, dataManager.getProfiles());
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Файл успешно сохранен!");
                alert.setHeaderText("Успех");
                alert.showAndWait();
            } catch (CsvParserException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Ошибка сохранения: " + e.getMessage());
                alert.showAndWait();
            }
        }
    }

    @FXML
    private void onAddProfile() {
        ChoiceDialog<String> typeDialog = new ChoiceDialog<>("Обычный пользователь",
                "Обычный пользователь", "Администратор");
        typeDialog.setTitle("Создание профиля");
        typeDialog.setHeaderText("Выберите тип нового профиля:");
        typeDialog.setContentText("Тип:");

        typeDialog.showAndWait().ifPresent(chosenType -> {
            int newId = dataManager.generateId();
            Profile newProfile;

            if ("Администратор".equals(chosenType)) {
                newProfile = new AdminProfile(newId, "", "", 2000, new Connections[0], new ArrayList<>());
            } else {
                newProfile = new UserProfile(newId, "", "", 2000, new Connections[0]);
            }

            if (ProfileEditDialog.showEditDialog((Editable) newProfile)) {
                dataManager.addProfile(newProfile);
                graph.addVertex(newProfile);
                tableController.refresh();
                graphViewController.redraw();
            }
        });
    }

    @FXML
    private void onEditProfile() {
        Profile selected = tableController.getSelectedItem();
        if (selected instanceof Editable editable) {
            if (ProfileEditDialog.showEditDialog(editable)) {
                tableController.refresh();
                graphViewController.redraw();
            }
        }
    }

    @FXML
    private void onGenerate() {
        int count;
        try {
            count = Integer.parseInt(generateCountField.getText().trim());
            if (count <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Введите корректное число!");
            alert.showAndWait();
            return;
        }

        DataGeneratorTask task = new DataGeneratorTask(graph, dataManager, count);
        this.activeTask = task;

        statusController.bindTask(task);
        generateBtn.setDisable(true);
        cancelBtn.setDisable(false);

        task.setOnSucceeded(e -> {
            finishGeneration();
            statusController.setMessage("Сгенерировано записей: " + count);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Генерация");
            alert.setHeaderText("Успешная генерация");
            alert.setContentText("Сгенерировано профилей: " + count);
            alert.showAndWait();
        });

        task.setOnCancelled(e -> {
            finishGeneration();
            statusController.setMessage("Генерация отменена.");
        });

        task.setOnFailed(e -> {
            finishGeneration();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Сбой при генерации данных.");
            alert.showAndWait();
        });

        Thread th = new Thread(task);
        th.setDaemon(true);
        th.start();
    }

    private void finishGeneration() {
        statusController.unbindTask();
        generateBtn.setDisable(false);
        cancelBtn.setDisable(true);
        tableController.refresh();
        graphViewController.redraw();
    }

    @FXML
    private void onCancel() {
        if (activeTask != null && activeTask.isRunning()) {
            activeTask.cancel();
        }
    }
}