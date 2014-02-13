package Server.interfaces;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

    /**
     * Here is the Service interface that we have seen so much of. It defines
     * only a single method which is invoked to provide the service. serve()
     * will be passed an input stream and an output stream to the client. It
     * should do whatever it wants with them, and should close them before
     * returning.
     *
     * All connections through the same port to this service share a single
     * Service object. Thus, any state local to an individual connection must be
     * stored in local variables within the serve() method. State that should be
     * global to all connections on the same port should be stored in instance
     * variables of the Service class. If the same Service is running on more
     * than one port, there will typically be different Service instances for
     * each port. Data that should be global to all connections on any port
     * should be stored in static variables.
     *
     * Note that implementations of this interface must have a no-argument
     * constructor if they are to be dynamically instantiated by the main()
     * method of the Server class.
     *
     */
    public interface Service {

        public void serve(InputStream in, OutputStream out) throws IOException;
    }
