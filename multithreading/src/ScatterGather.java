import java.util.*;
import java.util.concurrent.*;

/** Collect prices from multiple sites. Have a timeout for this and ignore slow sites.
 *
 */
public class ScatterGather {

    public static void main(String[] args){
        ScatterGather obj = new ScatterGather();
        try {
            Set<Integer> priceList = obj.getPrices();
            priceList.forEach(System.out::println);

            priceList = obj.getPricesWithCompletableFuture();
            priceList.forEach(System.out::println);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public Set<Integer> getPrices() throws InterruptedException {
        Set<Integer> prices = Collections.synchronizedSet(new HashSet<>());
        CountDownLatch latch = new CountDownLatch(3);

        ExecutorService executor = Executors.newFixedThreadPool(3);
        executor.execute(new Task(null, prices, latch, 1000));
        executor.execute(new Task(null, prices, latch, 2000));
        executor.execute(new Task(null, prices, latch, 5000));

        latch.await(3000, TimeUnit.MILLISECONDS);
        executor.shutdownNow();

        return prices;
    }

    public Set<Integer> getPricesWithCompletableFuture()  {
        Set<Integer> prices = Collections.synchronizedSet(new HashSet<>());

        CompletableFuture<Void> task1 = CompletableFuture.runAsync(new Task(null, prices, null, 1000));
        CompletableFuture<Void> task2 = CompletableFuture.runAsync(new Task(null, prices, null, 2000));
        CompletableFuture<Void> task3 = CompletableFuture.runAsync(new Task(null, prices, null, 5000));

        CompletableFuture<Void> allTasks = CompletableFuture.allOf(task1, task2, task3);
        try {
            allTasks.get(3000, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            // ignore the timeout exception and return price list
        }
        return prices;
    }

    class Task implements Runnable {
        private final int sleepTime;
        private final Set<Integer> prices;
        private final CountDownLatch taskDoneLatch;

        public Task(String url, Set<Integer> prices, CountDownLatch latch, int sleepTime){
            this.prices = prices;
            this.taskDoneLatch = latch;
            this.sleepTime = sleepTime;
        }

        public void run(){
            try {
                Thread.sleep(sleepTime);
                prices.add(5*sleepTime);
                if(taskDoneLatch != null)
                    taskDoneLatch.countDown();
            } catch (InterruptedException e) {
                //throw new RuntimeException(e);
            }


        }
    }

}
