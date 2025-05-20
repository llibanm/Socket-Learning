package core_system_tree_transfer;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class TreeBroadcastServer {

    private static final int SERVER_PORT = 9000;
    private static final int[] WORKER_PORTS = {9001,9002,9003};
    private static final String FILE_100MB = "/home/vazek/Documents/internship_document/random_text_100MB.txt";
    private static final String FILE_2GB = "/home/vazek/Documents/internship_document/random_data_text_file.txt";
    private static final long MB = 1024 * 1024;

    public static void main(String[] args) {


        try {

            TreeBroadcastServer server = new TreeBroadcastServer();
            System.out.println("Starting workers...");
            startWorkers();

            Thread.sleep(1000);


            server.performTreeBroadcast(FILE_100MB);

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

            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());

            dos.writeUTF(fileName);

            dos.writeInt(workerIndex);

            File file = new File(fileName);
            if(file.exists()){
                System.out.println("File exists!");
            }
            dos.writeLong(file.length());

            try(FileInputStream fis = new FileInputStream(file)) {
                byte[] bytes = new byte[(int)file.length()];
                int bytesRead=0;

                while((bytesRead = fis.read(bytes)) != -1) {
                    dos.write(bytes, 0, bytesRead);
                }
                dos.flush();
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
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
