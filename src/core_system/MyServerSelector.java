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

public class MyServerSelector implements Runnable  {

    private ServerSocketChannel serverChannel;
    private Selector selector;
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

    private static void handleWrite(SelectionKey key) throws IOException {
        SocketChannel workerChannel = (SocketChannel) key.channel();

        ByteBuffer byteBuffer = (ByteBuffer) key.attachment();

        if(byteBuffer == null){
            String respone = "[Server] HELLO WORLD!";
            byteBuffer = ByteBuffer.wrap(respone.getBytes());
        }

        try{
            workerChannel.write(byteBuffer);

            if(!byteBuffer.hasRemaining()){
                System.out.println("[Server] Write Complete to " + workerChannel.getRemoteAddress());

                //switching key to readable by adding OP.READ and remving OP.Write
                key.interestOps(key.interestOps() | SelectionKey.OP_READ);
                key.interestOps(key.interestOps() & ~SelectionKey.OP_WRITE);


                key.attach(null);
            }
            else{
                System.out.println("[Server] Write Failed, remaning bytes: "+byteBuffer.remaining());
            }
        }catch (IOException e){
            System.out.println("[Server] Write Failed, error writing to worker ");
            workerChannel.close();
            key.cancel();
        }
//        catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }

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
                        handleRead(key);
                    } else if (key.isWritable()) {
                        handleWrite(key);
                    }
                    iterator.remove();
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
