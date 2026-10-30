package ru.test.ui.controllers;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import ru.test.data.CsvSaver;
import ru.test.data.DataGeneratorTask;
import ru.test.data.DataManager;
import ru.test.data.serialization.BinaryGraphSerializer;
import ru.test.data.serialization.JsonGraphSerializer;
import ru.test.data.serialization.Serializer;
import ru.test.data.serialization.dto.GraphDto;
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
import java.util.List;

public class ToolbarController {

    @FXML private Button loadBtn;
    @FXML private Button saveBtn;
    @FXML private Button addProfileBtn;
    @FXML private Button editBtn;
    @FXML private TextField generateCountField;
    @FXML private Button generateBtn;
    @FXML private Button cancelBtn;

    private final Serializer<GraphDto> jsonSerializer = new JsonGraphSerializer();
    private final Serializer<GraphDto> binarySerializer = new BinaryGraphSerializer();

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
    private void onExportJson() {
        performExport(jsonSerializer);
    }

    @FXML
    private void onExportBinary() {
        performExport(binarySerializer);
    }

    @FXML
    private void onImportJson() {
        performImport(jsonSerializer);
    }

    @FXML
    private void onImportBinary() {
        performImport(binarySerializer);
    }

    private void performExport(Serializer<GraphDto> serializer) {
        Window window = getWindow();
        FileChooser fc = new FileChooser();
        fc.setTitle("Экспорт графа (" + serializer.getFormatName() + ")");
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(serializer.getFormatName(), "*." + serializer.getExtension())
        );

        File file = fc.showSaveDialog(window);
        if (file == null) return;

        Task<Void> exportTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                updateMessage("Сериализация данных...");
                GraphDto snapshot = graph.exportSnapshot(dataManager.getProfiles());
                serializer.save(snapshot, file);
                return null;
            }
        };

        this.activeTask = exportTask;
        statusController.bindTask(exportTask);
        cancelBtn.setDisable(false);

        exportTask.setOnSucceeded(e -> {
            statusController.unbindTask();
            cancelBtn.setDisable(true);
            statusController.setMessage("Экспорт успешно завершен.");

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Успех");
            alert.setHeaderText("Экспорт завершён");
            alert.setContentText("Данные сохранены в файл:\n" + file.getName());
            alert.showAndWait();
        });

        exportTask.setOnFailed(e -> {
            statusController.unbindTask();
            cancelBtn.setDisable(true);
            Throwable ex = exportTask.getException();
            String msg = ex != null ? ex.getMessage() : "Неизвестная ошибка записи";

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Ошибка экспорта");
            alert.setHeaderText("Не удалось экспортировать данные");
            alert.setContentText(msg);
            alert.showAndWait();
        });

        Thread th = new Thread(exportTask);
        th.setDaemon(true);
        th.start();
    }

    private void performImport(Serializer<GraphDto> serializer) {
        Window window = getWindow();
        FileChooser fc = new FileChooser();
        fc.setTitle("Импорт графа (" + serializer.getFormatName() + ")");
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(serializer.getFormatName(), "*." + serializer.getExtension())
        );

        File file = fc.showOpenDialog(window);
        if (file == null) return;

        editBtn.setDisable(true);

        Task<GraphDto> importTask = new Task<>() {
            @Override
            protected GraphDto call() throws Exception {
                updateMessage("Десериализация файла...");
                return serializer.load(file);
            }
        };

        this.activeTask = importTask;
        statusController.bindTask(importTask);
        cancelBtn.setDisable(false);

        importTask.setOnSucceeded(e -> {
            statusController.unbindTask();
            cancelBtn.setDisable(true);
            GraphDto loadedDto = importTask.getValue();

            graph.restoreFromSnapshot(loadedDto);
            dataManager.setAllProfiles(loadedDto.getProfiles() != null ? loadedDto.getProfiles() : List.of());

            tableController.refresh();
            graphViewController.redraw();
            statusController.setMessage("Импорт завершен.");

            int profilesCount = loadedDto.getProfiles() != null ? loadedDto.getProfiles().size() : 0;
            int edgesCount = loadedDto.getEdges() != null ? loadedDto.getEdges().size() : 0;

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Успех");
            alert.setHeaderText("Импорт завершен");
            alert.setContentText(String.format("Успешно восстановлено:\n- Профилей: %d\n- Связей дружбы: %d", profilesCount, edgesCount));
            alert.showAndWait();
        });

        importTask.setOnFailed(e -> {
            statusController.unbindTask();
            cancelBtn.setDisable(true);
            Throwable ex = importTask.getException();
            String msg = ex != null ? ex.getMessage() : "Неизвестная ошибка десериализации";

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Ошибка импорта");
            alert.setHeaderText("Сбой при чтении файла");
            alert.setContentText(msg);
            alert.showAndWait();
        });

        Thread th = new Thread(importTask);
        th.setDaemon(true);
        th.start();
    }

    private Window getWindow() {
        if (loadBtn != null && loadBtn.getScene() != null) {
            return loadBtn.getScene().getWindow();
        }
        return null;
    }

    @FXML
    private void onLoadCsv() {
        Window window = getWindow();
        FileChooser fc = new FileChooser();
        fc.setTitle("Выберите CSV файл");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = fc.showOpenDialog(window);

        if (file != null) {
            editBtn.setDisable(true);
            Task<List<Profile>> loadTask = new Task<>() {
                @Override
                protected List<Profile> call() throws Exception {
                    List<Profile> loaded = dataManager.parseFile(file);
                    graph.clear();
                    for (Profile p : loaded) {
                        graph.addVertex(p);
                    }
                    return loaded;
                }
            };

            this.activeTask = loadTask;
            statusController.bindTask(loadTask);
            cancelBtn.setDisable(false);

            loadTask.setOnSucceeded(e -> {
                statusController.unbindTask();
                cancelBtn.setDisable(true);
                List<Profile> loaded = loadTask.getValue();
                dataManager.setAllProfiles(loaded);

                tableController.refresh();
                graphViewController.redraw();
                statusController.setMessage("Загрузка завершена.");

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Успех");
                alert.setHeaderText("Загрузка завершена");
                alert.setContentText("Успешно загружено записей: " + loaded.size());
                alert.showAndWait();
            });

            loadTask.setOnFailed(e -> {
                statusController.unbindTask();
                cancelBtn.setDisable(true);
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
        Window window = getWindow();
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
            List<Profile> result = task.getValue();
            dataManager.addAllProfiles(result);

            finishGeneration();
            statusController.setMessage("Сгенерировано записей: " + result.size());

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Генерация");
            alert.setHeaderText("Успешная генерация");
            alert.setContentText("Сгенерировано профилей: " + result.size());
            alert.showAndWait();
        });

        task.setOnCancelled(e -> {
            List<Profile> result = task.getValue();
            if (result != null && !result.isEmpty()) {
                dataManager.addAllProfiles(result);
            }
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