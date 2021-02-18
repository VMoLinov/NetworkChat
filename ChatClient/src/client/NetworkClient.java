package client;

import client.controllers.*;
import client.models.Network;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.*;
import javafx.scene.control.Alert;
import javafx.stage.*;

import java.io.IOException;

public class NetworkClient extends Application {

    private Stage primaryStage;
    private Stage authStage;
    private Stage regStage;
    private Network network;
    private ChatController chatController;

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;
        network = new Network();
        if (!network.connect()) {
            showErrorMessage("Проблемы с соединением", "", "Ошибка подключения к серверу");
            return;
        }
        openAuthWindow();
        createMainChatWindow();
    }

    private void openAuthWindow() throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(NetworkClient.class.getResource("views/auth-view.fxml"));
        Parent root = loader.load();
        authStage = new Stage();
        authStage.setTitle("Авторизация");
        authStage.initModality(Modality.WINDOW_MODAL);
        authStage.initOwner(primaryStage);
        Scene scene = new Scene(root);
        authStage.setScene(scene);
        authStage.show();
        AuthController authController = loader.getController();
        authController.setNetwork(network);
        authController.setNetworkClient(this);
    }

    public void createMainChatWindow() throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(NetworkClient.class.getResource("views/chat-view.fxml"));
        Parent root = loader.load();
        primaryStage.setTitle("Messenger");
        primaryStage.setScene(new Scene(root, 600, 400));
        chatController = loader.getController();
        chatController.setNetwork(network);
        primaryStage.setOnCloseRequest(windowEvent -> {
            chatController.saveHistory();
            network.sendCloseCommand();
        });
    }

    public static void showErrorMessage(String title, String message, String errorMessage) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(message);
        alert.setContentText(errorMessage);
        alert.showAndWait();
    }

    public void openRegisterWindow() throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(NetworkClient.class.getResource("views/reg-view.fxml"));
        Parent root = loader.load();
        regStage = new Stage();
        regStage.setTitle("Registration");
        regStage.initModality(Modality.WINDOW_MODAL);
        regStage.initOwner(primaryStage);
        Scene scene = new Scene(root);
        regStage.setScene(scene);
        authStage.close();
        regStage.show();
        RegController regController = loader.getController();
        regController.setNetwork(network);
        regController.setNetworkClient(this);
    }

    public void openMainChatWindow() throws IOException {
        authStage.close();
        chatController.setLabel(network.getUsername());
        chatController.setHistory("ChatClient/resources/history_" + network.getLogin() + ".txt");
        chatController.appendHistory();
        primaryStage.show();
        primaryStage.setAlwaysOnTop(true);
        network.waitMessage(chatController);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
