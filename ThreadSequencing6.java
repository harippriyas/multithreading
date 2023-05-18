import java.util.ArrayList;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

/** Say there are 3 array lists l1, l2 & l3 of same length. Three threads accessing three lists. Say T1 -> l1, T2 ->l2 & T3 ->l3. 
 * It should print in the order say first element of 1st then first element of 2nd list and then first element of 3rd list. 
 * Then second element of 1st then second element of 2nd list and then second element of 3rd list
 *  */
public class ThreadSequencing6
{
	public ArrayList<String> a1, a2, a3;
	private AtomicInteger index = new AtomicInteger(0);
	
	public ThreadSequencing6()
	{
		a1 = new ArrayList();
		a2 = new ArrayList();
		a3 = new ArrayList();
		
		a1.add("apple");
		a1.add("pear");
		a1.add("mango");
		a2.add("carrot");
		a2.add("brinjal");
		a2.add("peas");
		a3.add("bacon");
		a3.add("pork");
		a3.add("chicken");
	}

	public MyThread createThreadObj(ArrayList<String> a, Semaphore currSem, Semaphore nextSem, boolean isLastThread)
	{
		return new MyThread(a, currSem, nextSem, isLastThread);
	}
	
	public class MyThread implements Runnable
	{
		private boolean isLastThread = false;
		private Semaphore currSem, nextSem;
		private ArrayList<String> list;

		public MyThread(ArrayList<String> a, Semaphore currSem, Semaphore nextSem, boolean isLastThread)
		{
			this.list = a;
			this.currSem = currSem;
			this.nextSem = nextSem;
			this.isLastThread = isLastThread;
		}
		
		@Override
		public void run() 
		{
			try
			{
				while(index.get() < list.size())
				{
					currSem.acquire();
					// Prevent slipped condition by double checking if the index got incremented 
					// 	by another thread while we were waiting for semaphore
					if(index.get() < list.size())
					{
						System.out.println(list.get(index.get()));
						if(isLastThread)
						{
							index.getAndIncrement();
						}
					}
					nextSem.release();
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
		ThreadSequencing6 ts = new ThreadSequencing6();
		Semaphore s1 = new Semaphore(1);
		Semaphore s2 = new Semaphore(1);
		Semaphore s3 = new Semaphore(1);
		Thread t1 = new Thread(ts.createThreadObj(ts.a1, s1, s2, false));
		Thread t2 = new Thread(ts.createThreadObj(ts.a2, s2, s3, false));
		Thread t3 = new Thread(ts.createThreadObj(ts.a3, s3, s1, true));
		try {
			s1.acquire();
			s2.acquire();
			s3.acquire();
			
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		t1.start();
		t2.start();
		t3.start();
		s1.release();
	}
}
