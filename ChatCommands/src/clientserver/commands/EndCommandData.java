package clientserver.commands;

import java.io.Serializable;

public class EndCommandData implements Serializable {

    private final String username;

    public EndCommandData(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }
}
