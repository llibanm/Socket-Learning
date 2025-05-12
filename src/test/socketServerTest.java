package test;

import java.io.*;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class socketServerTest {

    private SocketChannel[] connections = new SocketChannel[3];
    int connectionCount = 0;
    long linesNumber=0;

    public void run() throws IOException {
        String randomDataTextFilePath2G="/home/vazek/Documents/internship document/random_data_text_file.txt";
        String randomDataTextFilePath100MB="/home/vazek/Documents/internship document/random_text_100MB.txt"; //for test
        try {



            Selector selector = Selector.open();

            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.socket().bind(new InetSocketAddress("localhost", 2000)); // server open

            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT); // server register incoming key on status OP_ACCEPT

            System.out.println("[SERVER] : SERVER open on port 2000");

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
                        System.out.println("[SERVER] : sending data");
                    }
                    else if (key.isWritable()) {

                        long startTime = System.nanoTime();

                        SocketChannel socketChannel = (SocketChannel) key.channel();
                        ByteBuffer buffer;
                        byte[] messageBytes;
                        String message;
                        int messageLength;

                        try{BufferedReader br = new BufferedReader(new FileReader(randomDataTextFilePath2G));
                            while ((message = br.readLine()) != null) {
                                messageBytes = message.getBytes();
                                messageLength = messageBytes.length;
                                buffer = ByteBuffer.allocate(Integer.BYTES+messageLength);
                                buffer.putInt(messageLength);
                                buffer.put(messageBytes);
                                buffer.flip();

                                socketChannel.write(buffer);

                                if(!buffer.hasRemaining()){
                                    linesNumber++;
                                    //System.out.println("[SERVER] : lines: "+linesNumber);
                                }
                                buffer.clear();
                            }
                        }catch (IOException e){
                            throw new RuntimeException();
                        }

                        message="END";
                        messageBytes = message.getBytes();
                        messageLength = messageBytes.length;

                        buffer = ByteBuffer.allocate(Integer.BYTES+messageLength);// allocate the exact number of byte needed
                        buffer.putInt(messageLength);
                        buffer.put(messageBytes);
                        buffer.flip();

                        try {
                            socketChannel.write(buffer);

                            if(!buffer.hasRemaining()) {
                                System.out.println("[SERVER] : send data: "+message+" to "+socketChannel.getRemoteAddress());
                                key.interestOps(SelectionKey.OP_READ);// change write rights to read rights
                                long endTime = System.nanoTime();
                                long duration = endTime - startTime;
                                double durationSeconds = duration / 1000000000.0;
                                System.out.println("[SERVER]: Duration: "+durationSeconds+" seconds");
                            }
                            buffer.clear();

                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }

                    }
                    else if (key.isReadable()) {
                        SocketChannel socketChannel = (SocketChannel) key.channel();
                        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
                        int read = socketChannel.read(buffer);

                        if(read == -1) {
                            System.out.println("[SERVER] : connection closed");
                            key.cancel();
                        }
                        else {
                            buffer.flip();
                            int size = buffer.getInt();
                            buffer = ByteBuffer.allocate(size);
                            read = socketChannel.read(buffer);

                            if(read == -1) {
                                System.out.println("[SERVER] : connection closed");
                                key.cancel();
                            }
                            buffer.flip();
                            byte[] messageBytes = new byte[buffer.remaining()];
                            buffer.get(messageBytes);
                            String message = new String(messageBytes);
                            System.out.println("[SERVER] : recieved message: "+message+" to "+socketChannel.getRemoteAddress());

                            System.out.println("[SERVER] : Total connections received: "+connectionCount);
                            for(int i = 0;i<connectionCount;i++) {
                                System.out.println("[SERVER] : connection accepted "+connections[i]);
                            }

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
