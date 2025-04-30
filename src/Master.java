import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;

public class Master {

    private myServerSocket[] myServerSocketsArray; // each socket will be a server
    private Thread[] myThreadArray; // each socket will have its own thread to be executed on


    public Master(int numberOfServers) {
        myServerSocketsArray = new myServerSocket[numberOfServers];
        myThreadArray = new Thread[numberOfServers];
    }

    public void startServer() {
        try {
            for (int i = 0; i < myServerSocketsArray.length; i++) {
                myServerSocketsArray[i] = new myServerSocket(i,2000+i,"localhost");
                myThreadArray[i] = new Thread((Runnable) myServerSocketsArray[i]);
                myThreadArray[i].start();
            }

            for (int i = 0; i < myThreadArray.length; i++) {
                myThreadArray[i].join();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public myServerSocket[] getMyServerSocketsArray() {
        return myServerSocketsArray;
    }

    public Thread[] getMyThreadArray() {
        return myThreadArray;
    }

    public static void main(String[] args) {
        Master master = new Master(5);
        master.startServer();
    }
}
