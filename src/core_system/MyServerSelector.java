package core_system;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class MyServerSelector implements Runnable {

    private ServerSocketChannel serverChannel;
    private Selector selector;
    private int port;
    private Set<SocketChannel> newConnections;

    public MyServerSelector(int port) {
        this.port = port;
        newConnections = new HashSet<SocketChannel>();
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

//        String message ="echo "+output;
//        ByteBuffer writeBuffer = ByteBuffer.wrap(message.getBytes());
//        clientChannel.write(writeBuffer);
    }

    @Override
    public void run() {
        try {
            selector = Selector.open();

            serverChannel = ServerSocketChannel.open();
            serverChannel.configureBlocking(false);
            serverChannel.socket().bind(new InetSocketAddress("localhost", port));

            serverChannel.register(selector, SelectionKey.OP_ACCEPT);

            System.out.println("[Server] Started on port: " + port);
            System.out.println("[Server] Listening on port: " + port);

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

        }catch (IOException e){
            throw new RuntimeException(e);
        }

    }


}
