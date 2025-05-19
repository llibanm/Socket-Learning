package socketTest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Iterator;
import java.util.Set;

public class selectorWorkerTest implements Runnable {

    private int ID;
    private static int BUFFER_SIZE = 64 * 1024; //64kb allocated
    private ByteBuffer byteBuffer = ByteBuffer.allocate(BUFFER_SIZE);
    private static final String UPLOAD_DIRECTORY = "/home/vazek/Documents/internship_document/worker/worker100MB/worker";
    private final String UPLOAD_DIRECTORY_FINAL;
    private long bytesTransferred = 0;

    public selectorWorkerTest(int ID,String UPLOAD_DIRECTORY_FINAL) {
        this.ID = ID;
        this.UPLOAD_DIRECTORY_FINAL = UPLOAD_DIRECTORY + UPLOAD_DIRECTORY_FINAL;
        try (FileOutputStream fos = new FileOutputStream(this.UPLOAD_DIRECTORY_FINAL)) {
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

            socketChannelWorker.connect(new InetSocketAddress("localhost",9999)); // connection to localhost server on port 2000
            socketChannelWorker.register(selectorWorker, SelectionKey.OP_CONNECT); //

            File fileUpload = new File(UPLOAD_DIRECTORY_FINAL);

            FileChannel fileChannel = FileChannel.open(Paths.get(UPLOAD_DIRECTORY_FINAL),
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE);



            printMessage("File receive will be on: "+UPLOAD_DIRECTORY_FINAL);
//            boolean data_integrity_check = false;
//            boolean size_check = false;
//            boolean ack_size_check = false;
//            long fileSize = 0;

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
                        //if (size_check) {
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
                       // }
//                        else if(!size_check){ // we grab the size of the file
//                            SocketChannel socketChannel = (SocketChannel) key.channel();
//                            ByteBuffer byteBufferLong = ByteBuffer.allocate(Long.BYTES);
//                            int totalBytesLong = 0;
//
//                            while(totalBytesLong < Long.BYTES){
//                                int byteRead = socketChannel.read(byteBufferLong);
//                                if(byteRead == -1){
//                                    throw new IOException("connexion interrompue");
//                                }
//                                totalBytesLong += byteRead;
//                            }
//
//                            byteBufferLong.flip();
//                            fileSize = byteBufferLong.getLong();
//                            printMessage("received File size: "+fileSize);
//                            key.interestOps(SelectionKey.OP_WRITE);
//                            //size_check=true;
//                        }
                    }
                    else if (key.isWritable()) {// sending ack for receiving data
//                        if (!ack_size_check) {
//                            SocketChannel socketChannel = (SocketChannel) key.channel();
//                            ByteBuffer byteBuffer = ByteBuffer.wrap("ACK".getBytes());
//                            while(byteBuffer.hasRemaining()){
//                                socketChannel.write(byteBuffer);
//                            }
//                            ack_size_check=true;
//                            key.interestOps(SelectionKey.OP_READ);
//                            printMessage("ACK sent to server");
//                        }


                    }

                }
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            selectorWorkerTest test = new selectorWorkerTest(0,"1.txt");
            Thread thread = new Thread(test);
            thread.start();
            thread.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
