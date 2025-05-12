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

public class selectorWorkerTest implements Runnable {

    private int ID;

    public selectorWorkerTest(int ID) {
        this.ID = ID;
    }

    public void printMessage(String message) {
        System.out.println("[Worker: "+ID+"]: "+message);
    }

    @Override
    public void run() {
        try{
            Selector selectorWorker = Selector.open();
            SocketChannel socketChannelWorker = SocketChannel.open();
            socketChannelWorker.configureBlocking(false);

            printMessage("connection to server");

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
                            printMessage("ACCEPTED connection to server: "+socketChannelWorker.getRemoteAddress());
                        }

                        SocketChannel secondChannelWorker = (SocketChannel) key.channel();

                        secondChannelWorker.register(selectorWorker, SelectionKey.OP_READ);

                        printMessage("READ connection from: "+secondChannelWorker.getRemoteAddress());
                    }
                    else if (key.isReadable()) {
                        try {
                            ByteBuffer byteBuffer = ByteBuffer.allocate(Integer.BYTES);// will allocate the exact number of bytes necessary

                            int readBytes = socketChannelWorker.read(byteBuffer);
                            if(readBytes == -1){
                                printMessage("Read Failed");
                                printMessage("Connection closed");
                                key.cancel();
                                return;
                            }

                            byteBuffer.flip();
                            int size = byteBuffer.getInt();

                            byteBuffer =ByteBuffer.allocate(size); // re allocate the exact number of bytes necessary

                            readBytes = socketChannelWorker.read(byteBuffer);
                            if(readBytes == -1){
                                printMessage("Read Failed");
                                printMessage("Connection closed");
                                key.cancel();
                                return;
                            }
                            byteBuffer.flip();
                            byte[] message = new byte[byteBuffer.remaining()];
                            byteBuffer.get(message);
                            String messageString = new String(message, StandardCharsets.UTF_8);
                            printMessage("Received: " + messageString);
                            key.interestOps(SelectionKey.OP_WRITE);// changing read rights to write rights
                            printMessage("WRITE OK");
                        }catch (IOException e){
                            e.printStackTrace();}
                    }
                    else if (key.isWritable()) {
                        String message = "QUITTING";
                        byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);
                        int messageLength = messageBytes.length;

                        ByteBuffer byteBuffer = ByteBuffer.allocate(Integer.BYTES+messageLength);
                        byteBuffer.putInt(messageLength);
                        byteBuffer.put(messageBytes);
                        byteBuffer.flip();

                        try {
                            socketChannelWorker.write(byteBuffer);

                            if(!byteBuffer.hasRemaining()){
                                printMessage("message: "+message+" sent to server");
                                key.interestOps(SelectionKey.OP_READ);
                                key.cancel();
                                socketChannelWorker.close();
                                printMessage("Connection closed");
                                return;
                            }

                        }catch (IOException e){
                            key.cancel();
                            throw new RuntimeException();

                        }

                    }

                }
            }
        }catch (IOException e){
            throw new RuntimeException();
        }
    }

    public static void main(String[] args) {
        try {
            selectorWorkerTest test = new selectorWorkerTest(0);
            Thread thread = new Thread(test);
            thread.start();
            thread.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
