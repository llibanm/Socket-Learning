package core_system_normal_socket;

import java.net.Socket;
import java.util.concurrent.atomic.AtomicInteger;

public class LaunchWorkerNonNIOSocket {
    private int count=3;
    private  Worker[] workers;
    private  Thread[] workerThreads;

    public LaunchWorkerNonNIOSocket() {
        workers = new Worker[count];
        workerThreads = new Thread[count];
    }

    public void run(){
        for(int i=0; i<count; i++){

            AtomicInteger workerID = new AtomicInteger(i);

            workers[i] = new Worker(workerID,(i+1)+".txt");
            workerThreads[i] = new Thread(workers[i]);
            workerThreads[i].start();

        }

        for(int i=0; i<count; i++){
            try {
                workerThreads[i].join();
            }catch(InterruptedException e){
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        LaunchWorkerNonNIOSocket socket = new LaunchWorkerNonNIOSocket();
        socket.run();
    }
}
