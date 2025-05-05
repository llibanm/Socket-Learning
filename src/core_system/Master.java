package core_system;



public class Master {

    private MyServerSocket serverChannel;
    private Thread myThread;
    private final int port = 2000;
    public Master() {}

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
        Master master = new Master();
        master.start(master.getPort());
    }

    public MyServerSocket getServerChannel() {
        return serverChannel;
    }
}
