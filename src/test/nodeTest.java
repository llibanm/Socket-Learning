package test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;

public class nodeTest {

    private Socket serverPort; //port du node qui va servir de server
    private int intPort;

    private Socket clientPort; // port qui va se connecter vers l'autre port
    private int intPortAutreNode;
    private BufferedReader inClient;
    private PrintWriter outClient;

    private String addressAutrePort;
    private ServerSocket server;
    private BufferedReader inServer;
    private PrintWriter outServer;

    //node attempt to connect to another node
    int attempts;
    int attemptsMax;
    boolean success;

    public nodeTest(int monPort, String address, int portAutre){
        this.intPort=monPort;
        this.addressAutrePort = address;
        this.intPortAutreNode = portAutre;

        attempts = 0;
        attemptsMax = 15;
        success = false;
    }

    public void demarrer(){
        try {
            //creation socket server
            this.server = new ServerSocket(intPort);
            System.out.println("[SERVER] : server started on port "+this.intPort);


            //lancement threads
            Thread serverThread = new Thread(this::serverStart);
            Thread clientThread = new Thread(this::clientStart);

            serverThread.start();
            clientThread.start();

            serverThread.join();
            clientThread.join();

        }catch (IOException | InterruptedException e){
            e.printStackTrace();
        }
    }


    public void serverStart(){

        try{

            System.out.println("[SERVER] : Awating incoming connection");
            this.serverPort = server.accept();

            inServer = new BufferedReader(new InputStreamReader(this.serverPort.getInputStream()));
            outServer = new PrintWriter(this.serverPort.getOutputStream(), true);

            System.out.println("[SERVER] : Connection established on port "+this.intPort+" by "+this.addressAutrePort);
            System.out.println("[SERVER] : Now ending connection");

            inServer.close();
            outServer.close();
            server.close();
            serverPort.close();

        }catch (IOException e){
            e.printStackTrace();
        }

    }

    public void clientStart(){
        try{
            System.out.println("[CLIENT] : core_system.client started on port "+this.intPort);
            System.out.println("[CLIENT] : core_system.client connecting to "+this.addressAutrePort +":"+this.intPortAutreNode);



            while(!success && attempts<attemptsMax){
                try {
                    clientPort = new Socket(addressAutrePort, intPortAutreNode);
                    success = true;
                    inClient = new BufferedReader(new InputStreamReader(clientPort.getInputStream()));
                    outClient = new PrintWriter(clientPort.getOutputStream(), true);
                    System.out.println("[CLIENT] : core_system.client connected to "+this.addressAutrePort +":"+this.intPortAutreNode);
                }catch (ConnectException e){
                    attempts++;
                    System.out.println("[CLIENT] : Attemps "+attempts+" failed out of  "+attemptsMax+" to core_system.client connect to "+this.addressAutrePort);

                    if(attempts<attemptsMax){
                        System.out.println("[CLIENT] : Launching another attempt in 1 seconds");
                        Thread.sleep(1000);
                    }


                }
            }

//            clientPort = new Socket(addressAutrePort, intPortAutreNode);
//

//            System.out.println("[CLIENT] : Client connected to "+this.addressAutrePort +":"+this.intPortAutreNode);
            if(!success){
                System.out.println("[CLIENT] : core_system.client failed to connect to "+this.addressAutrePort);
                System.out.println("[CLIENT] : Closing connection attemps");
                System.out.println("[CLIENT] : core_system.client exiting");

                return;
            }

            System.out.println("[CLIENT] : Now ending connection");
            inClient.close();
            outClient.close();
            clientPort.close();

        }
        catch (IOException | InterruptedException e){
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        // on a besoin de 3 arguments, 1 pour le port actuel, un autre qui va être "localhost" et un dernier qui va être le port de l'autre
        //node

        if(args.length!=3) {
            System.out.println("Usage : java nodeTest <port> <adressePair> <portPair>");
            return;
        }
        int monPort = Integer.parseInt(args[0]);
        String address = args[1];
        int portPair = Integer.parseInt(args[2]);

        nodeTest node = new nodeTest(monPort, address, portPair);
        node.demarrer();

    }


}
