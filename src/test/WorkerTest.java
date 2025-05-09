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
import java.util.concurrent.atomic.AtomicBoolean;

public class WorkerTest {
    private static final String HOST = "localhost";
    private static final int PORT = 2000;

    private SocketChannel workerChannel;
    private Selector selectorWorker;
    private ByteBuffer byteBuffer;
    private AtomicBoolean cancelled = new AtomicBoolean(false);
    public WorkerTest() {}

    public void handleConnect(SelectionKey key) {

        SocketChannel channel = (SocketChannel) key.channel();

        try {
            if(channel.isConnectionPending()){
                channel.finishConnect();
            }
            System.out.println("[Server] Connected to " + channel.getRemoteAddress());

            channel.register(selectorWorker, SelectionKey.OP_READ );

        }catch(ConnectException e){
            System.out.println("[Worker] connection failed, could not find server");
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }

    }



    public void handleRead(SelectionKey key) {
       String s = recieveMessageWithLenght(key);
       if (s != null) {
           if(s.equals("QUIT")){
               key.cancel();
               System.out.println("[Worker] key cancelled");
               cancelled.set(true);
           }
       }

    }

    public String recieveMessageWithLenght(SelectionKey key) {

        try {
            ByteBuffer lenghtBuffer = ByteBuffer.allocate(Integer.BYTES);

            int bytesRead = 0;
            while(bytesRead < 4){
                int r = workerChannel.read(lenghtBuffer);
                if(r == -1){
                    System.out.println("[Worker] connection lost");
                    System.out.println("[Worker] 69");
                    cancelled.set(true);
                    return null;
                }
                bytesRead+=r;
            }

            lenghtBuffer.flip();

            int message_lenght = lenghtBuffer.getInt();

            ByteBuffer messageBuffer = ByteBuffer.allocate(message_lenght);

            bytesRead = 0;

            while(bytesRead < message_lenght){
                int r = workerChannel.read(messageBuffer);
                if(r == -1){
                    System.out.println("[Worker] connection lost");
                    System.out.println("[Worker] 89");
                    cancelled.set(true);
                    System.out.println("[Worker] cancelled: "+cancelled.get());
                    return null;
                }
                bytesRead+=r;
            }

            messageBuffer.flip();

            byte[] message_bytes = new byte[messageBuffer.remaining()];
            messageBuffer.get(message_bytes);

            String res = new String(message_bytes, StandardCharsets.UTF_8);

            System.out.println("[Worker] Received: " + res);
            System.out.println("[Worker] size of message: " + message_bytes.length);

            return res;

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

                    if (!cancelled.get()) {
                        if (key.isConnectable()){
                            handleConnect(key);
                        }
                        else if (key.isReadable()){
                            handleRead(key);
                        }
                        else if (key.isWritable()){

                        }
//                        System.out.println(" stop status: "+stopped.get());
                    }
                    else{
                        workerChannel.close();
                    }

                }
                if(cancelled.get()){
                    break;
                }
            }

        }catch (ConnectException e){
            System.out.println("[Worker] connection failed, could not find server");
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public static void main(String[] args) {
        WorkerTest test = new WorkerTest();
        test.run();
    }

    public void allocateBuffer(int size){
        byteBuffer = ByteBuffer.allocate(size);
    }
}
