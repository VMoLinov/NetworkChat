package chat.sql;

import java.sql.*;

public class BaseSQLService implements SQLService {

    private static Connection connection;
    private static Statement stmt;
    private static ResultSet rs;

    public void start() throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection("jdbc:sqlite:ChatServer/resources/mainDB.db");
        stmt = connection.createStatement();
    }

    @Override
    public String getUsernameByLoginAndPassword(String login, String password) {
        try {
            start();
            rs = stmt.executeQuery(String.format("SELECT username, password FROM users WHERE login = '%s'", login));
            if (password.equals(rs.getString("password"))) {
                return rs.getString("username");
            }
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        } finally {
            close();
        }
        return null;
    }

    @Override
    public void changeUsernameInSQL(String username, String newUsername) {
        try {
            start();
            stmt.executeUpdate(String.format("UPDATE users SET username = '%s' WHERE username = '%s'", newUsername, username));
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        } finally {
            close();
        }
    }

    public void close() {
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
