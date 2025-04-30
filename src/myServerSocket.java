import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class myServerSocket {

    private int port;
    private int socketID; // Id de l'instance de myServerSocket

    private Socket socket;
    private ServerSocket serverSocket;

    private String myAddress;

    private BufferedReader in;
    private PrintWriter out;

    public myServerSocket(int id,int port,String address){
        socketID = id;
        this.port = port;
        this.myAddress = address;
    }

    public void demarrer(){
        try {
            this.serverSocket = new ServerSocket(this.port);

            printMessage("[SERVER "+socketID+"] : server connected to "+this.myAddress+":"+this.port);
            printMessage("[SERVER "+socketID+"] : server awaiting connection");

            this.socket = serverSocket.accept();

            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            printMessage("[SERVER "+socketID+"] : accepted connection from address :"+socket.getInetAddress()+" on port "+socket.getPort());
            printMessage("[SERVER "+socketID+"] : now ending connection");

            in.close();
            out.close();
            socket.close();
            serverSocket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void printMessage(String msg){
        System.out.println(msg);
    }

    public int getPort() {
        return port;
    }

    public String getMyAddress() {
        return myAddress;
    }

    public int getSocketID() {
        return socketID;
    }

    public static void main(String[] args) {
        myServerSocket s = new myServerSocket(0,3000,"localhost");
        s.demarrer();
    }
}
