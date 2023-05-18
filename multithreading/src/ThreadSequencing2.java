/**
 * Salesforce: The first thread prints 1 1 1 ..., the second one prints 2 2 2 ..., and the third one prints 3 3 3 ...
 * Schedule these 3 threads in order to print 1 2 3 1 2 3 ...
 */

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class ThreadSequencing2 implements Runnable{
    public static int NUM_ITERATIONS = 10;
    private int threadId;
    private Semaphore currSem, nextSem;

    public ThreadSequencing2(int num, Semaphore curr, Semaphore next){
        this.threadId = num;
        this.currSem = curr;
        this.nextSem = next;
    }

    public void run() {
        for(int i=0; i<NUM_ITERATIONS; i++) {
            try {
                currSem.acquire();
                System.out.print(this.threadId + " ");
                nextSem.release();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

        }
    }

    public static void main(String[] args){
        ExecutorService executorService = Executors.newFixedThreadPool(5);
        Semaphore sem1 = new Semaphore(1);
        Semaphore sem2 = new Semaphore(1);
        Semaphore sem3 = new Semaphore(1);
        ThreadSequencing2 t1 = new ThreadSequencing2(1, sem1, sem2);
        ThreadSequencing2 t2 = new ThreadSequencing2(2, sem2, sem3);
        ThreadSequencing2 t3 = new ThreadSequencing2(3, sem3, sem1);

        try {
            sem1.acquire();
            sem2.acquire();
            sem3.acquire();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        executorService.submit(t1);
        executorService.submit(t2);
        executorService.submit(t3);

        sem1.release();
        executorService.shutdown();
    }
}
