package core_system_normal_socket;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private static final int PORT = 8080;
    private static final int BUFFER_SIZE = 8192;
    private static final int WORKER_COUNT = 3;
    private static String filePathToRead;

    public static void main(String[] args) {

        File randomDataTextFilePath2G= new File("/home/vazek/Documents/internship_document/random_data_text_file.txt");
        File randomDataTextFilePath100MB= new File("/home/vazek/Documents/internship_document/random_text_100MB.txt"); //for test

        ExecutorService executorService = Executors.newFixedThreadPool(WORKER_COUNT);

        try (ServerSocketChannel serverSocketChannel = ServerSocketChannel.open()) {
            serverSocketChannel.bind(new InetSocketAddress(PORT));
            serverSocketChannel.configureBlocking(true);
            System.out.println("Server started on port " + PORT);

            List<SocketChannel> workersChannels = new ArrayList<>();
            System.out.println("Waiting for : "+WORKER_COUNT+" to connect...");

            for (int i = 0; i < WORKER_COUNT; i++) {
                SocketChannel socketChannel = serverSocketChannel.accept();
                socketChannel.configureBlocking(true);
                workersChannels.add(socketChannel);
                System.out.println("Worker " + (i+1) + " connected from " + socketChannel.getRemoteAddress());
            }

            System.out.println("All "+WORKER_COUNT+" are connected, starting file distribution...");

            // TODO: Implement file reading and distribution logic

            // Wait for worker connections

            // Read file and distribute data to workers

            try(FileChannel fileChannel = FileChannel.open(Paths.get(randomDataTextFilePath100MB.getPath()), StandardOpenOption.READ)){
                long fileSize = fileChannel.size();
                System.out.println("File size: " + fileSize + " bytes");

                ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);

                while (fileChannel.read(buffer) != -1) {
                    buffer.flip();  // Prepare buffer for reading

                    // Send the same data to each worker
                    for (SocketChannel workerChannel : workersChannels) {
                        buffer.rewind();  // Reset position to start
                        while (buffer.hasRemaining()) {
                            workerChannel.write(buffer);
                        }
                    }

                    buffer.clear();  // Prepare buffer for writing
                }
            }

            for(SocketChannel socketChannel : workersChannels){
                socketChannel.close();
            }

            System.out.println("File distribution completed");

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            executorService.shutdown();
        }
    }



}
