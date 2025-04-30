package test;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
      for(int i = 0;i < 10;i++) {
          final int num = i;
          Thread t = new Thread(()->{
              System.out.println("Thread: " + num + " is running");
          });
          t.setName("Thread " + num);
          t.start();
      }
    }
}