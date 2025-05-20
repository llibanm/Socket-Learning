package core_system_tree_transfer;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class TreeBroadcastServer {

    private static final int SERVER_PORT = 9000;
    private static final int[] WORKER_PORTS = {9001,9002,9003};
    private static final String FILE_100MB = "/home/vazek/Documents/internship_document/random_text_100MB.txt";
    private static final String FILE_2GB = "/home/vazek/Documents/internship_document/random_data_text_file.txt";
    private static final long MB = 1024 * 1024;
    private static final int BUFFER_SIZE =16 * 1024 * 1024; //16MB

    public static void main(String[] args) {


        try {

            TreeBroadcastServer server = new TreeBroadcastServer();
            System.out.println("Starting workers...");
            startWorkers();

            Thread.sleep(1000);


            server.performTreeBroadcast(FILE_2GB);

            System.out.println("Broadcasting done. Shutting down...");
            System.exit(0);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static void startWorkers(){
        System.out.println("Starting workers...");
        for (int i = 0; i < WORKER_PORTS.length; i++) {
            final int workerIndex = i;
            new Thread(() -> {
                try {
                    WorkerNode worker = new WorkerNode(workerIndex,WORKER_PORTS[workerIndex]);
                    worker.start();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }).start();

        }
    };

    public void performTreeBroadcast(String fileName) {
        System.out.println("\n--- Starting Tree-based Broadcast for " + fileName + " ---");
        long startTime = System.currentTimeMillis();


        sendFileToWorker(0,fileName);

        waitForCompletion();

        long endTime = System.currentTimeMillis();
        double secondes = (endTime - startTime) / 1000.0;
        System.out.printf("Temps total de diffusion: %.3f secondes\n", secondes);
    }

    public void sendFileToWorker(int workerIndex, String fileName) {
        System.out.println("Server sending file to Worker " + workerIndex);

        try(Socket socket =  new Socket("localhost",  WORKER_PORTS[workerIndex])) {
            socket.setSendBufferSize(BUFFER_SIZE);
            socket.setTcpNoDelay(true);// pas de regroupement de paquet
            socket.setKeepAlive(true);

            DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream(), BUFFER_SIZE));

            dos.writeUTF(fileName);

            dos.writeInt(workerIndex);

            File file = new File(fileName);
            if(file.exists()){
                System.out.println("File exists!");
            }
            dos.writeLong(file.length());

            try(FileInputStream fis = new FileInputStream(file)) {

                byte[] buffer = new byte[BUFFER_SIZE];
                long totalBytesSent = 0;
                long reportInterval = file.length()/10;
                long nextReport = reportInterval;
                long startTime = System.currentTimeMillis();


                int bytesRead = 0;
                while((bytesRead = fis.read()) != -1) {
                    try {
                        dos.write(buffer,0,bytesRead);
                    } catch (IOException e) {
                        System.out.println("Writing file to Workers done.");
                        break;
                    }
                    totalBytesSent += bytesRead;
                }

            }
            System.out.println("File sent to Worker " + workerIndex);
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void waitForCompletion(){
        try {
            // Wait for workers to complete their transfers
            int minutes = 1;
            int seconds = minutes * 5;
            Thread.sleep(seconds*1000); //60 sec
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
