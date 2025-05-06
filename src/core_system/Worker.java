package core_system;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.nio.ByteBuffer;

public class Worker implements Runnable {

    private final int ID;
    private final int port = 2000;
    private Socket socket;

    private BufferedReader reader;
    private PrintWriter writer;

    public Worker(int ID){
        this.ID = ID;
    }

    public void printMessageConsole(String msg){
        System.out.println("[WORKER: "+getID()+"]: "+msg);
    }

    private void serverOutput(){
        String serverResponse = null;

        try {
            printMessageConsole("Waiting for server response...");
//            while ((serverResponse = reader.readLine()) != null) {
//                System.out.println("Received from server: " + serverResponse);
//            }
            serverResponse = reader.readLine();
            if(serverResponse != null){
                printMessageConsole("Server response: "+serverResponse);

            }
        } catch (IOException e) {
            printMessageConsole("[ERROR]: "+e.getMessage());
        }
    }

//    private void serverOutputV2(){
//        int bytesRead = 0;
//
//        try {
//            bytesRead = socket.
//        }
//    }

    @Override
    public void run() {

        try {
            printMessageConsole("Connecting to server...");
            socket = new Socket("localhost", port);

            reader = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
            writer = new PrintWriter(this.socket.getOutputStream(), true);

            printMessageConsole("Worker connected to server: " + socket.getRemoteSocketAddress()+":"+socket.getPort());
            serverOutput();
            printMessageConsole("Worker disconnecting from server...");
            Thread.sleep(1000);
            reader.close();
            writer.close();
            socket.close();

            printMessageConsole("Worker  disconnected");
        } catch (ConnectException e){
            printMessageConsole("Error connecting to server, port: " + port+" may not exist");
        } catch (IOException
                 | InterruptedException
                e){
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
