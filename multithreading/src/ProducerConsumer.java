import java.util.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class ProducerConsumer {
    private final Queue<String> queue = new LinkedList<>();
    public final ReentrantLock queueLock = new ReentrantLock();
    public final Condition qNotFull = queueLock.newCondition();
    public final Condition qNotEmpty = queueLock.newCondition();
    private final int maxItems = 2;

    /** Note: To implement this without ReentrantLock:
     * - make this method as synchronized
     * - replace Condition with Object. Eg. Object qNotFull
     * - replace await/signalAll with wait() and notifyAll()
     **/
    public void put(String item){
        queueLock.lock();
        try{
            while(queue.size() == maxItems){
                System.out.println("Queue is full");
                qNotFull.await();
            }
            System.out.println("Adding to queue "+item);
            queue.add(item);
            qNotEmpty.signalAll();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            queueLock.unlock();
        }
    }

    public void take(){
        queueLock.lock();
        try{
            while(queue.size() == 0){
                System.out.println("Queue is empty");
                qNotEmpty.await();
            }

            String item = queue.remove();
            System.out.println("Read from queue: "+item);
            qNotFull.signalAll();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            queueLock.unlock();
        }
    }

    public static void main(String[] args){
        final ProducerConsumer obj = new ProducerConsumer();
        ExecutorService executor = Executors.newCachedThreadPool();
        for(int i=0; i<15; i++){
            executor.submit(()->obj.put(UUID.randomUUID().toString()));
            executor.submit(()->obj.take());
        }
        executor.shutdown();
    }

}
