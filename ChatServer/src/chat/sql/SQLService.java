package chat.sql;

public interface SQLService {

    String getUsernameByLoginAndPassword(String login, String password);

    void changeUsernameInSQL(String username, String newUsername);

    boolean insertNewUser(String username, String login, String password);
}
