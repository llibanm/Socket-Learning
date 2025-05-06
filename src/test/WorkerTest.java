package test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;

public class WorkerTest {
    private static final String HOST = "localhost";
    private static final int PORT = 2000;

    private SocketChannel workerChannel;
    private Selector selectorWorker;
    private ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
    public WorkerTest() {}

    public void handleConnect(SelectionKey key) {

        SocketChannel channel = (SocketChannel) key.channel();

        try {
            if(channel.isConnectionPending()){
                channel.finishConnect();
            }
            System.out.println("[Server] Connected to " + channel.getRemoteAddress());

            channel.register(selectorWorker, SelectionKey.OP_READ | SelectionKey.OP_WRITE);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public void handleRead(SelectionKey key) {
        SocketChannel channel = (SocketChannel) key.channel();
        byteBuffer.clear();

        try {
            int bytesRead = channel.read(byteBuffer);

            if(bytesRead == -1) {
                System.out.println("[Server] Read Failed");
                channel.close();
                key.cancel();
                return;
            }

            byteBuffer.flip();

            byte[] bytes = new byte[byteBuffer.remaining()];
            byteBuffer.get(bytes);
            String message = new String(bytes, StandardCharsets.UTF_8);

            System.out.println("[Server] " + message);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void run(){

        try {
            workerChannel = SocketChannel.open();
            workerChannel.configureBlocking(false);

            selectorWorker = Selector.open();

            workerChannel.connect(new InetSocketAddress(HOST, PORT));

            workerChannel.register(selectorWorker, SelectionKey.OP_CONNECT);

            System.out.println("[Worker] Connection to server: " + HOST + ":" + PORT);

            while (true){
                selectorWorker.select();

                Iterator<SelectionKey> iterator = selectorWorker.selectedKeys().iterator();

                while (iterator.hasNext()){
                    SelectionKey key = iterator.next();

                    iterator.remove();

                    if(!key.isValid()){
                        continue;
                    }

                    if (key.isConnectable()){
                        handleConnect(key);
                    }
                    if (key.isReadable()){
                        handleRead(key);
                    }
                    if (key.isWritable()){

                    }
                }
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public static void main(String[] args) {
        WorkerTest test = new WorkerTest();
        test.run();
    }

}
