package core_system_normal_socket;

public class LaunchWorkerNonNIOSocket2GB {
    private int count=3;
    private  Worker2GB[] worker2GBS;
    private  Thread[] workerThreads;

    public LaunchWorkerNonNIOSocket2GB() {
        worker2GBS = new Worker2GB[count];
        workerThreads = new Thread[count];
    }

    public void run(){
        int i=0;
        while(i<count) {

            worker2GBS[i] = new Worker2GB(i,i+".txt");
            workerThreads[i] = new Thread(worker2GBS[i]);
            workerThreads[i].start();
            i++;
        }

        for(i=0; i<count; i++){
            try {
                workerThreads[i].join();
            }catch(InterruptedException e){
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        LaunchWorkerNonNIOSocket2GB socket = new LaunchWorkerNonNIOSocket2GB();
        socket.run();
    }
}