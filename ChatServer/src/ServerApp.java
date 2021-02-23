import chat.MyServer;

import java.io.IOException;
import java.util.logging.Level;

public class ServerApp {

    private static final int DEFAULT_PORT = 8189;

    public static void main(String[] args) {
        int port = DEFAULT_PORT;

        if (args.length != 0) {
            port = Integer.parseInt(args[0]);
        }

        try {
            new MyServer(port).start();
        } catch (IOException e) {
            MyServer.LOGGER.log(Level.WARNING, "Error!");
            System.exit(1);
        }
    }
}
