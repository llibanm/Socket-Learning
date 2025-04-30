import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class client {

    private Socket socket; // to connect itself to myServerSocket
    private int Port;

    private BufferedReader in;
    private PrintWriter out;

    private Scanner input; // for user input


    public client(int port) {
        this.Port = port;
    }

    public void start() {

        try {
            System.out.println("[CLIENT] Engaging connection...");
            this.socket = new Socket("localhost", this.Port);
            System.out.println("[CLIENT] Connection established on address : " + this.socket.getInetAddress().getHostAddress()+" and port : "+this.socket.getPort());

            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            input = new Scanner(System.in);
            String userInput;
            String serverOutput;


           while(true) {

               System.out.println("[CLIENT] Input a command : ");
               userInput = input.nextLine();
               userInput = userInput.toUpperCase();

               switch(userInput) {
                   case "QUIT","EXIT":
                            System.out.println("[CLIENT] Notifying server...");
                            out.println(userInput);
                            System.out.println("[CLIENT] Server notified");
                            System.out.println("[CLIENT] Waiting for response ...");
                            serverOutput = in.readLine();
                            System.out.println("[CLIENT] server output : "+serverOutput);

                       if (serverOutput.equals("EXIT")||serverOutput.equals("QUIT")) {
                           System.out.println("[CLIENT] Now quitting connection...");
                           in.close();
                           out.close();
                           socket.close();
                           break;
                       }
                   case "HELP":
                       availableCommands();
                       break;
                   default:
                       System.out.println("[CLIENT] Invalid input, try help to know available commands");
                       break;
               }
               if("Exit".equalsIgnoreCase(userInput)||"Quit".equalsIgnoreCase(userInput)) {
                   break;
               }

           }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }

    public void availableCommands(){
        System.out.println("""
                [CLIENT] Available commands:
                "help" : show all available commands.
                "quit" or "exit" : quit server.
                """);
    }

    public static void main(String[] args) {
        if(args.length != 1) {
            System.out.println("Usage: client <port>");
            return;
        }
        int port = Integer.parseInt(args[0]);
        client client = new client(port);
        client.start();
    }


}
