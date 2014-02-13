package Server;

import Server.interfaces.*;
import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class is a generic framework for a flexible, multi-threaded server. It
 * listens on any number of specified ports, and, when it receives a connection
 * on a port, passes input and output streams to a specified Service object
 * which provides the actual service. It can limit the number of concurrent
 * connections, and logs activity to a specified stream.
 *
 */
public class Server {

    // This is the state for the server
    private Map<Integer, Listener> services;    // Hashtable mapping ports to Listeners
    private ThreadGroup threadGroup;            // The threadgroup for all our threads
    private int maxConnections;

    private static final Logger LOGGER = Logger.getLogger(Server.class.getName());

    /**
     * This is the Server() constructor. It must be passed a stream to send log
     * output to (may be null), and the limit on the number of concurrent
     * connections.
     *
     * @param maxConnections
     */
    public Server(int maxConnections) {
        LOGGER.info("Starting server");
        initServer(maxConnections);
    }

    private void initServer(int maxConnections) {
        threadGroup = new ThreadGroup(Server.class.getName());
        threadGroup.setDaemon(true);
        this.maxConnections = maxConnections;
        services = new HashMap<Integer, Listener>();
        Listener.setMaxConnections(maxConnections);
    }

    /**
     * This is the Server() constructor. It must be passed a stream to send log
     * output to (may be null), and the limit on the number of concurrent
     * connections.
     *
     * @throws java.lang.Throwable
     */
    protected void closeServer() throws Throwable {
        LOGGER.info("Closing Server");
        removeAllServices();
    }

    /**
     * This method makes the server start providing a new service. It runs the
     * specified Service object on the specified port.
     *
     * @param service
     * @param port
     * @throws java.io.IOException
     */
    public synchronized void addService(Service service, int port)
            throws IOException {
        Integer key = new Integer(port);  // the hashtable key
        // Check whether a service is already on that port
        if (services.get(key) != null) {
            throw new IllegalArgumentException("Port " + port
                    + " already in use.");
        }
        // Create a Listener object to listen for connections on the port
        Listener listener = new Listener(threadGroup, port, service);
        // Store it in the hashtable
        services.put(key, listener);

        LOGGER.log(Level.INFO, "Starting service {0} on port {1}",
                new Object[]{service.getClass().getName(), port});

        // Start the listener running.
        listener.start();
    }

    /**
     * This method makes the server stop providing a service on a port. It does
     * not terminate any pending connections to that service, merely causes the
     * server to stop accepting new connections
     *
     * @param port
     * @throws java.io.IOException
     */
    public synchronized void removeService(int port) throws IOException {
        Integer key = new Integer(port);  // hashtable key
        // Look up the Listener object for the porportt in the hashtable
        final Listener listener = (Listener) services.get(key);
        if (listener == null) {
            return;
        }
        // Ask the listener to stop
        listener.pleaseStop();
        
        LOGGER.log(Level.INFO, "Stopping service {0} on port {1}",
                new Object[]{listener.service.getClass().getName(), port});
        
        // Remove it from the hashtable
        services.remove(key);
    }

    /**
     * This method makes the server stop providing a service on a port. It does
     * not terminate any pending connections to that service, merely causes the
     * server to stop accepting new connections
     *
     * @throws java.io.IOException
     */
    public synchronized void removeAllServices() throws IOException {
        // Look up the Listener object for the porportt in the hashtable
        for (Integer key : services.keySet()) {
            // Ask the listener to stop
            Listener listener = services.get(key);
            listener.pleaseStop();
            removeService(key);
            // Remove it from the hashtable
            services.remove(key);
        }
    }

    /**
     * Change the current connection limit
     *
     * @param max
     */
    public synchronized void setMaxConnections(int max) {
        maxConnections = max;
        Listener.setMaxConnections(max);
    }

    /**
     * This method displays status information about the server on the specified
     * stream. It can be used for debugging, and is used by the Control service
     * later in this example.
     *
     * @param out
     */
    public synchronized void displayStatus(PrintWriter out) {
        for (Integer port : services.keySet()) {
            Listener listener = (Listener) services.get(port);
            out.print("SERVICE " + listener.service.getClass().getName()
                    + " ON PORT " + port + "\n");
            for (Connection c : listener) {
                out.print("CONNECTED TO "
                        + c.client.getInetAddress().getHostAddress()
                        + ":" + c.client.getPort() + " ON PORT "
                        + c.client.getLocalPort() + " FOR SERVICE "
                        + c.service.getClass().getName() + "\n");
            }
        }
    }
}
