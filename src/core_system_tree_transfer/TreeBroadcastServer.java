package core_system_tree_transfer;

public class TreeBroadcastServer {

    private static final int SERVER_PORT = 9000;
    private static final int[] WORKER_PORTS = {9001,9002,9003};
    private static final String FILE_100MB = "/home/vazek/Documents/internship_document/random_text_100MB.txt";
    private static final String FILE_2GB = "/home/vazek/Documents/internship_document/random_data_text_file.txt";
    private static final long MB = 1024 * 1024;

    public static void main(String[] args) {


        try {


            System.out.println("Starting workers...");
            //startWorker();

            Thread.sleep(1000);


            //perfomTreeBroadcast(FILE_100MB)

            System.out.println("Broadcasting done. Shutting down...");
            System.exit(0);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static void startWorkers(){
        System.out.println("Starting workers...");
        for (int i = 0; i < WORKER_PORTS.length; i++) {
            final int workerIndex = i;
//            new Thread(() -> {
//
//            })
        }
    };

}
