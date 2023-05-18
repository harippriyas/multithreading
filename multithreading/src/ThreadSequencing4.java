import java.util.concurrent.atomic.AtomicInteger;

/** Scenario: Print series 010203040506. Using multi-threading 1st thread will print only 0 2nd thread will print only even numbers and 3rd thread print only odd numbers.
 *  
 *  */
public class ThreadSequencing4
{
	public static enum Type { ZERO, ODD, EVEN };
	
	private Object lock = new Object();
	private AtomicInteger numToPrint = new AtomicInteger(1);
	private volatile int prevThread = Type.ZERO.ordinal();
	private volatile int nextThread = Type.ZERO.ordinal();

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
						if(nextThread == threadId)
						{
							if(threadId == Type.ZERO.ordinal())
							{
								System.out.print("0");
								if(prevThread == Type.ODD.ordinal())
								{
									nextThread = Type.EVEN.ordinal();
								}
								else
								{
									nextThread = Type.ODD.ordinal();
								}
							}
							else if(threadId == Type.ODD.ordinal())
							{
								System.out.print(numToPrint.getAndIncrement());
								prevThread = Type.ODD.ordinal();
								nextThread = Type.ZERO.ordinal();
							}
							else if(threadId == Type.EVEN.ordinal())
							{
								System.out.print(numToPrint.getAndIncrement());
								prevThread = Type.EVEN.ordinal();
								nextThread = Type.ZERO.ordinal();
							}
							
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
		ThreadSequencing4 ts = new ThreadSequencing4();
		
		Thread t1 = new Thread(ts.createThreadObj(ThreadSequencing4.Type.ZERO.ordinal()));
		Thread t2 = new Thread(ts.createThreadObj(ThreadSequencing4.Type.ODD.ordinal()));
		Thread t3 = new Thread(ts.createThreadObj(ThreadSequencing4.Type.EVEN.ordinal()));
		
		t1.start();
		t2.start();
		t3.start();
		
	}
}
