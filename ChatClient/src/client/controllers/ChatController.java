package client.controllers;

import client.NetworkClient;
import client.models.Network;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;

import java.io.*;
import java.text.DateFormat;
import java.util.*;

public class ChatController {

    @FXML
    public ListView<String> usersList;

    @FXML
    private Button sendButton;

    @FXML
    private Button nameButton;

    @FXML
    private Button exitButton;

    @FXML
    private TextArea chatHistory;

    @FXML
    private TextField textField;

    @FXML
    private Label usernameTitle;

    @FXML
    private ComboBox usersListContext;

    private Network network;
    private String selectedRecipient;
    private File history;
    private final int HISTORY_SIZE = 100;

    public void setLabel(String usernameTitle) {
        this.usernameTitle.setText(usernameTitle);
    }

    public void setNetwork(Network network) {
        this.network = network;
    }

    public void setHistory(String history) {
        this.history = new File(history);
    }

    @FXML
    public void initialize() {
        sendButton.setOnAction(event -> ChatController.this.sendMessage());
        nameButton.setOnAction(event -> ChatController.this.changeName());
        textField.setOnAction(event -> ChatController.this.sendMessage());
        exitButton.setOnAction(event -> {
            ChatController.this.saveHistory();
            network.sendCloseCommand();
        });
        usersList.setCellFactory(lv -> {
            MultipleSelectionModel<String> selectionModel = usersList.getSelectionModel();
            ListCell<String> cell = new ListCell<>();
            cell.textProperty().bind(cell.itemProperty());
            cell.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
                usersList.requestFocus();
                if (!cell.isEmpty()) {
                    int index = cell.getIndex();
                    if (selectionModel.getSelectedIndices().contains(index)) {
                        selectionModel.clearSelection(index);
                        selectedRecipient = null;
                    } else {
                        selectionModel.select(index);
                        selectedRecipient = cell.getItem();
                    }
                    event.consume();
                }
            });
            return cell;
        });
    }

    private void sendMessage() {
        String message = textField.getText();
        if (message.isBlank()) {
            return;
        }
        textField.clear();
        String selectedRecipientBox = String.valueOf(usersListContext.getValue());
        try {
            if (!selectedRecipientBox.equals("Public message") && selectedRecipient != null) {
                NetworkClient.showErrorMessage("Ошибка отправки", "Ошибка при отправке сообщения", "Выберите одного получателя");
            } else if (!selectedRecipientBox.equals("Public message")) {
                appendMessage("Я: " + message);
                network.sendPrivateMessage(message, selectedRecipientBox);
            } else if (selectedRecipient != null) {
                appendMessage("Я: " + message);
                network.sendPrivateMessage(message, selectedRecipient);
            } else {
                appendMessage("Я: " + message);
                network.sendMessage(message);
            }
        } catch (IOException e) {
            e.printStackTrace();
            NetworkClient.showErrorMessage("Ошибка подключения", "Ошибка при отправке сообщения", e.getMessage());
        }
    }

    public void appendHistory() throws IOException {
        if (history.exists()) {
            var in = new BufferedReader(new FileReader(history));
            ArrayList<String> str = new ArrayList<>();
            String line;
            while ((line = in.readLine()) != null) {
                str.add(line + System.lineSeparator());
            }
            int lines = Math.min(str.size(), HISTORY_SIZE);
            for (int i = 0; i < lines; i++) {
                chatHistory.appendText(str.get(i));
            }
            in.close();
        } else history.createNewFile();
    }

    public void saveHistory() {
        try (FileWriter writer = new FileWriter(history)) {
            writer.write(chatHistory.getText());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void appendMessage(String message) {
        String timestamp = DateFormat.getInstance().format(new Date());
        chatHistory.appendText(timestamp);
        chatHistory.appendText(System.lineSeparator());
        chatHistory.appendText(message);
        chatHistory.appendText(System.lineSeparator());
        chatHistory.appendText(System.lineSeparator());
    }

    public void updateUsers(List<String> users) {
        usersList.setItems(FXCollections.observableArrayList(users));
        users.remove(network.getUsername());
        users.add("Public message");
        usersListContext.setItems(FXCollections.observableArrayList(users));
        usersListContext.setValue("Public message");
    }

    public void changeName() {
        String newUsername = textField.getText();
        if (newUsername.isBlank() || newUsername.equals(network.getUsername())) {
            NetworkClient.showErrorMessage("Ошибка смены ника", "Введите новый ник", "Новый ник не может быть пустым");
            return;
        }
        textField.clear();
        try {
            network.ChangeUsername(newUsername);
            if (network.getUsername().equals(newUsername)) {
                setLabel(newUsername);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}