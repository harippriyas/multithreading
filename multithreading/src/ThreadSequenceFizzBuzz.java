import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class ThreadSequenceFizzBuzz {

    public volatile AtomicInteger count = new AtomicInteger(1);
    public final ReentrantLock printLock = new ReentrantLock();
    public Condition lockCondition = printLock.newCondition();
    public static enum TYPE { FIZZ, BUZZ, FIZZBUZZ, NUM};

    public static void main(String[] args)
    {
        ThreadSequenceFizzBuzz obj = new ThreadSequenceFizzBuzz();
        obj.doPrint();
    }

    public void doPrint()
    {
        Printer fizzThread = new Printer(TYPE.FIZZ.ordinal());
        Printer buzzThread = new Printer(TYPE.BUZZ.ordinal());
        Printer fizzbuzzThread = new Printer(TYPE.FIZZBUZZ.ordinal());
        Printer numThread = new Printer(TYPE.NUM.ordinal());
        try (ExecutorService executor = Executors.newFixedThreadPool(4)) {
            executor.execute(fizzThread::execute);
            executor.execute(buzzThread::execute);
            executor.execute(fizzbuzzThread::execute);
            executor.execute(numThread::execute);
            executor.shutdown();
        }

    }

    class Printer {
        private final int type;

        public Printer(int type){
            this.type = type;
        }
        public void execute(){
            while(count.get() < 20)
            {
                printLock.lock();
                try{
                    boolean executeDone = false;
                    if (count.get() % 3 == 0 && count.get() % 5 == 0 && type == TYPE.FIZZBUZZ.ordinal()){
                        executeDone=true;
                        System.out.print(TYPE.FIZZBUZZ.name());
                    }
                    else if (count.get() % 3 == 0 && count.get() % 5 != 0 && type == TYPE.FIZZ.ordinal()){
                        executeDone=true;
                        System.out.print(TYPE.FIZZ.name());
                    }
                    else if (count.get() % 3 != 0 && count.get() % 5 == 0 && type == TYPE.BUZZ.ordinal()){
                        executeDone=true;
                        System.out.print(TYPE.BUZZ.name());
                    }
                    else if (count.get() % 3 != 0 && count.get() % 5 != 0 && type == TYPE.NUM.ordinal()){
                        executeDone=true;
                        System.out.print(count.get());

                    }

                    if(executeDone){
                        System.out.print(" ");
                        count.getAndIncrement();
                        lockCondition.signalAll();
                    }
                    else {
                        lockCondition.await();
                    }
                } catch (InterruptedException e) {
                    // throw new RuntimeException(e);
                } finally {
                    printLock.unlock();
                }
            }
        }
    }
}
