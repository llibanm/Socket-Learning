package test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
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

    public void run() throws IOException {
        String randomDataTextFilePAth="/home/vazek/Documents/internship document/random_data_text_file.txt";
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
                        SocketChannel socketChannel = (SocketChannel) key.channel();
                        String message = "Hello Worker";

                        byte[] messageBytes = message.getBytes();
                        int messageLength = messageBytes.length;

                        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES+messageLength);// allocate the exact number of byte needed
                        buffer.putInt(messageLength);
                        buffer.put(messageBytes);
                        buffer.flip();

                        try {
                            socketChannel.write(buffer);

                            if(!buffer.hasRemaining()) {
                                System.out.println("[SERVER] : send data: "+message+" to "+socketChannel.getRemoteAddress());
                                key.interestOps(SelectionKey.OP_READ);// change write rights to read rights
                            }

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
                            for(int i = 0;i<connections.length;i++) {
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
