import java.util.concurrent.Semaphore;

public class ThreadAsyncToSync {
	
	public  Callback getCallableObj(Semaphore sem)
	{
		return new SyncCallback(sem);
	}
	
    public static void main( String args[] ) throws Exception{
    	
    	Semaphore sem = new Semaphore(0);
    	ThreadAsyncToSync obj = new ThreadAsyncToSync();
        Executor executor = new Executor();
        executor.asynchronousExecution(obj.getCallableObj(sem));

        sem.acquire();
        System.out.println("main thread exiting...");
    }
    
    class SyncCallback implements Callback
    {
    	Semaphore s;
    	public SyncCallback(Semaphore s)
    	{
    		this.s = s;
    	}
    	
		@Override
		public void done() {
			System.out.println("I am done");
			s.release();
			
		}
    	
    }
}

interface Callback {

    public void done();
}


class Executor {

    public void asynchronousExecution(Callback callback) throws Exception {

        Thread t = new Thread(() -> {
            // Do some useful work
            try {
                Thread.sleep(5000);
            } catch (InterruptedException ie) {
            }
            callback.done();
        });
        t.start();
    }
}