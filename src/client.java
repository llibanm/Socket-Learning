import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class client {

    private Socket socket; // to connect itself to myServerSocket
    private int Port;

    private BufferedReader in;
    private PrintWriter out;

    public client(int port) {
        this.Port = port;
    }

    public void start() {

        try {
            System.out.println("[CLIENT] Engaging connection...");
            this.socket = new Socket("localhost", this.Port);
            System.out.println("[CLIENT] Connection established on address : " + this.socket.getInetAddress().getHostAddress()+" and port : "+this.socket.getPort());

            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            System.out.println("[CLIENT] Now quitting connection...");
            in.close();
            out.close();
            socket.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }
    public static void main(String[] args) {
        if(args.length != 1) {
            System.out.println("Usage: client <port>");
            return;
        }
        int port = Integer.parseInt(args[0]);
        client client = new client(port);
        client.start();
    }


}
