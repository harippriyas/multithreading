/**
 * Goldman-sachs: Print series 010203040506.
 * Using multi-threading 1st thread will print only 0.
 * 2nd thread will print only even numbers and 3rd thread print only odd numbers.
 */

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadSequencing4 implements Runnable{
    public static int NUM_ITERATIONS = 10;

    public static AtomicInteger counter = new AtomicInteger(1);

    public static Semaphore zeroSem = new Semaphore(1);
    public static Semaphore numSem = new Semaphore(1);
    private int threadId;
    public ThreadSequencing4(int num){
        this.threadId = num;
    }

    public void run() {
        for(int i=0; i<NUM_ITERATIONS; i++) {
            try {
                if(threadId == 0) {
                    zeroSem.acquire();
                    System.out.print("0");
                    numSem.release();
                }
                else {
                    numSem.acquire();
                    System.out.print(counter.getAndIncrement());
                    zeroSem.release();
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

        }
    }

    public static void main(String[] args){
        ExecutorService executorService = Executors.newFixedThreadPool(5);
        ThreadSequencing4 t1 = new ThreadSequencing4(0);
        ThreadSequencing4 t2 = new ThreadSequencing4(1);

        try {
            zeroSem.acquire();
            numSem.acquire();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        executorService.submit(t1);
        executorService.submit(t2);

        zeroSem.release();
        try {
            executorService.awaitTermination(5, TimeUnit.SECONDS);
            System.out.println();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        finally {
            System.out.println("Shutting down executor");
            executorService.shutdownNow();
        }

    }



}
