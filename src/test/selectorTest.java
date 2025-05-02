package test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class selectorTest {


    public static void main(String[] args) {
        try {
            //create new selector
            Selector selector = Selector.open();


            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.socket().bind(new InetSocketAddress("localhost", 8080));

            //non-blocking mode
            serverSocketChannel.configureBlocking(false);

            //register with selector, interest in incoming connection
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

            System.out.println("[Server] Started on port 8080");
            System.out.println("[Server] Listening on port 8080");

            while (true) {
                int readyChannels = selector.select();

                if (readyChannels == 0) {
                continue;
                }

                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectionKeys.iterator();

                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    if (key.isAcceptable()) {
                        handleAccept(key, selector);
                    }
                    else if (key.isReadable()) {
                        handleRead(key);
                    }
                    iterator.remove();
                }

            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static void handleAccept(SelectionKey key, Selector selector) throws IOException {
        //accpet connection
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
        SocketChannel socketChannel = serverSocketChannel.accept();
        socketChannel.configureBlocking(false);

        // register new client channel for reading
        socketChannel.register(selector, SelectionKey.OP_READ);
        System.out.println("[Server] Accepted connection from " + socketChannel.getRemoteAddress());
    }

    private static void handleRead(SelectionKey key) throws IOException {
        //accept conenction
        SocketChannel clientChannel = (SocketChannel) key.channel();
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);

        int BytesRead=0;

        try {
            BytesRead = clientChannel.read(byteBuffer);
        }catch (IOException e){
            System.out.println("[Server] Read Failed");
            System.out.println("[Server] connection closed");
            clientChannel.close();
            key.cancel();
            e.printStackTrace();
        }

        if(BytesRead == -1){
            System.out.println("[Server] Connection closed");
            clientChannel.close();
            key.cancel();
            return;
        }
        byteBuffer.flip();
        byte[] data = new byte[byteBuffer.limit()];
        byteBuffer.get(data);

        String output = new String(data);

        System.out.println("[Server] Received: " + output+" from "+clientChannel.getRemoteAddress());

        String message ="echo "+output;
        ByteBuffer writeBuffer = ByteBuffer.wrap(message.getBytes());
        clientChannel.write(writeBuffer);
    }
}
