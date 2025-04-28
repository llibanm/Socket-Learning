package Shuffle;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;

public class node {



    private int Port; // le port serverSocket du node actuel
    private int PortPair; // le port serverSocket du node pair

    private String adressePaire;

    private ServerSocket serverSocket;

    private Socket socketClient;
    private Socket socketServer;

    private BufferedReader inServer;
    private PrintWriter outServer;

    private BufferedReader inClient;
    private PrintWriter outClient;

    public node(int monPort,String adressePaire, int portPair) {
        this.Port = portPair;
        this.adressePaire = adressePaire;
        this.PortPair = portPair;
    }

    public void demarrer() {
        try {
            //creation su socket server
            serverSocket = new ServerSocket(this.Port);
            System.out.println("[CLIENT] Server started on port " + this.Port);

            //lancement de threads, un pour client et un pour server
            Thread serverThread = new Thread(this::fonctionServer);
            Thread clientThread = new Thread(this::fonctionClient);

            serverThread.start();

            Thread.sleep(2000);
            clientThread.start();

            serverThread.join();
            clientThread.join();

        }
        catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void fonctionServer() {
        try {
            System.out.println("[SERVER]Waiting for client to connect...");
            socketServer = serverSocket.accept();

            System.out.println("[SERVER] Client connected");

            inServer = new BufferedReader(new InputStreamReader(socketServer.getInputStream()));
            outServer = new PrintWriter(socketServer.getOutputStream(), true);

            outServer.println("[SERVER] Hello from server "+this.Port);

            String message = inServer.readLine();
            System.out.println("[SERVER] Server received: "+message);

            message = inServer.readLine();
            System.out.println("[SERVER] Server received: "+message);

            inServer.close();
            outServer.close();
            socketServer.close();
            serverSocket.close();

        }catch (IOException e){
            e.printStackTrace();
        }
    }

    private void fonctionClient(){
        try{
            boolean connected = false;
            System.out.println("[CLIENT] attempt to connect to "+this.adressePaire+" : "+this.Port);

            try {
                socketClient = new Socket(this.adressePaire, this.Port);
                connected = true;
                System.out.println("[CLIENT] Client connected to "+this.adressePaire+" : "+this.Port);
            } catch (IOException e) {
                System.out.println("Failed to connect to "+this.adressePaire+" : "+this.Port);
            }

            if(!connected){
                System.out.println("[CLIENT] Client attempt failed");
                return;
            }

            inClient = new BufferedReader(new InputStreamReader(socketClient.getInputStream()));
            outClient = new PrintWriter(socketClient.getOutputStream(), true);

            outClient.println("[CLIENT] Hello from client "+this.Port);

            String message = inClient.readLine();
            System.out.println("[CLIENT] Client received: "+message);

            outClient.println("[CLIENT] end of connexion");

            inClient.close();
            outClient.close();
            socketClient.close();

        }
        catch(IOException e){
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        if(args.length!=3) {
            System.out.println("Usage: java Shuffle/node <Port> <adressePair> <portPair>");
            return;
        }

        int monPort =Integer.parseInt(args[0]);
        String adressePaire = args[1];
        int portPair = Integer.parseInt(args[2]);

        node m = new node(monPort,adressePaire,portPair);
        m.demarrer();

    }

}
