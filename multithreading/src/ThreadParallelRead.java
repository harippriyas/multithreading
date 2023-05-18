import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

/** GS: You have 5 data sources. There is a program which calls these data sources and returns a count value. 
 *  How do you speedup this program?
		int count = getCount(ds1);
		if(count < 100 )
		  count = count + getCount(ds2);
		if(count < 100)
		  count = count + getCount(ds3);
		if(count < 100)
		  count = count + getCount(ds4);
		if(count < 100)
		  count = count + getCount(ds5);
 **/
public class ThreadParallelRead
{
	public volatile int count = 0;
	public AtomicInteger index = new AtomicInteger();
	public ArrayList<Integer> ds = new ArrayList();
	
	public ThreadParallelRead()
	{
		ds.add(34);
		ds.add(54);
		ds.add(64);
		ds.add(32);
		ds.add(5);
	}
	
	public void readWithExecutors()
	{
		System.out.println("readWithExecutors called");
		ExecutorService executors = Executors.newFixedThreadPool(5);
		ConcurrentLinkedQueue<Future> results = new ConcurrentLinkedQueue();
		for(int i = 0; i < 5; i++)
		{
			Future<?> result = executors.submit(() -> getCount());
			results.add(result);
		}
		
		while(count < 100)
		{
			results.forEach(result -> {
				if(result.isDone())
				{
					try {
						int c= Integer.parseInt(result.get().toString());
						
						synchronized(this)
						{
							if(count < 100)
							{
								System.out.println("Adding " + c);
								count += Integer.parseInt(result.get().toString());
							}
							results.remove(result);
						}
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			});
		}
		System.out.println(count);
		executors.shutdown();
	}
	
	public void readWithStreams()
	{
		System.out.println("readWithStreams called");
		count = 0;
		ds.parallelStream().forEach(i -> { 
			synchronized(this)
			{
				if(count < 100)
				{
					System.out.println("Adding " + i);
					count += (int) i;
				}
			}
		});
		System.out.println(count);
	}
	
	public int getCount()
	{
		return ds.get(index.getAndIncrement());
	}
	
	public static void main(String[] args)
	{
		ThreadParallelRead ts = new ThreadParallelRead();
		ts.readWithExecutors();
		ts.readWithStreams();
	}
}
