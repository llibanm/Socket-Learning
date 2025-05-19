package core_system_normal_socket;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.atomic.AtomicLong;

public class Worker2GB implements Runnable {
    private static final int BUFFER_SIZE = 8192;
    private static final long PROGRESS_INTERVAL = 10 * 1024 * 1024;
    private static final String serverAddress="localhost";
    private static final int serverPort=8080;
    private static String writingFilePathFirst="/home/vazek/Documents/internship_document/workerSocket2GB/worker";
    private String writingFilePathFinal;
    private int workerID;

    public Worker2GB(int wID, String filePathToWriteAdditional) {
        workerID = wID;
        System.out.println("Worker ID: "+workerID);
        writingFilePathFinal= writingFilePathFirst + filePathToWriteAdditional;
    }


    public void run(){
        System.out.println("Worker ID: "+workerID);
        try {
            SocketChannel socketChannel = SocketChannel.open();
            socketChannel.connect(new InetSocketAddress(serverAddress, serverPort));
            socketChannel.configureBlocking(true);
            printMessage("Worker " + workerID + " connected to " + serverAddress + ":" + serverAddress);

            // TODO: Implement logic to receive data and write to file

            FileChannel outputFileChannel = FileChannel.open( // create output file channel
                    Paths.get(writingFilePathFinal),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.WRITE,
                    StandardOpenOption.TRUNCATE_EXISTING
            );
            ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
            AtomicLong totalBytesReceived = new AtomicLong(0);
            long startTime = System.currentTimeMillis();
            long lastProgressReport = 0;

            int bytesRead;
            while((bytesRead = socketChannel.read(buffer))!=-1){
                if(bytesRead > 0){
                    buffer.flip();
                    outputFileChannel.write(buffer);
                    long currentTotal = totalBytesReceived.addAndGet(bytesRead);
                    buffer.clear();

                    if (currentTotal - lastProgressReport >= PROGRESS_INTERVAL) {
                        long elapsedSeconds = (System.currentTimeMillis() - startTime) / 1000;
                        double mbReceived = currentTotal / (1024.0 * 1024.0);
                        double mbps = elapsedSeconds > 0 ? mbReceived / elapsedSeconds : 0;

                        System.out.printf("[Worker:"+workerID+"]: "+" Progress: %.2f MB received (%.2f MB/s)%n \n",
                                mbReceived, mbps);
                        lastProgressReport = currentTotal;
                    }

                }
            }
            outputFileChannel.close();
            socketChannel.close();
            System.out.println("[Worker:"+workerID+"]: "+" Received and wrote " + totalBytesReceived + " bytes to " + writingFilePathFinal+"\n");

            long elapsedSeconds = (System.currentTimeMillis() - startTime) / 1000;
            double mbReceived = totalBytesReceived.get() / (1024.0 * 1024.0);
            double mbps = elapsedSeconds > 0 ? mbReceived / elapsedSeconds : 0;

            System.out.printf("[Worker:"+workerID+"]: "+" Worker " + workerID +" Completed: %.2f MB received and written to %s%n \n",
                    mbReceived, writingFilePathFinal);
            System.out.printf("[Worker:"+workerID+"]: "+" Average transfer rate: %.2f MB/s%n \n", mbps);


        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public  void printMessage(String message) {
        System.out.println("[Worker:"+workerID+"]: "+message);
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        Worker2GB worker = new Worker2GB(0, "0.txt");
        Thread thread = new Thread(worker);
        thread.start();
        thread.join();
    }
}