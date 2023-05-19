/**
 * Design an algorithm whereby either an Uber ride can have all democrats or republicans or 2 Dems and 2 Republicans.
 * Model the ride requestors as threads. When all the threads are seated,
 * any one of the four threads can invoke the method drive()
 */

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadUberRide2 {
    public static AtomicInteger numDemocrats = new AtomicInteger(0);

    public static AtomicInteger numRepublicans = new AtomicInteger(0);

    public synchronized void seatRepublican(){
        while(  numDemocrats.get() > 2 ||                                   // dem=3, wait for one more democrat
                (numDemocrats.get() > 0 && numRepublicans.get() > 1) ||     // rep=2, dem=1, wait for one more democrat
                (numDemocrats.get() == 0 && numRepublicans.get() == 4)){    // rep=4, wait for drive to start
            try{
                wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        System.out.println("Seated republican");
        int n = numRepublicans.incrementAndGet();
        if( n == 4 || (n==2 && numDemocrats.get() ==2)){
            drive();
        }
        notifyAll();
    }

    public synchronized void seatDemocrat(){
        while(  numRepublicans.get() > 2 ||                               // rep=3, need one more republican
                (numRepublicans.get() > 0 && numDemocrats.get() > 1) ||   // dem=2, rep=1, need one more republican
                (numRepublicans.get() == 0 && numDemocrats.get() == 4)){  // dem=4, wait for drive to start
            try{
                wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        int n = numDemocrats.incrementAndGet();
        System.out.println("Seated democrat");
        if( n == 4 || (n==2 && numRepublicans.get() ==2)){
            drive();
        }
        notifyAll();
    }

    public synchronized void drive(){
        System.out.println("Starting drive");
        numDemocrats.set(0);
        numRepublicans.set(0);
    }

    public static void main(String[] args){
        ExecutorService executorService = Executors.newFixedThreadPool(5);
        ThreadUberRide2 t = new ThreadUberRide2();

        for(int i=0; i<10 ;i++) {
            Runnable r1 = () -> {
                t.seatDemocrat();
            };
            executorService.submit(r1);
            
            Runnable r2 = () -> {
                t.seatRepublican();
            };
            executorService.submit(r2);
        }

        try {
            executorService.awaitTermination(10, TimeUnit.SECONDS);
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
