import java.util.concurrent.atomic.AtomicInteger;

/** Scenario: Print series 010203040506. Using multi-threading 1st thread will print only 0 2nd thread will print only even numbers and 3rd thread print only odd numbers.
 *  
 *  Simpler solution but only uses 2 threads
 *  */
public class ThreadSequencing5
{
	
	private Object lock = new Object();
	private AtomicInteger numToPrint = new AtomicInteger(1);
	private AtomicInteger currThread = new AtomicInteger(1);

	public MyThread createThreadObj(int threadId)
	{
		return new MyThread(threadId);
	}
	
	public class MyThread implements Runnable
	{
		private int threadId;

		public MyThread(int threadId)
		{
			this.threadId = threadId;
		}
		
		@Override
		public void run() 
		{
			try
			{
				while(numToPrint.get() <= 10)
				{
					synchronized(lock)
					{
						if(currThread.get() == threadId)
						{
							if(threadId == 1)
							{
								System.out.print("0");
								currThread.getAndIncrement();
							}
							else
							{
								System.out.print(numToPrint.getAndIncrement());
								currThread.set(1);
							}
							lock.notifyAll();
						}
						else
						{
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
		ThreadSequencing5 ts = new ThreadSequencing5();
		
		Thread t1 = new Thread(ts.createThreadObj(1));
		Thread t2 = new Thread(ts.createThreadObj(2));
		
		t1.start();
		t2.start();
		
	}
}
