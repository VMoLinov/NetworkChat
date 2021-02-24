package client.controllers;

import client.NetworkClient;
import client.models.Network;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.io.IOException;

public class RegController {

    @FXML
    private TextField usernameField;

    @FXML
    public TextField loginField;

    @FXML
    public PasswordField passwordField;

    private Network network;
    private NetworkClient networkClient;

    @FXML
    public void checkRegistration() throws IOException {
        String username = usernameField.getText();
        String login = loginField.getText();
        String password = passwordField.getText();
        if (username.isBlank() || login.isBlank() || password.isBlank()) {
            NetworkClient.showErrorMessage("Ошибка авторизации", "Ошибка ввода", "Поля не должны быть пустыми");
            return;
        }
        String registerErrorMessage = network.sendRegisterCommand(username, login, password);
        if (registerErrorMessage != null) {
            NetworkClient.showErrorMessage("Ошибка регистрации", "Что-то не то", registerErrorMessage);
        } else {
            network.setLogin(login);
            networkClient.closeRegStage();
            networkClient.openMainChatWindow();
        }
    }

    public void setNetwork(Network network) {
        this.network = network;
    }

    public void setNetworkClient(NetworkClient networkClient) {
        this.networkClient = networkClient;
    }
}
