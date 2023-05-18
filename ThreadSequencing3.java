import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

/** Scenario: There are 10 resources and 5 threads. Threads should ask for Resources in order. 
 *  Eg t1 asks for 3 resources, t2 asks for 4 resources… 
 *  */
public class ThreadSequencing3
{
	private Object lock = new Object();
	private AtomicInteger resourceCounter = new AtomicInteger(1);
	private AtomicInteger currThread = new AtomicInteger(1);

	public MyThread createThreadObj(int threadId, int numResources)
	{
		return new MyThread(threadId, numResources);
	}
	
	public class MyThread implements Runnable
	{
		private int threadId, numResources;

		public MyThread(int threadId, int numResources)
		{
			this.threadId = threadId;
			this.numResources = numResources;
		}
		
		@Override
		public void run() 
		{
			try
			{
				while(resourceCounter.get() <= 10)
				{
					System.out.println("in thread " + threadId);
					
					// need synchronized so that we can use wait() and notify(). 
					// Removing synchronized, wait(), notify() works but will be a busy-wait. Thread does much more spinning than is needed.
					synchronized(lock)
					{
						if(currThread.get() == threadId)
						{
							IntStream.range(0, numResources).forEach(i -> System.out.println("Thread " + threadId + ": " + resourceCounter.getAndIncrement()));
							currThread.getAndIncrement();
							lock.notifyAll();
						}
						else
						{
							// this is not enough to keep the thread alive. 
							// You need the while at the top so that the run() does not exit until we print the resource for this thread.
							lock.wait();
						}
					}
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			
		}
		
	}
	
	public static void main(String[] args)
	{
		ThreadSequencing3 ts = new ThreadSequencing3();
		
		Thread t1 = new Thread(ts.createThreadObj(1, 3));
		Thread t2 = new Thread(ts.createThreadObj(2, 1));
		Thread t3 = new Thread(ts.createThreadObj(3, 2));
		Thread t4 = new Thread(ts.createThreadObj(4, 3));
		Thread t5 = new Thread(ts.createThreadObj(5, 1));
		
		t1.start();
		t2.start();
		t3.start();
		t4.start();
		t5.start();
		
	}
}
