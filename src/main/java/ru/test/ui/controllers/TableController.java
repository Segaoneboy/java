package ru.test.ui.controllers;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import ru.test.domain.AdminProfile;
import ru.test.domain.DeletedProfile;
import ru.test.domain.Profile;
import ru.test.domain.UserProfile;

import java.util.function.Consumer;

public class TableController {

    @FXML private TableView<Profile> tableView;
    @FXML private TableColumn<Profile, Number> idCol;
    @FXML private TableColumn<Profile, String> nameCol;
    @FXML private TableColumn<Profile, String> cityCol;
    @FXML private TableColumn<Profile, Number> birthYearCol;
    @FXML private TableColumn<Profile, String> typeCol;
    @FXML private TableColumn<Profile, String> groupsCol;

    private Consumer<Profile> selectionListener;

    @FXML
    public void initialize() {
        idCol.setCellValueFactory(cell -> new SimpleIntegerProperty(cell.getValue().getId()));
        nameCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getName()));
        cityCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getCity()));
        birthYearCol.setCellValueFactory(cell -> new SimpleIntegerProperty(cell.getValue().getBirthYear()));

        typeCol.setCellValueFactory(cell -> {
            Profile p = cell.getValue();
            if (p instanceof AdminProfile) return new SimpleStringProperty("Админ");
            if (p instanceof DeletedProfile) return new SimpleStringProperty("Удален");
            if (p instanceof UserProfile) return new SimpleStringProperty("Пользователь");
            return new SimpleStringProperty("Профиль");
        });

        groupsCol.setCellValueFactory(cell -> {
            Profile p = cell.getValue();
            if (p instanceof AdminProfile admin && admin.getGroups() != null && !admin.getGroups().isEmpty()) {
                return new SimpleStringProperty(String.join(", ", admin.getGroups()));
            }
            return new SimpleStringProperty("-");
        });

        tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (selectionListener != null) {
                selectionListener.accept(newVal);
            }
        });
    }

    public void setItems(ObservableList<Profile> items) {
        tableView.setItems(items);
    }

    public void setOnSelectionChanged(Consumer<Profile> listener) {
        this.selectionListener = listener;
    }

    public Profile getSelectedItem() {
        return tableView.getSelectionModel().getSelectedItem();
    }

    public void refresh() {
        tableView.refresh();
    }
}