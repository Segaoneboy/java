package ru.test.ui.components;

import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import ru.test.domain.Editable;
import ru.test.domain.exception.CsvParserException;

import java.util.HashMap;
import java.util.Map;

public class ProfileEditDialog extends Dialog<Boolean> {

    public ProfileEditDialog(Editable editable) {
        setTitle("Редактирование");

        ButtonType saveButtonType = new ButtonType("Сохранить", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

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

        Button saveButton  = (Button) getDialogPane().lookupButton(saveButtonType);

        saveButton.addEventFilter(ActionEvent.ACTION, event ->{
            Map<String, String> updateData = new HashMap<>();
            for(var entry : textFields.entrySet()){
                updateData.put(entry.getKey(),entry.getValue().getText());
            }
            try{
                editable.update(updateData);
            }catch (CsvParserException e){
                event.consume();

                showErrorAlert(e);
            }
        });
        setResultConverter(button -> button == saveButtonType);
    }
    private void showErrorAlert(CsvParserException e){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка ввода");
        alert.setHeaderText(e.getErrorCode().getDefaultMessage());
        alert.setContentText(e.getMessage());
        alert.showAndWait();
    }

    public static boolean showEditDialog(Editable editable) {
        return new ProfileEditDialog(editable).showAndWait().orElse(false);
    }
}