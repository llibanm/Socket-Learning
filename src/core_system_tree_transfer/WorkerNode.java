package core_system_tree_transfer;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

public class WorkerNode {

    private static final int[] WORKER_PORTS = {9001,9002,9003};
    private  int nodeID;
    private  int port;
    private static final int BUFFER_SIZE = 8 * 1024; //paquets de 8KB par envoi
    private static final ConcurrentHashMap<String,Long> receivedFiles = new ConcurrentHashMap<>();

    public WorkerNode(int nodeID, int port) {
        this.nodeID = nodeID;
        this.port = port;
    }


    private void startServer() throws IOException {
        try(ServerSocket serverSocket = new ServerSocket(port)) {
            while(true) {
                try (Socket socket = serverSocket.accept()) {
                    //handleCLient(clientSocket)
                }catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void handleClient(Socket socket) throws IOException {
        DataInputStream dis = new DataInputStream(socket.getInputStream());

        //receive file name
        String fileName = dis.readUTF();

        // receive tree position
        int senderPosition = dis.readInt();

        // receive file size
        long fileSize = dis.readLong();

        System.out.println("Worker " + nodeID + " receiving file: " + fileName +
                " (Size: " + (fileSize / (1024*1024)) + " MB) from position " + senderPosition);

        long startTime = System.currentTimeMillis();


        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
    }
}
