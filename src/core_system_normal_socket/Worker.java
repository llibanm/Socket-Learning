package core_system_normal_socket;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.*;

public class Worker {
    private static final int BUFFER_SIZE = 8192;
    private static String filePathToWrite;
    private static final String serverAddress="localhost";
    private static final int serverPort=8080;
    public static void main(String[] args) {



        int port = 8080;

        try {
            SocketChannel socketChannel = SocketChannel.open();
            socketChannel.connect(new InetSocketAddress(serverAddress, port));
            socketChannel.configureBlocking(true);
            System.out.println("Connected to server at " + serverAddress + ":" + port);

            // TODO: Implement logic to receive data and write to file

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}