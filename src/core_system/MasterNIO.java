package core_system;



public class MasterNIO {

    private MyServerSocket serverChannel;
    private Thread myThread;
    private final int port = 2000;
    public MasterNIO() {}

    public void start(int port) {
        try {
            serverChannel = new MyServerSocket(port);
            myThread = new Thread(serverChannel);
            myThread.start();
            myThread.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public int getPort(){
        return port;
    }

    public static void main(String[] args) {
        MasterNIO master = new MasterNIO();
        master.start(master.getPort());
    }

    public MyServerSocket getServerChannel() {
        return serverChannel;
    }
}
