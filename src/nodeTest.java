import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
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

    public nodeTest(int monPort, String address, int portAutre){
        this.intPort=monPort;
        this.addressAutrePort = address;
        this.intPortAutreNode = portAutre;
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
            Thread.sleep(2000);
            clientThread.start();

            serverThread.join();
            clientThread.join();

        }catch (IOException | InterruptedException e){
            e.printStackTrace();
        }
    }


    public void serverStart(){

        try{

            System.out.println("[SERVER] : server started on port "+this.intPort);
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
            System.out.println("[CLIENT] : client connectingt to "+this.addressAutrePort +":"+this.intPortAutreNode);
            clientPort = new Socket(addressAutrePort, intPortAutreNode);

            inClient = new BufferedReader(new InputStreamReader(clientPort.getInputStream()));
            outClient = new PrintWriter(clientPort.getOutputStream(), true);
            System.out.println("[CLIENT] : Client connected to "+this.addressAutrePort +":"+this.intPortAutreNode);

            System.out.println("[CLIENT] : Now ending connection");
            inClient.close();
            outClient.close();
            clientPort.close();

        }
        catch (IOException e){
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
