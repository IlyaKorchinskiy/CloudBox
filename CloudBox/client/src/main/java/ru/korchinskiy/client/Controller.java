package ru.korchinskiy.client;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.input.MouseEvent;
import ru.korchinskiy.common.*;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Controller implements Initializable {
    @FXML
    public ListView<String> cloudListView;

    @FXML
    public ListView<String> localListView;

    @FXML
    public Label infoLabel;

    @FXML
    public ProgressBar progressBar;

    private Connection connection;

    private RequestMessage request;
    private File directory;
    private FileMessage fileMessage;
    private ExecutorService executorService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        connection = Connection.getConnection();
        request = new RequestMessage();
        directory = new File("client/local/");
        fileMessage = new FileMessage();
        progressBar.setProgress(0);
        progressBar.setVisible(false);

        executorService = Executors.newSingleThreadExecutor();

        Thread readThread = new Thread(() -> getMessage());
        readThread.setDaemon(true);
        readThread.start();

        updateLocalHost();
        requestUpdate();

        initializeListView(localListView);
        initializeListView(cloudListView);
    }

    private void initializeListView(ListView<String> listView) {
        listView.setEditable(true);
        listView.setCellFactory(TextFieldListCell.forListView());
        listView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        ContextMenu menu = new ContextMenu();
        MenuItem renameItem = new MenuItem("Rename");
        MenuItem deleteItem = new MenuItem("Delete");
        renameItem.setOnAction(event -> listView.edit(listView.getSelectionModel().getSelectedIndex()));
        deleteItem.setOnAction(event -> {
            if (listView.getId().equals("localListView"))
                deleteLocalFile(event);
            else
                requestDelete(event);
        });
        menu.getItems().addAll(renameItem, deleteItem);
        listView.setContextMenu(menu);
        listView.setOnEditCommit(event -> {
            if (renameFile(event))
                listView.getItems().set(event.getIndex(), event.getNewValue());
        });
        listView.setOnEditCancel(event -> {

        });
    }

    private void getMessage() {
        try {
            while (true) {
                Message message = connection.readMessage();
                if (message instanceof FileMessage) {
                    FileMessage msg = (FileMessage) message;
                    try {
                        FileOutputStream out = new FileOutputStream(new File(directory, msg.getName()), true);
                        out.write(msg.getBytes());
                        out.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    updateLocalHost();
                }
                if (message instanceof RequestMessage) {
                    RequestMessage request = (RequestMessage) message;
                    if (request.getType().equals(RequestMessage.REQUEST_UPDATE)) {
                        updateCloudList(request);
                    }
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void updateCloudList(RequestMessage request) {
        Platform.runLater(() -> {
            String[] filesList = request.getFiles();
            if (filesList == null) return;
            cloudListView.getItems().setAll(Arrays.asList(filesList));
        });
    }

    // отправляем файлы в облако
    public void sendFileToServer(ActionEvent actionEvent) {
        ObservableList<String> selectedItems = localListView.getSelectionModel().getSelectedItems();
        if (checkForSelection(selectedItems, infoLabel)) return;
        String[] files = new String[selectedItems.size()];
        executorService.submit(() -> {
            byte[] bytes = new byte[FileMessage.MAX_BYTE_AMOUNT];
            for (String fileName : selectedItems.toArray(files)) {
                try {
                    BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(directory.getPath() + "/" + fileName));
                    int parts;
                    parts = inputStream.available() / bytes.length;
                    System.out.println(inputStream.available());
                    fileMessage.setName(fileName);
                    for (int i = 0; i < parts + 1; i++) {
                        if (inputStream.available() < bytes.length) {
                            byte[] lastBytes = new byte[inputStream.available()];
                            inputStream.read(lastBytes);
                            inputStream.close();
                            fileMessage.setBytes(lastBytes);
                        } else {
                            inputStream.read(bytes);
                            fileMessage.setBytes(bytes);
                        }
                        connection.sendMessage(fileMessage);
                        updateBar("Передаю " + fileName, parts, i);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            updateBar("", 1, -1);
        });
    }

    private void updateBar(String text, int parts, int i) {
        Platform.runLater(() -> {
            infoLabel.setText(text);
            if (i == -1)
                progressBar.setVisible(false);
            else
                progressBar.setVisible(true);
            progressBar.setProgress((double) 1 / parts * (i + 1));
        });
    }

    // подгружаем файлы из локального каталога
    public void updateLocalHost() {
        Platform.runLater(() -> {
            String[] list = directory.list();
            if (list == null) return;
            localListView.getItems().setAll(Arrays.asList(list));
        });
    }

    // удалить локальный файл
    public void deleteLocalFile(ActionEvent actionEvent) {
        ObservableList<String> selectedItems = localListView.getSelectionModel().getSelectedItems();
        if (checkForSelection(selectedItems, infoLabel)) return;
        String[] files = new String[selectedItems.size()];
        try {
            for (String file : selectedItems.toArray(files)) {
                Files.delete(Paths.get(directory.getPath() + "/" + file));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        updateLocalHost();
    }

    // запросить файлы с сервера
    public void requestFile(ActionEvent actionEvent) {
        ObservableList<String> selectedItems = cloudListView.getSelectionModel().getSelectedItems();
        if (checkForSelection(selectedItems, infoLabel)) return;
        ObservableList<String> items = localListView.getItems();
        for (String item : items) {
            for (String selected : selectedItems) {
                if (item.equals(selected)) {
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Такой файл уже есть. Перезаписать?",
                            ButtonType.OK, ButtonType.CANCEL);
                    Optional<ButtonType> result = alert.showAndWait();
                    if (result.get().getText().equals("Cancel")) return;
                }
            }
        }
        sendRequest(RequestMessage.REQUEST_FILE, selectedItems);
    }

    // запрос на удаление файлов с сервера
    public void requestDelete(ActionEvent actionEvent) {
        ObservableList<String> selectedItems = cloudListView.getSelectionModel().getSelectedItems();
        if (checkForSelection(selectedItems, infoLabel)) return;
        sendRequest(RequestMessage.REQUEST_DELETE, selectedItems);
    }

    // запрос на переименование файла
    public void requestRename(String newValue) {
        ObservableList<String> selectedItems = FXCollections.observableArrayList();
        selectedItems.add(cloudListView.getSelectionModel().getSelectedItem());
        if (checkForSelection(selectedItems, infoLabel)) return;
        selectedItems.add(newValue);
        sendRequest(RequestMessage.REQUEST_RENAME, selectedItems);
    }

    // запрос на обновление файлов на сервере
    public void requestUpdate() {
        sendRequest(RequestMessage.REQUEST_UPDATE, null);
    }

    private void sendRequest(String type, ObservableList<String> selectedItems) {
        request.setType(type);
        if (selectedItems != null) {
            String[] files = new String[selectedItems.size()];
            request.setFiles(selectedItems.toArray(files));
        }
        connection.sendMessage(request);
    }

    private boolean checkForSelection(ObservableList<String> selectedItems, Label infoLabel) {
        if (selectedItems.size() == 0) {
            infoLabel.setText("Ничего не выбрано!");
            return true;
        }
        return false;
    }

    // очистим выбранные при клике на пустоту
    public void clearCloudSelected(MouseEvent event) {
        cloudListView.getSelectionModel().clearSelection();
    }

    public void clearLocalSelected(MouseEvent event) {
        localListView.getSelectionModel().clearSelection();
    }

    public void renameCloudFile(ActionEvent actionEvent) {
        cloudListView.edit(cloudListView.getSelectionModel().getSelectedIndex());
    }

    public void renameLocalFile(ActionEvent actionEvent) {
        localListView.edit(localListView.getSelectionModel().getSelectedIndex());
    }

    private boolean renameFile(ListView.EditEvent<String> event) {
        String listViewName = event.getSource().getId();
        ObservableList<String> items = event.getSource().getItems();
        for (String item : items) {
            if (item.equals(event.getNewValue())) {
                infoLabel.setText("Такое имя уже есть");
                return false;
            }
        }
        if (listViewName.equals("localListView")) {
            try {
                Path source = Paths.get(directory + "/" + event.getSource().getSelectionModel().getSelectedItem());
                Files.move(source, source.resolveSibling(event.getNewValue()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (listViewName.equals("cloudListView")) {
            requestRename(event.getNewValue());
        }
        return true;
    }
}
