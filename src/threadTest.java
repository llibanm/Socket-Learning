import java.io.IOException;

public class threadTest implements Runnable {
    @Override
    public void run() {
        System.out.println("[Thread]"+
                Thread.currentThread().getName()+
                ": thread started");
    }

    public static void main(String[] args) throws IOException {
        Thread thread = new Thread(new threadTest());
        thread.start();
    }
}
