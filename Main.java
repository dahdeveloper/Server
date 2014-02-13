package Server;

import Server.Services.Control;
import Server.Services.Reverse;
import Server.interfaces.Service;
import java.io.IOException;
import java.util.logging.Logger;

/**
 *
 * @author Nhahn
 */
public class Main {

    private final static Logger LOGGER = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        try {
            LoggerConfig.setup();
            LOGGER.info("Init Application");

            // Create a Server object that uses standard out as its log and
            // has a limit of ten concurrent connections at once.
            Server s = new Server(10);

            // Parse the argument list
            s.addService(new Control(s, "1234"), 27000);
            s.addService((Service) new Reverse(), 23000);

        } catch (IOException e) { // Display a message if anything goes wrong
            System.err.println("Server: " + e);
            System.err.println("Usage: java Server "
                    + "[-control <password> <port>] "
                    + "[<servicename> <port> ... ]");
            System.exit(1);
        }
    }
}
