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
    public static void main(String[] args) {
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

                }


            }


        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
