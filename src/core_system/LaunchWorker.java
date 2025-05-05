package core_system;

import java.io.IOException;

public class LaunchWorker {

    private Worker[] workersTab;
    private Thread[] workerThreadsTab;

    public LaunchWorker(int nbrWorkers) {
        workersTab = new Worker[nbrWorkers];
        workerThreadsTab = new Thread[nbrWorkers];
    }

    public void start(){
        try {
            for(int i = 0; i < workersTab.length; i++){
                workersTab[i] = new Worker(i);
                workerThreadsTab[i] = new Thread(workersTab[i]);
                workerThreadsTab[i].start();
            }
            for(int i = 0; i < workerThreadsTab.length; i++){
                workerThreadsTab[i].join();
            }
        }catch ( InterruptedException e){
            e.printStackTrace();
        }
    }

    public Worker[] getWorkersTab() {
        return workersTab;
    }

    public Thread[] getWorkerThreadsTab() {
        return workerThreadsTab;
    }

    public static void main(String[] args){
        if(args.length != 1){
            System.out.println("Usage: java MasterNIO <Number of worker>");
            return;
        }
        LaunchWorker launchWorker = new LaunchWorker(Integer.parseInt(args[0]));
        launchWorker.start();
    }
}
