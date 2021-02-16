package clientserver.commands;

import java.io.Serializable;

public class ChangeUsernameCommandData implements Serializable {

    private final String username;
    private final String newUsername;

    public ChangeUsernameCommandData(String username, String newUsername) {
        this.username = username;
        this.newUsername = newUsername;
    }

    public String getUsername() {
        return username;
    }

    public String getNewUsername() {
        return newUsername;
    }
}
