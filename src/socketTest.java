import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class socketTest {
    public static void main(String[] args) {

        try( Socket socket = new Socket("localhost",9999)){
            PrintWriter out = new PrintWriter(socket.getOutputStream(),true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            out.println("hello server");
            System.out.println("[Client] sent : hello server");
            System.out.println("[Client] Server response: " + in.readLine());
            socket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
