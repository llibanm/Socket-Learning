package core_system;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class MyServerSelector implements Runnable  {

    private ServerSocketChannel serverChannel;
    private Selector selector;
    private ByteBuffer buffer;
    private AtomicBoolean stop = new AtomicBoolean(false);
    private int port;



    public MyServerSelector(int port) {
        this.port = port;
    }


    private static void handleAccept(SelectionKey key, Selector selector) throws IOException {
        //accpet connection
        SocketChannel socketChannel;
        try (ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel()) {
            socketChannel = serverSocketChannel.accept();
        }
        socketChannel.configureBlocking(false);

        // register new client channel for reading
        //socketChannel.register(selector, SelectionKey.OP_READ);
        socketChannel.register(selector, SelectionKey.OP_WRITE);
        System.out.println("[Server] Accepted connection from " + socketChannel.getRemoteAddress());
    }

    private static void handleRead(SelectionKey key) throws IOException {
        //accept conenction
        SocketChannel clientChannel = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);

        int BytesRead=0;

        try {
            BytesRead = clientChannel.read(buffer);
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
        buffer.flip();
        byte[] data = new byte[buffer.limit()];
        buffer.get(data);

        String output = new String(data);

        System.out.println("[Server] Received: " + output+" from "+clientChannel.getRemoteAddress());


//        String message ="echo "+output;
//        ByteBuffer writeBuffer = ByteBuffer.wrap(message.getBytes());
//        clientChannel.write(writeBuffer);
    }



    public void handleWrite(SelectionKey key) throws IOException {
        try {
            sendMessageWithLenght(key,"Hello World!");
//        key.interestOps(key.interestOps() | SelectionKey.OP_READ);
//        key.interestOps(key.interestOps() & ~SelectionKey.OP_WRITE);
            Thread.sleep(3000);
            sendMessageWithLenght(key,"QUIT");
            key.interestOps(key.interestOps() | SelectionKey.OP_READ);
            key.interestOps(key.interestOps() & ~SelectionKey.OP_WRITE);
            Thread.sleep(1000);
            stop.set(true);

//            int readykeys = selector.select();
//
//            if (readykeys > 0) {
//                Thread.sleep(3000);
//                readykeys = selector.select();
//            }

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }



    public void sendMessageWithLenght(SelectionKey key, String message) {
        //méthode en-tête + message
        SocketChannel serverSocketChannel = (SocketChannel) key.channel();

        byte[] messageBytes = message.getBytes();
        int messageLength = messageBytes.length;
        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES+messageLength);

        buffer.putInt(messageLength);

        buffer.put(messageBytes);

        buffer.flip();

        try {

                serverSocketChannel.write(buffer);

            if (!buffer.hasRemaining()) {
                System.out.println("[Server] Sent " + message + " to : " + serverSocketChannel.getRemoteAddress());
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public void stop(){

        if(selector != null){
            selector.wakeup();
        }

        try {
            if(selector != null){
                for(SelectionKey key : selector.keys()){
                    try {
                        key.channel().close();
                    }catch (IOException e){
                        e.printStackTrace();
                    }
                }
                selector.close();
            }

            if(serverChannel != null){
                serverChannel.close();
            }

        }catch (IOException e){
            e.printStackTrace();
        }

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

            while (true) {// à enlever
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
//                        handleRead(key);
                    } else if (key.isWritable()) {
                        handleWrite(key);
                    }
                    iterator.remove();
                }
                if(stop.get()){
                    break;
                }


            }

        }catch (IOException e){
            throw new RuntimeException(e);
        }
//        catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }

    }


}
