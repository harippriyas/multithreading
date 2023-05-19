/**
 * There are five philosopher's sitting on a roundtable. They have only five forks between themselves
 * to eat their food with. Each philosopher requires both the left and the right fork to eat his food.
 * Design a solution where each philosopher gets a chance to eat his food without causing a deadlock
 */

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadDiningPhilosophers {
    public static Semaphore diningPhils = new Semaphore(4);

    public static Semaphore[] forks = new Semaphore[5];

    public void dine(int id){
        Semaphore rightFork = forks[id];
        Semaphore leftFork = forks[(id+4)%5];
        try {
            System.out.println("Professor " + id + " is waiting");
            diningPhils.acquire();

            rightFork.acquire();
            leftFork.acquire();
            Thread.sleep(20);
            System.out.println("Professor " + id + " has finished dining");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        finally {
            rightFork.release();
            leftFork.release();
            diningPhils.release();

        }

    }

    public static void main(String[] args){
        ExecutorService executorService = Executors.newFixedThreadPool(5);
        ThreadDiningPhilosophers t = new ThreadDiningPhilosophers();
        AtomicInteger id = new AtomicInteger(0);
        for(int i=0; i<5 ;i++) {
            forks[i] = new Semaphore(1);
        }
        for(int i=0; i<5 ;i++) {
            Runnable r1 = () -> {
                t.dine(id.getAndIncrement());
            };
            executorService.submit(r1);
        }
        executorService.shutdown();
    }



}
