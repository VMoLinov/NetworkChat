package clientserver.commands;

import java.io.Serializable;

public class RegisterErrorCommandData implements Serializable {

    private String errorMessage;

    public RegisterErrorCommandData(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
