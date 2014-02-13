package Server.Services;

import java.io.*;
import Server.interfaces.*;

/**
 * This service demonstrates how to maintain state across connections by saving
 * it in instance variables and using synchronized access to those variables. It
 * maintains a count of how many clients have connected and tells each client
 * what number it is
 *
 */
public class UniqueID implements Service {

    public int id = 0;

    public synchronized int nextId() {
        return id++;
    }

    /**
     *
     * @param i
     * @param o
     * @throws IOException
     */
    @Override
    public void serve(InputStream i, OutputStream o) throws IOException {
        PrintWriter out = new PrintWriter(o);
        out.print("You are client #: " + nextId() + "\n");
        out.close();
        i.close();
    }
}
