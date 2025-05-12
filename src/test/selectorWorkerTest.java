package test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;

public class selectorWorkerTest {



    public static void main(String[] args) throws IOException {

        Selector selectorWorker = Selector.open();
        SocketChannel socketChannelWorker = SocketChannel.open();
        socketChannelWorker.configureBlocking(false);

        System.out.println("[WORKER] : connection to server");

        socketChannelWorker.connect(new InetSocketAddress("localhost",2000)); // connection to localhost server on port 2000
        socketChannelWorker.register(selectorWorker, SelectionKey.OP_CONNECT); //



        while (true) { // tant que la connection est etablie

            int ready = selectorWorker.select();
            if(ready == 0){
                continue;
            }



            Set<SelectionKey> keys = selectorWorker.selectedKeys();
            Iterator<SelectionKey> iterator = keys.iterator();

            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                iterator.remove();

                    if (key.isConnectable()) {

                        if(socketChannelWorker.finishConnect()){
                            System.out.println("[WORKER] : ACCEPTED connection to server: "+socketChannelWorker.getRemoteAddress());
                        }

                        SocketChannel secondChannelWorker = (SocketChannel) key.channel();

                        secondChannelWorker.register(selectorWorker, SelectionKey.OP_READ);

                        System.out.println("[WORKER] : READ connection from: "+secondChannelWorker.getRemoteAddress());
                    }
                    else if (key.isReadable()) {
                        try {
                            ByteBuffer byteBuffer = ByteBuffer.allocate(Integer.BYTES);// will allocate the exact number of bytes necessary

                            int readBytes = socketChannelWorker.read(byteBuffer);
                            if(readBytes == -1){
                                System.out.println("[WORKER] : Read Failed");
                                System.out.println("[WORKER] : Connection closed");
                                key.cancel();
                                return;
                            }

                            byteBuffer.flip();
                            int size = byteBuffer.getInt();

                            byteBuffer =ByteBuffer.allocate(size); // re allocate the exact number of bytes necessary

                            readBytes = socketChannelWorker.read(byteBuffer);
                            if(readBytes == -1){
                                System.out.println("[WORKER] : Read Failed");
                                System.out.println("[WORKER] : Connection closed");
                                key.cancel();
                                return;
                            }
                            byteBuffer.flip();
                            byte[] message = new byte[byteBuffer.remaining()];
                            byteBuffer.get(message);
                            String messageString = new String(message, StandardCharsets.UTF_8);
                            System.out.println("[WORKER] : Received: " + messageString);
                            key.interestOps(SelectionKey.OP_WRITE);// changing read rights to write rights
                            System.out.println("[WORKER] : WRITE OK");
                        }catch (IOException e){
                            e.printStackTrace();}
                    }
                    else if (key.isWritable()) {

                    }

            }
        }

    }

}
