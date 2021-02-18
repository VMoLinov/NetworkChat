package chat.handler;

import chat.MyServer;
import chat.sql.SQLService;
import clientserver.Command;
import clientserver.CommandType;
import clientserver.commands.*;

import java.io.*;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

public class ClientHandler {

    private final MyServer myServer;
    private final Socket clientSocket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private String username;
    private long delay = 120000L;

    public ClientHandler(MyServer myServer, Socket clientSocket) {
        this.myServer = myServer;
        this.clientSocket = clientSocket;
    }

    public void handle() throws IOException {
        in = new ObjectInputStream(clientSocket.getInputStream());
        out = new ObjectOutputStream(clientSocket.getOutputStream());
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                if (username == null) {
                    try {
                        System.out.println("Превышено время ожидания. Соединение разорвано");
                        clientSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        new Timer().schedule(timerTask, delay);
        new Thread(() -> {
            try {
                authentication();
                readMessage();
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }).start();
    }

    private void authentication() throws IOException {
        while (true) {
            Command command = readCommand();
            if (command == null) {
                continue;
            }
            if (command.getType() == CommandType.AUTH) {
                boolean isSuccessAuth = processAuthCommand(command);
                if (isSuccessAuth) {
                    break;
                }
            } else if (command.getType() == CommandType.REG) {
                boolean isSuccessReg = processRegCommand(command);
                if (isSuccessReg) {
                    break;
                }
            } else sendMessage(Command.authErrorCommand("Ошибка авторизации"));
        }
    }

    private boolean processAuthCommand(Command command) throws IOException {
        AuthCommandData cmdData = (AuthCommandData) command.getData();
        String login = cmdData.getLogin();
        String password = cmdData.getPassword();
        SQLService sqlService = myServer.getsqlService();
        this.username = sqlService.getUsernameByLoginAndPassword(login, password);
        if (username != null) {
            if (myServer.isUsernameBusy(username)) {
                sendMessage(Command.authErrorCommand("Логин уже используется"));
                return false;
            }
            sendMessage(Command.authOkCommand(username));
            String message = String.format(">>> %s присоединился к чату", username);
            myServer.broadcastMessage(this, Command.messageInfoCommand(message, null));
            myServer.subscribe(this);
            return true;
        } else {
            sendMessage(Command.authErrorCommand("Логин или пароль не соответствуют действительности"));
            return false;
        }
    }

    private boolean processRegCommand(Command command) throws IOException {
        RegisterCommandData cmdData = (RegisterCommandData) command.getData();
        String username = cmdData.getUsername();
        String login = cmdData.getLogin();
        String password = cmdData.getPassword();
        SQLService sqlService = myServer.getsqlService();
        if (sqlService.insertNewUser(username, login, password)) {
            this.username = username;
            sendMessage(Command.registerOkCommand(username));
            String message = String.format(">>> %s join chat", username);
            myServer.broadcastMessage(this, Command.messageInfoCommand(message, null));
            myServer.subscribe(this);
            return true;
        } else {
            sendMessage(Command.registerErrorCommand("Login already exists"));
            return false;
        }
    }

    private Command readCommand() throws IOException {
        try {
            return (Command) in.readObject();
        } catch (ClassNotFoundException e) {
            String errorMessage = "Получен неизвестный объект";
            System.err.println(errorMessage);
            e.printStackTrace();
            return null;
        }
    }

    private void readMessage() throws IOException {
        while (true) {
            Command command = readCommand();
            if (command == null) {
                continue;
            }
            switch (command.getType()) {
                case END: {
                    EndCommandData data = (EndCommandData) command.getData();
                    String message = String.format(">>> %s вышел из чата", data.getUsername());
                    myServer.broadcastMessage(this, Command.messageInfoCommand(message, null));
                    myServer.unSubscribe(this);
                    clientSocket.close();
                    break;
                }
                case NICK: {
                    ChangeUsernameCommandData data = (ChangeUsernameCommandData) command.getData();
                    if (!myServer.isUsernameBusy(data.getNewUsername())) {
                        String message = String.format(">>> %s теперь %s <<<", data.getUsername(), data.getNewUsername());
                        myServer.broadcastMessage(this, Command.messageInfoCommand(message, null));
                        myServer.changeUsername(data.getUsername(), data.getNewUsername());
                    } else {
                        sendMessage(Command.errorCommand("Логин уже используется"));
                    }
                    break;
                }
                case PUBLIC_MESSAGE: {
                    PublicMessageCommandData data = (PublicMessageCommandData) command.getData();
                    String message = data.getMessage();
                    String sender = data.getSender();
                    myServer.broadcastMessage(this, Command.messageInfoCommand(message, sender));
                    break;
                }
                case PRIVATE_MESSAGE:
                    PrivateMessageCommandData data = (PrivateMessageCommandData) command.getData();
                    String recipient = data.getReceiver();
                    String message = data.getMessage();
                    myServer.sendPrivateMessage(recipient, Command.messageInfoCommand(message, username));
                    break;
                default:
                    String errorMessage = "Неизвестный тип команды" + command.getType();
                    System.err.println(errorMessage);
                    sendMessage(Command.errorCommand(errorMessage));
            }
        }
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String newUsername) {
        username = newUsername;
    }

    public void sendMessage(Command command) throws IOException {
        out.writeObject(command);
    }
}
