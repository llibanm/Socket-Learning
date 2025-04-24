import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class Master {

    private static ServerSocket serverSocket;
    private static Socket masterSocket;
    private static BufferedReader in;
    private static PrintWriter out;
    private Boolean running = true;

    public Master() throws IOException {}

    public void start() throws IOException {
        serverSocket = new ServerSocket(9998);
        System.out.println("[Master] Server started");

        System.out.println("[Master] Waiting for connection...");
        masterSocket = serverSocket.accept();

        in = new BufferedReader(new InputStreamReader(masterSocket.getInputStream()));
        out = new PrintWriter(masterSocket.getOutputStream(), true);
        String message;

        System.out.println("[Master] Client connected");

        while ((message = in.readLine()) != null) {
            if (message.equals("exit"))
                break;
            System.out.println("[Master] Recieved : "+message);
        }

        System.out.println("[Master] Client disconnected");
        serverSocket.close();

    }

    public InetAddress getMasterInetAddress() {
        return masterSocket.getInetAddress();
    }

    public static void main(String[] args) throws IOException {
        Master master = new Master();
        master.start();
    }

}
