package Server;

import Server.interfaces.Service;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This nested Thread subclass is a "listener". It listens for connections on a
 * specified port (using a ServerSocket) and when it gets a connection request,
 * it calls the addConnection() method to accept (or reject) the
 * connection. There is one Listener for each Service being provided by the
 * Server.
 *
 * @author Nhahn
 */
public class Listener extends Thread implements Iterable<Connection> {

    // The set of current connections
    private final ArrayList<Connection> connections;
    private static int numConnections;
    private static int maxConnections;		// The concurrent connection limit
    ServerSocket listen_socket;    		// The socket to listen for connections

    int port;                      // The port we're listening on
    Service service;               // The service to provide on that port
    volatile boolean stop = false; // Whether we've been asked to stop

    private static final Logger LOGGER = Logger.getLogger(Listener.class.getName());

    /**
     * The Listener constructor creates a thread for itself in the threadgroup.
     * It creates a ServerSocket to listen for connections on the specified
     * port. It arranges for the ServerSocket to be interruptible, so that
     * services can be removed from the server.
     *
     * @param group
     * @param port
     * @param service
     * @throws java.io.IOException
     */
    public Listener(ThreadGroup group, int port, Service service)
            throws IOException {
        super(group, "Listener:" + port);

        this.port = port;
        this.service = service;
        connections = new ArrayList<Connection>();
        listen_socket = new ServerSocket(port);
        // give it a non-zero timeout so accept() can be interrupted
        //listen_socket.setSoTimeout(600000);
    }

    /**
     * This is the polite way to get a Listener to stop accepting connections *
     */
    public void pleaseStop() {
        this.stop = true;              // Set the stop flag
        this.interrupt();              // Stop blocking in accept()
        try {
            listen_socket.close();
            endAllConnection();
        } // Stop listening.
        catch (IOException e) {
        }
    }

    /**
     * A Listener is a Thread, and this is its body. Wait for connection
     * requests, accept them, and pass the socket on to the addConnection method
     * of the server.
     *
     */
    @Override
    public void run() {
        while (!stop) {      // loop until we're asked to stop.
            try {
                Socket client = listen_socket.accept();
                addConnection(client, service);
            } catch (InterruptedIOException e) {
            } catch (IOException e) {
                //Logger
            }
        }
    }

    /**
     * This is the method that Listener objects call when they accept a
     * connection from a client. It either creates a Connection object for the
     * connection and adds it to the list of current connections, or, if the
     * limit on connections has been reached, it closes the connection.
     *
     * @param s
     * @param service
     */
    protected synchronized void addConnection(Socket s, Service service) {
        // If the connection limit has been reached
        if (numConnections >= maxConnections) {
            try {
                // Then tell the client it is being rejected.
                PrintWriter out = new PrintWriter(s.getOutputStream());
                out.print("Connection refused; "
                        + "the server is busy; please try again later.\n");
                out.flush();
                // And close the connection to the rejected client.
                s.close();
                // And log it, of course
                LOGGER.log(Level.INFO, "Connection refused to {0}:{1}: max connections reached.",
                        new Object[]{s.getInetAddress().getHostAddress(), s.getPort()});
            } catch (IOException e) {

            }
        } else {
            // Otherwise, if the limit has not been reached
            // Create a Connection thread to handle this connection
            Connection c = new Connection(this, s, service);
            numConnections++;

            // Add it to the list of current connections
            connections.add(c);

            // Log this new connection
            LOGGER.log(Level.INFO, "Connected to {0}:{1} on port {2} for service {3}",
                    new Object[]{
                        s.getInetAddress().getHostAddress(),
                        s.getPort(), s.getLocalPort(),
                        service.getClass().getName()
                    });

            // And start the Connection thread to provide the service
            c.start();
        }
    }

    /**
     * A Connection thread calls this method just before it exits. It removes
     * the specified Connection from the set of connections.
     *
     * @param c
     * @throws java.io.IOException
     */
    protected synchronized void endConnection(Connection c) throws IOException {
        // Add it to the list of current connections
        connections.remove(c);
        numConnections--;
        c.Close();
        LOGGER.log(Level.INFO, "Connection to {0}:{1} closed.",
                new Object[]{c.client.getInetAddress().getHostAddress(), c.client.getPort()});
    }

    /**
     * A Connection thread calls this method just before it exits. It removes
     * the specified Connection from the set of connections.
     *
     * @throws java.io.IOException
     */
    protected synchronized void endAllConnection() throws IOException {
        for (Connection c : connections) {
            c.Close();
        }
        numConnections -= connections.size();
        connections.clear();
    }

    /**
     * Change the current connection limit
     *
     * @param max
     */
    public static void setMaxConnections(int max) {
        maxConnections = max;
    }

    @Override
    public Iterator<Connection> iterator() {
        return new ListnerIterator(connections);
    }

    private class ListnerIterator implements Iterator<Connection> {

        ArrayList<Connection> connections;
        int current;

        public ListnerIterator(ArrayList<Connection> con) {
            connections = con;
            current = 0;
        }

        @Override
        public boolean hasNext() {
            return current < connections.size();
        }

        @Override
        public Connection next() {
            Connection c = connections.get(current);
            current++;
            return c;
        }

        @Override
        public void remove() {
            try {
                endConnection(connections.get(current));
            } catch (IOException ex) {
                Logger.getLogger(Listener.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

}
