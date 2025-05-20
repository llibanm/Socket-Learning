package core_system_tree_transfer;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

public class WorkerNode {

    private static final int[] WORKER_PORTS = {9001,9002,9003};
    private  int nodeID;
    private  int port;
    private static final int BUFFER_SIZE = 16 * 1024 * 1024; //paquets de 16MB par envoi
    private static final ConcurrentHashMap<String,Long> receivedFiles = new ConcurrentHashMap<>();
    private boolean running;
    private ServerSocket serverSocket;

    public WorkerNode(int nodeID, int port) throws IOException {
        this.nodeID = nodeID;
        this.port = port;
        running = false;
        serverSocket = new ServerSocket(port);
    }


    public void start() throws IOException {
        System.out.println("Starting Worker Node " + nodeID + " on port " + port);
        this.running = true;
        try {
            while(true) {
                try (Socket socket = serverSocket.accept()) {
                    socket.setReceiveBufferSize(BUFFER_SIZE);
                    socket.setTcpNoDelay(true);
                    handleClient(socket);
                }catch (Exception e) {
                    if (this.running) {
                        e.printStackTrace();
                    }
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            stop();
        }
    }

    public void stop() {
        if (nodeID != 0) {


            try {
                Thread.sleep(20000);
                this.running = false;
                if(!serverSocket.isClosed()) {
                    serverSocket.close();
                }
            }catch (IOException | InterruptedException e){
                e.printStackTrace();
            }
        }
        else{
            try {
                Thread.sleep(20000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
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

        String workerDir = "/home/vazek/Documents/internship_document/workerTree/worker"+nodeID+".txt";

        try(FileOutputStream fos = new FileOutputStream(workerDir)) {
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;
            long totalBytesRead = 0;
            long reportInterval = fileSize / 10;
            long nextReportInterval = reportInterval;
            boolean complete = false;
            while(totalBytesRead < fileSize && !complete) {
                bytesRead = dis.read(buffer,0,(int)Math.min(BUFFER_SIZE, fileSize-totalBytesRead));
                if(bytesRead == -1) {
                    System.out.println("Worker " + nodeID + "reached end of file" );
                    complete = true;
                    break;
                }
                fos.write(buffer,0,bytesRead);
                totalBytesRead += bytesRead;
            }
            fos.flush();
        }

        long endTime = System.currentTimeMillis();
        long duration = (endTime - startTime);

        System.out.println("Worker " + nodeID + " received file in " + duration + " ms");
        receivedFiles.put(fileName, duration);

        if(nodeID == 0){
            forwardFile(fileName,1);
            forwardFile(fileName,2);
        }
    }


    private void forwardFile(String FileName, int targetWorker){

        try{

            System.out.println("Worker " + nodeID + " forwarding file to: " + targetWorker);
            String localFilePAth = "/home/vazek/Documents/internship_document/workerTree/worker"+nodeID+".txt";
            File localFile = new File(localFilePAth);

            try(Socket socket = new Socket("localhost", WORKER_PORTS[targetWorker])) {
                DataOutputStream dos = new DataOutputStream(socket.getOutputStream());

                dos.writeUTF(FileName);

                dos.writeInt(nodeID);

                dos.writeLong(localFile.length());

                try (FileInputStream fis = new FileInputStream(localFile)) {
                    byte[] buffer = new byte[BUFFER_SIZE];
                    int bytesRead;
                    while ((bytesRead = fis.read(buffer)) != -1) {
                        dos.write(buffer,0,bytesRead);
                    }
                    dos.flush();
                    System.out.println("Worker " + nodeID + " completed forwarding to Worker " + targetWorker);
                }

            }


        }catch (Exception e){
            e.printStackTrace();
        }

    }
}
