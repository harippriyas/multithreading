import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

/** Scenario: There are 10 resources and 5 threads. Threads should ask for Resources in order. 
 *  Eg First allocate 3 resources to t1, then 1 resource to t2, then 2 resources to t3,...
 *  */
public class ThreadSequencing3 implements Runnable
{
	private static Object lock = new Object();
	private static AtomicInteger resourceCounter = new AtomicInteger(0);
	private static AtomicInteger currThread = new AtomicInteger(1);

	private int threadId, numResources;

	public ThreadSequencing3(int threadId, int numResources)
	{
		this.threadId = threadId;
		this.numResources = numResources;
	}

	@Override
	public void run()
	{
		try
		{
			while(resourceCounter.get() < 10)
			{
				System.out.println("in thread " + threadId);

				// need synchronized so that we can use wait() and notify().
				// Removing synchronized, wait(), notify() works but will be a busy-wait.
				// Thread does much more spinning than is needed.
				synchronized(lock)
				{
					if(currThread.get() == threadId)
					{
						IntStream.range(0, numResources).forEach(i ->
								System.out.println("Thread " + threadId + ": " + resourceCounter.incrementAndGet()));
						currThread.getAndIncrement();
						lock.notifyAll();
					}
					else
					{
						// this is not enough to keep the thread alive.
						// You need the while at the top so that the run() does not exit until we print
						// the resource for this thread.
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

	public static void main(String[] args)
	{
		ExecutorService executorService = Executors.newFixedThreadPool(5);
		executorService.submit(new ThreadSequencing3(1, 3));
		executorService.submit(new ThreadSequencing3(2, 1));
		executorService.submit(new ThreadSequencing3(3, 2));
		executorService.submit(new ThreadSequencing3(4, 3));
		executorService.submit(new ThreadSequencing3(5, 1));

		try {
			executorService.awaitTermination(10, TimeUnit.SECONDS);

		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		finally {
			System.out.println("Shutting down executor");
			executorService.shutdownNow();
		}
		
	}
}
