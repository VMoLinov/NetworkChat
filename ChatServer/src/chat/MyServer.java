package chat;

import chat.sql.*;
import chat.handler.ClientHandler;
import clientserver.Command;

import java.io.IOException;
import java.net.*;
import java.util.*;

public class MyServer {

    private final ServerSocket serverSocket;
    private final SQLService sqlService;
    private final List<ClientHandler> clients = new ArrayList<>();

    public MyServer(int port) throws IOException {
        this.serverSocket = new ServerSocket(port);
        this.sqlService = new BaseSQLService();
    }

    public void start() throws IOException {
        System.out.println("Сервер запущен!");
        try {
            while (!Thread.currentThread().isInterrupted()) {
                waitAndProcessNewClientConnection();
            }
        } catch (IOException e) {
            System.out.println("Ошибка создания нового подключения");
            e.printStackTrace();
        } finally {
            serverSocket.close();
        }
    }

    private void waitAndProcessNewClientConnection() throws IOException {
        System.out.println("Ожидание пользователя...");
        Socket clientSocket = serverSocket.accept();
        System.out.println("Клиент подключился!");
        processClientConnection(clientSocket);
    }

    private void processClientConnection(Socket clientSocket) throws IOException {
        ClientHandler clientHandler = new ClientHandler(this, clientSocket);
        clientHandler.handle();
    }

    public SQLService getAuthService() {
        return sqlService;
    }

    public synchronized boolean isUsernameBusy(String clientUsername) {
        for (ClientHandler client : clients) {
            if (client.getUsername().equals(clientUsername)) {
                return true;
            }
        }
        return false;
    }

    public synchronized void subscribe(ClientHandler clientHandler) throws IOException {
        clients.add(clientHandler);
        List<String> usernames = getAllUsernames();
        broadcastMessage(null, Command.updateUsersListCommand(usernames));
    }

    private List<String> getAllUsernames() {
        List<String> usernames = new ArrayList<>();
        for (ClientHandler client : clients) {
            usernames.add(client.getUsername());
        }
        return usernames;
    }

    public synchronized void unSubscribe(ClientHandler clientHandler) throws IOException {
        clients.remove(clientHandler);
        List<String> usernames = getAllUsernames();
        broadcastMessage(null, Command.updateUsersListCommand(usernames));
    }

    public synchronized void broadcastMessage(ClientHandler sender, Command command) throws IOException {
        for (ClientHandler client : clients) {
            if (client == sender) {
                continue;
            }
            client.sendMessage(command);
        }
    }

    public synchronized void sendPrivateMessage(String recipient, Command command) throws IOException {
        for (ClientHandler client : clients) {
            if (client.getUsername().equals(recipient)) {
                client.sendMessage(command);
                break;
            }
        }
    }

    public synchronized void changeUsername(String username, String newUsername) throws IOException {
        changeClientInList(username, newUsername);
        List<String> usernames = getAllUsernames();
        broadcastMessage(null, Command.updateUsersListCommand(usernames));
        sqlService.changeUsernameInSQL(username, newUsername);
    }

    private void changeClientInList(String username, String newUsername) {
        for (ClientHandler client : clients) {
            if (client.getUsername().equals(username)) {
                client.setUsername(newUsername);
                break;
            }
        }
    }
}
