package test;

import java.awt.*;
import java.io.*;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Iterator;
import java.util.Set;

public class socketServerTest {

    private SocketChannel[] connections = new SocketChannel[3];
    private final int BUFFER_SIZE = 64 * 1024; // 64KB
    int connectionCount = 0;
    long linesNumber=0;

    public void run() throws IOException {
        File randomDataTextFilePath2G= new File("/home/vazek/Documents/internship_document/random_data_text_file.txt");

        File randomDataTextFilePath100MB= new File("/home/vazek/Documents/internship_document/random_text_100MB.txt"); //for test
        long fileSizeRandomDataTextFile2G = randomDataTextFilePath2G.length();
        long fileSizeRandomDataTextFilePath100MB = (randomDataTextFilePath100MB.length()/ (1024 * 1024));
        long bytesSent=0;
        try {



            Selector selector = Selector.open();

            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.socket().bind(new InetSocketAddress("localhost", 9999)); // server open

            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT); // server register incoming key on status OP_ACCEPT

            FileInputStream fis = new FileInputStream(randomDataTextFilePath100MB);
            FileChannel fileChannel = fis.getChannel();


            ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE); // allocating buffer size of 64KB chunks
            System.out.println("[SERVER] : SERVER open on port 2000");

            boolean data_integrity_check = false;
            boolean size_check = false;
            boolean ack_size_check = false;



            while (true) {

                int readyChannels = selector.select();

                if (readyChannels == 0) { // if no channel available, reran looping
                    continue;
                }

                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectionKeys.iterator();

                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    iterator.remove();

                    if (key.isAcceptable()) {
                        SocketChannel socketChannel = ((ServerSocketChannel)key.channel()).accept(); // handleAccept part
                        socketChannel.configureBlocking(false);

                        if(connectionCount < 3){
                            connections[connectionCount] = socketChannel;
                            connectionCount++;
                        }

                        socketChannel.register(selector, SelectionKey.OP_WRITE); // change OP_ACCEPT into OP_WRITE, server will write to incoming connections
                        System.out.println("[SERVER] : SERVER accepted connection from: "+socketChannel.getRemoteAddress());
                        System.out.println("[SERVER] : Preparing to send file: " + randomDataTextFilePath100MB.getName() + " (" +
                                (fileSizeRandomDataTextFilePath100MB + " MB)"));
                        System.out.println("[SERVER] : sending data size");
                    }
                    else if (key.isWritable()) {
                        if (size_check) {
                            SocketChannel socketChannel = (SocketChannel)key.channel();
                            fileChannel = FileChannel.open(Paths.get(randomDataTextFilePath100MB.getPath()), StandardOpenOption.READ);
                            fileSizeRandomDataTextFilePath100MB = fileChannel.size();
                            //System.out.println("[SERVER] : file opened, sending file size: " + fileSizeRandomDataTextFilePath100MB);

                            ByteBuffer byteBuffer = ByteBuffer.allocate(BUFFER_SIZE);
                            int bytesRead = fileChannel.read(byteBuffer);

                            if(bytesRead > 0){
                                byteBuffer.flip();

                                if(bytesRead == 0){

                                }
                                byteBuffer.clear();
                                int bytesWritten = socketChannel.write(byteBuffer);
                                bytesSent+=bytesWritten;
    //                            System.out.println("[SERVER] : Sent: "+(bytesSent / 1024)+" kb written out of "+(fileSizeRandomDataTextFilePath100MB * 1024)+" b");
                            }

                            if(bytesRead == -1 || bytesSent >= fileSizeRandomDataTextFilePath100MB){
                                double percentComplete = (double) bytesSent / fileSizeRandomDataTextFilePath100MB * 100;
                                System.out.printf("Transfer progress: %.2f%% (%d/%d bytes)%n",
                                        percentComplete, bytesSent, fileSizeRandomDataTextFilePath100MB);

                                System.out.println("[SERVER] : data transfert complete");
                                fileChannel.close();
                                socketChannel.close();
                                bytesSent=0;
                            }
                        }
                        else if(!size_check){// we sent beforehand the size of the incoming file
                            SocketChannel socketChannel = (SocketChannel)key.channel();
                            ByteBuffer byteBuffer = ByteBuffer.allocate(Long.BYTES);
                            byteBuffer.putLong(randomDataTextFilePath100MB.length());
                            byteBuffer.flip();
                            while(byteBuffer.hasRemaining()){
                                socketChannel.write(byteBuffer);
                            }
                           //size_check=true;
                            key.interestOps(SelectionKey.OP_READ);
                        }

                    }
                    else if (key.isReadable()) {
                        if (!ack_size_check) {
                            SocketChannel socketChannel = (SocketChannel) key.channel();
                            ByteBuffer byteBufferAck = ByteBuffer.allocate(3);
                            int totalByteRead=0;
                            while(totalByteRead < 3){
                                int bytesRead = socketChannel.read(byteBufferAck);
                                if(bytesRead == -1){
                                    System.out.println("[SERVER] : client disconnected");
                                    return;
                                }
                                totalByteRead+=bytesRead;
                            }
                            byteBufferAck.flip();
                            byte[] ackBytes = new byte[3];
                            byteBufferAck.get(ackBytes);
                            String ackString = new String(ackBytes);
                            if(ackString.equals("ACK")){
                                System.out.println("[SERVER] : ACK received, will start sending data now");
                            }
                            key.interestOps(SelectionKey.OP_WRITE);
                        }

                    }

                }


            }


        } catch (IOException  e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) throws IOException {

        socketServerTest socketServerTest = new socketServerTest();
        socketServerTest.run();

    }

    public SocketChannel[] getConnections() {
        return connections;
    }
}
