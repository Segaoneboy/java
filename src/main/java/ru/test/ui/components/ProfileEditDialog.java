package ru.test.ui.components;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import ru.test.domain.Editable;

import java.util.HashMap;
import java.util.Map;

public class ProfileEditDialog extends Dialog<Boolean> {

    public ProfileEditDialog(Editable editable) {
        setTitle("Редактирование профиля");

        ButtonType saveButton = new ButtonType("Сохранить", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(saveButton, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        Map<String, String> initialData = editable.getEditableFields();
        Map<String, TextField> textFields = new HashMap<>();

        int row = 0;
        for (Map.Entry<String, String> entry : initialData.entrySet()) {
            Label label = new Label(entry.getKey() + ":");
            TextField field = new TextField(entry.getValue());

            grid.add(label, 0, row);
            grid.add(field, 1, row);

            textFields.put(entry.getKey(), field);
            row++;
        }

        getDialogPane().setContent(grid);

        setResultConverter(button -> {
            if (button == saveButton) {
                Map<String, String> updatedData = new HashMap<>();
                for (var entry : textFields.entrySet()) {
                    updatedData.put(entry.getKey(), entry.getValue().getText());
                }
                editable.update(updatedData);
                return true;
            }
            return false;
        });
    }

    public static boolean showEditDialog(Editable editable) {
        return new ProfileEditDialog(editable).showAndWait().orElse(false);
    }
}