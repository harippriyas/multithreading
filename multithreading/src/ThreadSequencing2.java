/**
 * Salesforce: The first thread prints 1 1 1 ..., the second one prints 2 2 2 ..., and the third one prints 3 3 3 ...
 * Schedule these 3 threads in order to print 1 2 3 1 2 3 ...
 */

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class ThreadSequencing2 {
    private static int NUM_ITERATIONS = 10;
    private static enum TYPE {ONE, TWO, THREE};

    private State currState;
    private final ReentrantLock printerLock;
    private final Condition printConditiom;
    
    public ThreadSequencing2() {
        currState = new State(TYPE.ONE.ordinal(), 0);
        printerLock = new ReentrantLock();
        printConditiom = printerLock.newCondition();
    }

    public void startPrintThreads(){
        State one = new State(TYPE.ONE.ordinal(), TYPE.TWO.ordinal()) ;
        State two = new State(TYPE.TWO.ordinal(), TYPE.THREE.ordinal()) ;
        State three = new State(TYPE.THREE.ordinal(), TYPE.ONE.ordinal()) ;

        ExecutorService executor = Executors.newFixedThreadPool(3);
        executor.execute(()->print(one));
        executor.execute(()->print(two));
        executor.execute(()->print(three));
        executor.shutdown();

    }

    public void print(State threadState){
        for(int i=0; i<NUM_ITERATIONS; i++) {
            printerLock.lock();
            try {
                while (currState.currType != threadState.currType) {
                    printConditiom.await();
                }
                System.out.print(threadState.currType + 1 + " ");
                currState.currType = threadState.nextType;
                printConditiom.signalAll();

            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                printerLock.unlock();
            }
        }
    }
    public static void main(String[] args){
        ThreadSequencing2 printer = new ThreadSequencing2();
        printer.startPrintThreads();
    }

    class State {
        private int currType;
        private int nextType;

        public State(int currType, int nextType) {
            this.currType = currType;
            this.nextType = nextType;
        }

    }

}
