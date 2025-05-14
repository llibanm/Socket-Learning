package test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Iterator;
import java.util.Set;

public class selectorWorkerTest implements Runnable {

    private int ID;
    private static int BUFFER_SIZE = 64 * 1024; //8kb allocated
    private ByteBuffer byteBuffer = ByteBuffer.allocate(BUFFER_SIZE);
    private static final String UPLOAD_DIRECTORY = "/home/vazek/Documents/internship_document/worker/worker100MB/worker.txt";
    private long bytesTransferred = 0;

    public selectorWorkerTest(int ID) {
        this.ID = ID;
        try (FileOutputStream fos = new FileOutputStream(UPLOAD_DIRECTORY)) {
            printMessage("File emptied successfully");
        } catch (IOException e) {
           e.printStackTrace();
        }
    }

    public void printMessage(String message) {
        System.out.println("[Worker: "+ID+"]: "+message);
    }

    @Override
    public void run() {
        try{
            Selector selectorWorker = Selector.open();
            SocketChannel socketChannelWorker = SocketChannel.open();
            socketChannelWorker.configureBlocking(false);

            printMessage("connection to server");

            socketChannelWorker.connect(new InetSocketAddress("localhost",2000)); // connection to localhost server on port 2000
            socketChannelWorker.register(selectorWorker, SelectionKey.OP_CONNECT); //

            File fileUpload = new File(UPLOAD_DIRECTORY);

            if(!fileUpload.exists()){
                fileUpload.mkdir();
            }

            FileChannel fileChannel = FileChannel.open(Paths.get(UPLOAD_DIRECTORY),
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE);



            printMessage("File receive will be on: "+UPLOAD_DIRECTORY);
            Boolean headerDone = false;

            long startTime = System.currentTimeMillis();

            while (true) { // tant que la connection est etablie

                int ready = selectorWorker.select();
                if(ready == 0){
                    continue;
                }



                Set<SelectionKey> keys = selectorWorker.selectedKeys();
                Iterator<SelectionKey> iterator = keys.iterator();

                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    iterator.remove();

                    if (key.isConnectable()) {

                        if(socketChannelWorker.finishConnect()){
                            printMessage("ACCEPTED connection to server: "+socketChannelWorker.getRemoteAddress());
                        }

                        SocketChannel secondChannelWorker = (SocketChannel) key.channel();

                        secondChannelWorker.register(selectorWorker, SelectionKey.OP_READ);

                        printMessage("READ connection from: "+secondChannelWorker.getRemoteAddress());
                    }
                    else if (key.isReadable()) {
                        SocketChannel socketChannel = (SocketChannel) key.channel();
                        byteBuffer.clear();
                        int bytesRead = socketChannel.read(byteBuffer);

                        if(bytesRead > 0) {
                            byteBuffer.flip();
                            fileChannel.write(byteBuffer);
                            bytesTransferred += bytesRead;
                        } else if (bytesRead == -1) {
                            long endTime = System.currentTimeMillis();
                            double transferTime = (endTime - startTime)/1000.0;
                            double transferRate = (bytesTransferred/(1024*1024)/transferTime);


                            printMessage("File transfert complete");
                            System.out.printf("[Worker: "+ID+"] : Received: %.2f MB%n", bytesTransferred / (1024.0 * 1024.0));
                            System.out.printf("[Worker: "+ID+"] : Transfer time: %.2f seconds%n", transferTime);
                            System.out.printf("[Worker: "+ID+"] : Transfer rate: %.2f MB/s%n", transferRate);
                            socketChannel.close();
                            fileChannel.close();
                            selectorWorker.close();
                            return;
                        }
                    }
                    else if (key.isWritable()) {


                    }

                }
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            selectorWorkerTest test = new selectorWorkerTest(0);
            Thread thread = new Thread(test);
            thread.start();
            thread.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
