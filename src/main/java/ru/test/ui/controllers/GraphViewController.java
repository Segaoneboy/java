package ru.test.ui.controllers;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import ru.test.data.DataManager;
import ru.test.domain.AdminProfile;
import ru.test.domain.DeletedProfile;
import ru.test.domain.Profile;
import ru.test.domain.datastructure.BFSResult;
import ru.test.domain.datastructure.Connections;
import ru.test.domain.datastructure.Graph;
import ru.test.domain.datastructure.Vertex;

public class GraphViewController {

    @FXML private TextField startIdField;
    @FXML private TextField targetIdField;
    @FXML private Pane canvasContainer;
    @FXML private Canvas graphCanvas;
    @FXML private Label selectedUserInfoLabel;

    private Graph graph;
    private DataManager dataManager;
    private int[] highlightedPath = new int[0];
    private int[] visitedNodes = new int[0];
    private Profile selectedProfile;

    public void init(Graph graph, DataManager dataManager) {
        this.graph = graph;
        this.dataManager = dataManager;
        redraw();
    }

    @FXML
    public void initialize() {
        canvasContainer.widthProperty().addListener((obs, oldV, newV) -> {
            graphCanvas.setWidth(newV.doubleValue());
            redraw();
        });
        canvasContainer.heightProperty().addListener((obs, oldV, newV) -> {
            graphCanvas.setHeight(newV.doubleValue());
            redraw();
        });
    }

    public void setSelectedProfile(Profile profile) {
        this.selectedProfile = profile;
        updateSelectedUserInfo();
        redraw();
    }

    private void updateSelectedUserInfo() {
        if (selectedProfile == null || graph == null) {
            selectedUserInfoLabel.setText("Выберите профиль в таблице, чтобы увидеть его связи.");
            return;
        }

        Vertex v = graph.findVertexById(selectedProfile.getId());
        if (v == null || v.getConnectionCount() == 0) {
            selectedUserInfoLabel.setText(String.format("Профиль: [%d] %s — друзей нет",
                    selectedProfile.getId(), selectedProfile.getName()));
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Профиль: [%d] %s | Друзей (%d): ",
                selectedProfile.getId(), selectedProfile.getName(), v.getConnectionCount()));

        Connections[] conns = v.getConnections();
        for (int i = 0; i < v.getConnectionCount(); i++) {
            sb.append(String.format("ID %d [сила: %d]", conns[i].getFriendId(), conns[i].getConnection()));
            if (i < v.getConnectionCount() - 1) sb.append(", ");
        }
        selectedUserInfoLabel.setText(sb.toString());
    }

    @FXML
    private void onAddFriendshipDialog() {
        if (graph == null || graph.getVertexCount() < 2) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Для добавления дружбы в графе должно быть минимум 2 профиля!");
            alert.showAndWait();
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Добавить связь дружбы");
        dialog.setHeaderText("Укажите ID двух пользователей и силу связи");

        ButtonType okBtn = new ButtonType("Добавить", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okBtn, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(15));

        TextField id1Field = new TextField(selectedProfile != null ? String.valueOf(selectedProfile.getId()) : "");
        TextField id2Field = new TextField();
        TextField strengthField = new TextField("5");

        grid.add(new Label("ID пользователя 1:"), 0, 0);
        grid.add(id1Field, 1, 0);
        grid.add(new Label("ID пользователя 2:"), 0, 1);
        grid.add(id2Field, 1, 1);
        grid.add(new Label("Сила связи (1-10):"), 0, 2);
        grid.add(strengthField, 1, 2);

        dialog.getDialogPane().setContent(grid);

        dialog.showAndWait().ifPresent(response -> {
            if (response == okBtn) {
                try {
                    int id1 = Integer.parseInt(id1Field.getText().trim());
                    int id2 = Integer.parseInt(id2Field.getText().trim());
                    int strength = Integer.parseInt(strengthField.getText().trim());

                    if (id1 == id2) {
                        new Alert(Alert.AlertType.ERROR, "Нельзя подружить профиль с самим собой!").showAndWait();
                        return;
                    }

                    boolean added = graph.addEdge(id1, id2, strength);
                    if (added) {
                        updateSelectedUserInfo();
                        redraw();
                        Alert alert = new Alert(Alert.AlertType.INFORMATION, "Связь дружбы успешно добавлена!");
                        alert.showAndWait();
                    } else {
                        new Alert(Alert.AlertType.ERROR, "Не удалось добавить связь. Проверьте существование обоих ID.").showAndWait();
                    }
                } catch (NumberFormatException e) {
                    new Alert(Alert.AlertType.ERROR, "ID и сила связи должны быть целыми числами!").showAndWait();
                }
            }
        });
    }

    @FXML
    private void onFindShortestPath() {
        if (graph == null) return;
        try {
            int startId = Integer.parseInt(startIdField.getText().trim());
            int targetId = Integer.parseInt(targetIdField.getText().trim());

            BFSResult result = graph.findShortestPath(startId, targetId);
            this.visitedNodes = result.getVisitedOrder();

            if (result.isFound()) {
                this.highlightedPath = result.getPath();
                redraw();
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Цепочка найдена");
                alert.setHeaderText("Кратчайший путь найден!");
                alert.setContentText("Длина цепочки рукопожатий: " + (result.getPath().length - 1));
                alert.showAndWait();
            } else {
                this.highlightedPath = new int[0];
                redraw();
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Маршрут");
                alert.setHeaderText("Связь отсутствует");
                alert.setContentText("Путь между указанными профилями не найден.");
                alert.showAndWait();
            }
        } catch (NumberFormatException e) {
            new Alert(Alert.AlertType.ERROR, "Введите числовые ID начала и конца пути!").showAndWait();
        }
    }

    @FXML
    private void onResetHighlight() {
        this.highlightedPath = new int[0];
        this.visitedNodes = new int[0];
        redraw();
    }

    public void redraw() {
        if (graphCanvas == null) return;
        GraphicsContext gc = graphCanvas.getGraphicsContext2D();
        double w = graphCanvas.getWidth();
        double h = graphCanvas.getHeight();
        gc.clearRect(0, 0, w, h);

        if (graph == null || graph.getVertexCount() == 0) {
            gc.setFill(Color.GRAY);
            gc.fillText("Граф пуст. Сгенерируйте данные или добавьте профили.", 20, 30);
            return;
        }

        boolean hasPath = highlightedPath != null && highlightedPath.length > 0;
        if (selectedProfile == null && !hasPath) {
            gc.setFill(Color.GRAY);
            gc.fillText("Выберите профиль в таблице слева для просмотра его связей.", 20, 40);
            return;
        }

        int[] subGraphIds = collectSubGraphIds(hasPath);
        int n = subGraphIds.length;
        if (n == 0) return;

        int rootId = hasPath ? highlightedPath[0] : selectedProfile.getId();
        int[] levels = new int[n];
        for (int i = 0; i < n; i++) levels[i] = -1;

        int rootIdx = findIndex(subGraphIds, rootId, n);
        if (rootIdx != -1) levels[rootIdx] = 0;

        int maxLevel = 0;
        boolean expanded = true;
        while (expanded) {
            expanded = false;
            for (int i = 0; i < n; i++) {
                if (levels[i] != -1) {
                    Vertex v = graph.findVertexById(subGraphIds[i]);
                    if (v != null) {
                        Connections[] conns = v.getConnections();
                        for (int k = 0; k < v.getConnectionCount(); k++) {
                            int targetIdx = findIndex(subGraphIds, conns[k].getFriendId(), n);
                            if (targetIdx != -1 && levels[targetIdx] == -1) {
                                levels[targetIdx] = levels[i] + 1;
                                if (levels[targetIdx] > maxLevel) maxLevel = levels[targetIdx];
                                expanded = true;
                            }
                        }
                    }
                }
            }
        }

        for (int i = 0; i < n; i++) {
            if (levels[i] == -1) levels[i] = maxLevel + 1;
        }

        int[] countPerLevel = new int[maxLevel + 2];
        for (int i = 0; i < n; i++) countPerLevel[levels[i]]++;

        int[] currentPosOnLevel = new int[maxLevel + 2];
        double topPadding = 50.0;
        double bottomPadding = 50.0;
        double levelHeight = (maxLevel == 0) ? 0 : (h - topPadding - bottomPadding) / Math.max(maxLevel, 1);

        double[] xPos = new double[n];
        double[] yPos = new double[n];

        for (int i = 0; i < n; i++) {
            int lvl = levels[i];
            int pos = currentPosOnLevel[lvl]++;
            double count = countPerLevel[lvl];

            xPos[i] = (w / (count + 1)) * (pos + 1);
            yPos[i] = (maxLevel == 0) ? h / 2.0 : topPadding + lvl * levelHeight;
        }

        for (int i = 0; i < n; i++) {
            Vertex v = graph.findVertexById(subGraphIds[i]);
            if (v == null) continue;

            Connections[] conns = v.getConnections();
            for (int k = 0; k < v.getConnectionCount(); k++) {
                int targetIdx = findIndex(subGraphIds, conns[k].getFriendId(), n);
                if (targetIdx > i) {
                    boolean inPath = isEdgeInPath(subGraphIds[i], conns[k].getFriendId());
                    if (inPath) {
                        gc.setStroke(Color.RED);
                        gc.setLineWidth(3.5);
                    } else {
                        gc.setStroke(Color.rgb(180, 180, 180));
                        gc.setLineWidth(1.2);
                    }
                    gc.strokeLine(xPos[i], yPos[i], xPos[targetIdx], yPos[targetIdx]);

                    double midX = (xPos[i] + xPos[targetIdx]) / 2.0;
                    double midY = (yPos[i] + yPos[targetIdx]) / 2.0;
                    gc.setFill(Color.DARKSLATEBLUE);
                    gc.fillText(String.valueOf(conns[k].getConnection()), midX + 3, midY - 3);
                }
            }
        }

        double nodeR = 15;
        for (int i = 0; i < n; i++) {
            Vertex v = graph.findVertexById(subGraphIds[i]);
            if (v == null) continue;
            Profile p = v.getProfile();
            double x = xPos[i];
            double y = yPos[i];

            if (highlightedPath.length > 0 && subGraphIds[i] == highlightedPath[0]) {
                gc.setFill(Color.LIMEGREEN);
            } else if (highlightedPath.length > 0 && subGraphIds[i] == highlightedPath[highlightedPath.length - 1]) {
                gc.setFill(Color.CRIMSON);
            } else if (containsInt(highlightedPath, subGraphIds[i])) {
                gc.setFill(Color.RED);
            } else if (selectedProfile != null && p.getId() == selectedProfile.getId()) {
                gc.setFill(Color.GOLD);
            } else if (containsInt(visitedNodes, subGraphIds[i])) {
                gc.setFill(Color.ORANGE);
            } else if (p instanceof DeletedProfile) {
                gc.setFill(Color.GRAY);
            } else if (p instanceof AdminProfile) {
                gc.setFill(Color.LIGHTSKYBLUE);
            } else {
                gc.setFill(Color.LIGHTGREEN);
            }

            gc.fillOval(x - nodeR, y - nodeR, nodeR * 2, nodeR * 2);
            gc.setStroke(Color.BLACK);
            gc.setLineWidth(1.5);
            gc.strokeOval(x - nodeR, y - nodeR, nodeR * 2, nodeR * 2);

            gc.setFill(Color.BLACK);
            gc.fillText(String.valueOf(subGraphIds[i]), x - 5, y + 4);
        }
    }


    private int[] collectSubGraphIds(boolean hasPath) {
        int[] temp = new int[64];
        int size = 0;

        if (hasPath) {
            for (int id : highlightedPath) {
                if (!containsInt(temp, id, size)) temp[size++] = id;
            }
            for (int id : visitedNodes) {
                if (size >= 30) break;
                if (!containsInt(temp, id, size)) temp[size++] = id;
            }
        } else if (selectedProfile != null) {
            int selfId = selectedProfile.getId();
            temp[size++] = selfId;

            Vertex selfV = graph.findVertexById(selfId);
            if (selfV != null) {
                for (int i = 0; i < selfV.getConnectionCount(); i++) {
                    int fId = selfV.getConnections()[i].getFriendId();
                    if (!containsInt(temp, fId, size) && size < temp.length) {
                        temp[size++] = fId;
                    }
                }
                int directFriendsCount = size;
                for (int i = 1; i < directFriendsCount; i++) {
                    Vertex fV = graph.findVertexById(temp[i]);
                    if (fV != null) {
                        for (int k = 0; k < fV.getConnectionCount(); k++) {
                            int ffId = fV.getConnections()[k].getFriendId();
                            if (!containsInt(temp, ffId, size) && size < temp.length) {
                                temp[size++] = ffId;
                            }
                        }
                    }
                }
            }
        }

        int[] result = new int[size];
        System.arraycopy(temp, 0, result, 0, size);
        return result;
    }

    private boolean containsInt(int[] arr, int val, int len) {
        for (int i = 0; i < len; i++) {
            if (arr[i] == val) return true;
        }
        return false;
    }

    private int findIndex(int[] arr, int val, int len) {
        for (int i = 0; i < len; i++) {
            if (arr[i] == val) return i;
        }
        return -1;
    }

    private boolean containsInt(int[] arr, int val) {
        for (int x : arr) {
            if (x == val) return true;
        }
        return false;
    }

    private boolean isEdgeInPath(int u, int v) {
        if (highlightedPath.length < 2) return false;
        for (int i = 0; i < highlightedPath.length - 1; i++) {
            int p1 = highlightedPath[i];
            int p2 = highlightedPath[i + 1];
            if ((p1 == u && p2 == v) || (p1 == v && p2 == u)) return true;
        }
        return false;
    }
}