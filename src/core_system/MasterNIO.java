package core_system;



public class MasterNIO {

    private MyServerSelector serverChannel;
    private Thread serverThread;
    private final int port = 2000;


    private Worker[] workersTab;
    private Thread[] workerThreadsTab;

    public MasterNIO(int nbrWorkers) {
        this.workersTab = new Worker[nbrWorkers];
        this.workerThreadsTab = new Thread[nbrWorkers];
    }

    public void start() {
            serverChannel = new MyServerSelector(this.port);
            serverThread = new Thread(serverChannel);


        try {
            System.out.println("[MasterNIO] Lancement du serveur...");
            serverThread.start();
            serverThread.join();
            System.out.println("[MasterNIO] Lancement des workers");
            Thread.sleep(1);
            for (int i = 0; i < workersTab.length; i++) {
                workersTab[i] = new Worker(i++);
                workerThreadsTab[i] = new Thread(workersTab[i]);
                workerThreadsTab[i].start();
                workerThreadsTab[i].join();
            }
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

    public Worker[] getWorkersTab() {
        return workersTab;
    }

    public Thread[] getThreadsTab() {
        return workerThreadsTab;
    }

    public static void main(String[] args) {

        if(args.length != 1){
            System.out.println("Usage: java <Number of workers to instantiate>");
            System.exit(1);
        }

        MasterNIO nio = new MasterNIO(Integer.parseInt(args[0]));
        nio.start();

    }

}
