package ru.test.ui.controllers;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

public class StatusController {

    @FXML private Label statusLabel;
    @FXML private ProgressBar progressBar;

    public void bindTask(Task<?> task) {
        progressBar.progressProperty().bind(task.progressProperty());
        statusLabel.textProperty().bind(task.messageProperty());
    }

    public void unbindTask() {
        progressBar.progressProperty().unbind();
        statusLabel.textProperty().unbind();
    }

    public void setMessage(String message) {
        statusLabel.setText(message);
    }

    public void setProgress(double progress) {
        progressBar.setProgress(progress);
    }
}