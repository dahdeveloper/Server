package Server.Services;

import Server.interfaces.Service;
import java.io.*;

/**
 * This service is an HTTP mirror, just like the HttpMirror class implemented
 * earlier in this chapter. It echos back the client's HTTP request
 *
 */
public class HTTPMirror implements Service {

    @Override
    public void serve(InputStream i, OutputStream o) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(i));
        PrintWriter out = new PrintWriter(o);
        out.print("HTTP/1.0 200 \n");
        out.print("Content-Type: text/plain\n\n");
        String line;
        while ((line = in.readLine()) != null) {
            if (line.length() == 0) {
                break;
            }
            out.print(line + "\n");
        }
        out.close();
        in.close();
    }
}
