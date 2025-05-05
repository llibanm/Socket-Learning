package core_system;



public class MasterNIO {

    private MyServerSelector serverChannel;
    private Thread serverThread;
    private final int port = 2000;




    public MasterNIO() {}

    public void start() {
            serverChannel = new MyServerSelector(this.port);
            serverThread = new Thread(serverChannel);


        try {
            System.out.println("[MasterNIO] Lancement du serveur...");
            serverThread.start();
            serverThread.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public int getPort(){
        return port;
    }

    public MyServerSelector getServerChannel() {
        return serverChannel;
    }


    public static void main(String[] args) {

        MasterNIO nio = new MasterNIO();
        nio.start();

    }

}
