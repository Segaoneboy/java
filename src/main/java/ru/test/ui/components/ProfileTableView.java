package ru.test.ui.components;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import ru.test.domain.AdminProfile;
import ru.test.domain.DeletedProfile;
import ru.test.domain.Profile;

public class ProfileTableView extends TableView<Profile> {

    public ProfileTableView() {
        TableColumn<Profile, Number> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(cell -> new SimpleIntegerProperty(cell.getValue().getId()));

        TableColumn<Profile, String> nameCol = new TableColumn<>("Имя");
        nameCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getName()));

        TableColumn<Profile, String> cityCol = new TableColumn<>("Город");
        cityCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getCity()));

        TableColumn<Profile, Number> birthYearCol = new TableColumn<>("Год рождения");
        birthYearCol.setCellValueFactory(cell -> new SimpleIntegerProperty(cell.getValue().getBirthYear()));

        TableColumn<Profile, String> typeCol = new TableColumn<>("Тип");
        typeCol.setCellValueFactory(cell -> {
            Profile p = cell.getValue();
            if (p instanceof AdminProfile) return new SimpleStringProperty("Админ");
            if (p instanceof DeletedProfile) return new SimpleStringProperty("Удален");
            return new SimpleStringProperty("Пользователь");
        });

        TableColumn<Profile, String> groupsCol = new TableColumn<>("Группы");
        groupsCol.setCellValueFactory(cell -> {
            Profile p = cell.getValue();
            if (p instanceof AdminProfile admin) {
                if (admin.getGroups() != null && !admin.getGroups().isEmpty()) {
                    return new SimpleStringProperty(String.join(", ", admin.getGroups()));
                }
            }
            return new SimpleStringProperty("-");
        });

        getColumns().addAll(idCol, nameCol, cityCol, birthYearCol, typeCol, groupsCol);
        setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }
}