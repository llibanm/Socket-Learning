package core_system_normal_socket;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.atomic.AtomicInteger;

public class Worker implements Runnable {
    private static final int BUFFER_SIZE = 8192;
    private static final String serverAddress="localhost";
    private static final int serverPort=8080;
    private static String writingFilePathFirst="/home/vazek/Documents/internship_document/workerSocket/worker";
    private static String writingFilePathFinal;
    private static int workerID;

    public Worker(AtomicInteger wID,String filePathToWriteAdditional) {
        workerID = wID.get();

        writingFilePathFinal= writingFilePathFirst + filePathToWriteAdditional;
    }

    @Override
    public void run(){


        int port = 8080;

        try {
            SocketChannel socketChannel = SocketChannel.open();
            socketChannel.connect(new InetSocketAddress(serverAddress, port));
            socketChannel.configureBlocking(true);
            System.out.println("Worker " + workerID + " connected to " + serverAddress + ":" + port);

            // TODO: Implement logic to receive data and write to file

            FileChannel outputFileChannel = FileChannel.open( // create output file channel
                    Paths.get(writingFilePathFinal),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.WRITE,
                    StandardOpenOption.TRUNCATE_EXISTING
            );
            ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
            long totalBytesReceived = 0;

            int bytesRead;
            while((bytesRead = socketChannel.read(buffer))!=-1){
                if(bytesRead > 0){
                    buffer.flip();
                    outputFileChannel.write(buffer);
                    totalBytesReceived += bytesRead;
                    buffer.clear();
                }
            }
            outputFileChannel.close();
            socketChannel.close();
            printMessage("Received and wrote " + totalBytesReceived + " bytes to " + writingFilePathFinal);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void printMessage(String message) {
        System.out.println("[Worker:"+workerID+"]: "+message);
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        Worker worker = new Worker(new AtomicInteger(1), "1.txt");
        Thread thread = new Thread(worker);
        thread.start();
        thread.join();
    }
}