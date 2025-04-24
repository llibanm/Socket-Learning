import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class client {

    private Socket socket;
    private int Port;
    private BufferedReader in;
    private PrintWriter out;
    public client(int port) {
        this.Port = port;
    }

    public void start() throws IOException {

        this.socket = new Socket("localhost", this.Port);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);


    }


}
