package ru.korchinskiy.client;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import ru.korchinskiy.common.AuthMessage;
import ru.korchinskiy.common.Message;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class LoginController implements Initializable {
    @FXML
    public TextField loginField;

    @FXML
    public TextField passField;

    @FXML
    public Label infoLabel;

    private Connection connection;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        connection = Connection.getConnection();
        connection.start();
        Thread readThread = new Thread(() -> getMessage());
        readThread.setDaemon(true);
        readThread.start();
    }

    private void getMessage() {
        try {
            while (true) {
                Message message = connection.readMessage();
                if (message instanceof AuthMessage) {
                    AuthMessage authMessage = (AuthMessage) message;
                    if (authMessage.getLogin().equals(""))
                        if (authMessage.getType().equals(AuthMessage.SIGN_IN_TYPE))
                            updateInfoLabel("Неверные логин/пароль");
                        else
                            updateInfoLabel("Такой пользователь уже существует");
                    else {
                        switchScene();
                        break;
                    }
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void signIn() {
        String login = loginField.getText();
        String pass = passField.getText();
        if (login.equals("") || pass.equals("")) {
            updateInfoLabel("Введите логин и пароль!!");
            return;
        }
        AuthMessage message = new AuthMessage(AuthMessage.SIGN_IN_TYPE, login, pass);
        connection.sendMessage(message);
    }

    public void switchScene() {
        Platform.runLater(() -> {
            Parent root = null;
            try {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/mainWindow.fxml"));
                root = fxmlLoader.load();
                Stage stage = (Stage) loginField.getScene().getWindow();
                Scene scene = new Scene(root, 600, 450);
                stage.setScene(scene);
                stage.setTitle(loginField.getText());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void updateInfoLabel(String message) {
        Platform.runLater(() -> {
            infoLabel.setText(message);
        });
    }

    public void register(ActionEvent actionEvent) {
        String login = loginField.getText();
        String pass = passField.getText();
        if (login.equals("") || pass.equals("")) {
            updateInfoLabel("Введите логин и пароль!!");
            return;
        }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Создаем нового пользователя " + login + " ?",
                ButtonType.OK, ButtonType.CANCEL);
        Optional<ButtonType> result = alert.showAndWait();
        if (result.get().getText().equals("OK")) {
            AuthMessage message = new AuthMessage(AuthMessage.REGISTER_TYPE, login, pass);
            connection.sendMessage(message);
        }
    }

    public void onEnterPressed(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ENTER)
            signIn();
    }
}
