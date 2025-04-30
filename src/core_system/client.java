package core_system;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class client {

    private int Id;
    private Socket socket; // to connect itself to core_system.myServerSocket
    private int Port;

    private BufferedReader in;
    private PrintWriter out;



    public client(int port) {
        this.Port = port;
    }

    public void start() {

        try {
            System.out.println("[Worker] Engaging connection...");
            this.socket = new Socket("localhost", this.Port);
            System.out.println("[Worker] Connection established on address : " + this.socket.getInetAddress().getHostAddress()+" and port : "+this.socket.getPort());

            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);


            String workerInput;
            String serverOutput;


           while(true) {


               switch(workerInput) {
                   case "QUIT","EXIT":
                            System.out.println("[Worker] Notifying server...");
                            out.println(workerInput);
                            System.out.println("[Worker] Server notified");
                            System.out.println("[Worker] Waiting for response ...");
                            serverOutput = in.readLine();
                            System.out.println("[Worker] server output : "+serverOutput);

                       if (serverOutput.equals("EXIT")||serverOutput.equals("QUIT")) {
                           System.out.println("[Worker] Now quitting connection...");
                           in.close();
                           out.close();
                           socket.close();
                           break;
                       }
                   default:
                       System.out.println("[Worker] Invalid input, try help to know available commands");
                       break;
               }
               if("Exit".equalsIgnoreCase(workerInput)||"Quit".equalsIgnoreCase(workerInput)) {
                   break;
               }

           }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }
//
//    public void availableCommands(){
//        System.out.println("""
//                [CLIENT] Available commands:
//                "help" : show all available commands.
//                "quit" or "exit" : quit server.
//                """);
//    }

    public static void main(String[] args) {
        if(args.length != 1) {
            System.out.println("Usage: core_system.client <port>");
            return;
        }
        int port = Integer.parseInt(args[0]);
        client client = new client(port);
        client.start();
    }


}
