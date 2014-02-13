package Server.Services;

import Server.interfaces.Service;
import java.io.*;
import java.util.Date;

/**
 * A very simple service. It displays the current time on the server to the
 * client, and closes the connection.
 *
 */
public class Time implements Service {

    @Override
    public void serve(InputStream i, OutputStream o) throws IOException {
        PrintWriter out = new PrintWriter(o);
        out.print(new Date() + "\n");
        out.close();
        i.close();
    }
}
