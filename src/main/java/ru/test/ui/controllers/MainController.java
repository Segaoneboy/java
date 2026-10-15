package ru.test.ui.controllers;

import javafx.fxml.FXML;
import ru.test.data.DataManager;
import ru.test.domain.Editable;
import ru.test.domain.datastructure.Graph;

public class MainController {

    @FXML private ToolbarController toolbarController;
    @FXML private TableController tableController;
    @FXML private GraphViewController graphViewController;
    @FXML private StatusController statusController;

    private final DataManager dataManager = new DataManager();
    private final Graph graph = new Graph(64);

    public MainController() {}

    @FXML
    public void initialize() {
        tableController.setItems(dataManager.getProfiles());

        graphViewController.init(graph, dataManager);

        toolbarController.init(dataManager, graph, tableController, statusController, graphViewController);

        tableController.setOnSelectionChanged(selected -> {
            boolean isEditable = (selected instanceof Editable);
            toolbarController.setEditButtonDisabled(!isEditable);

            graphViewController.setSelectedProfile(selected);
        });
    }
}