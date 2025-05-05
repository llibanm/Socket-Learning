package core_system;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Worker implements Runnable {

    private final int ID;
    private final int port = 2000;
    private Socket socket;

    private BufferedReader inClient;
    private PrintWriter outClient;
    public Worker(int ID){
        this.ID = ID;
    }

    @Override
    public void run() {

        try {
            System.out.println("[CLIENT:"+getID()+"] : Connecting to server...");
            socket = new Socket("localhost", port);

            inClient = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
            outClient = new PrintWriter(this.socket.getOutputStream(), true);

            System.out.println("[CLIENT:"+getID()+"] : Client connected to server: " + socket.getRemoteSocketAddress()+":"+socket.getPort());
            Thread.sleep(3000);
            System.out.println("[CLIENT:"+getID()+"] : Client disconnecting from server...");
            inClient.close();
            outClient.close();
            socket.close();
            System.out.println("[CLIENT:"+getID()+"] : Client disconnected");
        }catch (IOException | InterruptedException e){
            e.printStackTrace();
        }

    }

    public int getID() {
        return ID;
    }

    public static void main(String[] args) {
        try {
            Worker worker = new Worker(0);
            Thread thread = new Thread(worker);
            thread.start();
            thread.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
