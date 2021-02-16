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
    private TextArea chatHistory;

    @FXML
    private TextField textField;

    @FXML
    private Label usernameTitle;

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
        this.history = new File (history);
    }

    @FXML
    public void initialize() {
        sendButton.setOnAction(event -> ChatController.this.sendMessage());
        textField.setOnAction(event -> ChatController.this.sendMessage());
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
        try {
            if ("/end".equals(message)) {
                saveHistory();
                network.sendCloseCommand();
            }
            if (message.startsWith("/nick")) {
                String[] split = message.split("\\s+", 2);
                network.ChangeUsername(split[1]);
                if (network.getUsername().equals(split[1])) {
                    setLabel(split[1]);
                }
                return;
            }
            appendMessage("Я: " + message);
            if (selectedRecipient != null) {
                network.sendPrivateMessage(message, selectedRecipient);
            } else {
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
    }
}
