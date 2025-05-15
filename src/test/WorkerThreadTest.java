package test;

public class WorkerThreadTest {

    private selectorWorkerTest[] workers;
    private Thread[] workerThreads;

    public WorkerThreadTest() {
        workers = new selectorWorkerTest[3];
        workerThreads = new Thread[workers.length];
    }

    public void start() {
        try {
            for (int i = 0; i < workers.length; i++) {
                String filePathLastNumberIdentifier= String.valueOf(i+1)+".txt";
                workers[i] = new selectorWorkerTest(i,filePathLastNumberIdentifier);
                workerThreads[i] = new Thread(workers[i]);
                workerThreads[i].start();
            }


            for (int i = 0; i < workerThreads.length; i++) {
                workerThreads[i].join();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        WorkerThreadTest test = new WorkerThreadTest();
        test.start();
    }


    public Thread[] getWorkerThreads() {
        return workerThreads;
    }

    public selectorWorkerTest[] getWorkers() {
        return workers;
    }




}
