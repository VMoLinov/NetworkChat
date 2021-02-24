package clientserver;

public enum CommandType {
    AUTH,
    AUTH_OK,
    AUTH_ERROR,
    REG,
    REG_OK,
    REG_ERROR,
    PRIVATE_MESSAGE,
    PUBLIC_MESSAGE,
    INFO_MESSAGE,
    ERROR,
    NICK,
    END,
    UPDATE_USERS_LIST
}
