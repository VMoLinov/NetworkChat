import chat.MyServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.logging.Level;

public class ServerApp {

    private static final int DEFAULT_PORT = 8189;

    public static final Logger LOGGER = LogManager.getLogger(ServerApp.class.getName());

    public static void main(String[] args) {
        System.setProperty("log4j.configurationFile","/ChatServer/resources/log4j2.xml");
        int port = DEFAULT_PORT;

        if (args.length != 0) {
            port = Integer.parseInt(args[0]);
        }

        try {
            new MyServer(port).start();
        } catch (IOException e) {
            LOGGER.error("Error!");
            System.exit(1);
        }
    }
}
