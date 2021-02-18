package clientserver.commands;

import java.io.Serializable;

public class RegisterCommandData implements Serializable {

    private String username;
    private String login;
    private String password;

    public RegisterCommandData(String username, String login, String password) {
        this.username = username;
        this.login = login;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }
}
