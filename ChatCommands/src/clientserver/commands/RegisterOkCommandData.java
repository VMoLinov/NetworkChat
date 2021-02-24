package clientserver.commands;

import java.io.Serializable;

public class RegisterOkCommandData implements Serializable {

    private String username;

    public RegisterOkCommandData(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }
}
