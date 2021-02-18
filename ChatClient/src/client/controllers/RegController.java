package client.controllers;

import client.NetworkClient;
import client.models.Network;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class RegController {

    @FXML
    private TextField usernameField;

    @FXML
    public TextField loginField;

    @FXML
    public PasswordField passwordField;

    private Network network;
    private NetworkClient networkClient;

    public Network getNetwork() {
        return network;
    }

    public void setNetwork(Network network) {
        this.network = network;
    }

    public NetworkClient getNetworkClient() {
        return networkClient;
    }

    public void setNetworkClient(NetworkClient networkClient) {
        this.networkClient = networkClient;
    }
}
