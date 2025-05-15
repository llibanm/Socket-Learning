package core_system_selector_nio;



public class MasterNIO {

    private MyServerSelector100MBTransfert serverChannel;
    private Thread serverThread;
    private final int port = 2000;




    public MasterNIO() {}

    public void start() {
            serverChannel = new MyServerSelector100MBTransfert(this.port);
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

    public MyServerSelector100MBTransfert getServerChannel() {
        return serverChannel;
    }


    public static void main(String[] args) {

        MasterNIO nio = new MasterNIO();
        nio.start();

    }

}
