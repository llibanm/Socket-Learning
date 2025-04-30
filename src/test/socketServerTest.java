package test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class socketServerTest {
    public static void main(String[] args) {
        try(ServerSocket serverSocket = new ServerSocket(9999)) {
            System.out.println("Server is listening on port 99");
            Socket socket = serverSocket.accept();
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            String line = in.readLine();
            System.out.println("[Server] Recieved : "+line);
            out.println("Hello from server");
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
