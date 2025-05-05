package core_system;



public class Master {

    private MyServerSocket serverChannel;
    private Thread myThread;
    public Master(int port) {
    }

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

    public static void main(String[] args) {
        if(args.length != 2) {
            System.out.println("[Server] Usage: Master <port>");
        }
        int port = Integer.parseInt(args[0]);

        Master master = new Master(port);
        master.start(port);
    }

    public MyServerSocket getServerChannel() {
        return serverChannel;
    }
}
