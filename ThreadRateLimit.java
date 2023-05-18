import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadRateLimit {
	
	private AtomicInteger tokens = new AtomicInteger(0);
	
	
	public synchronized void addToken()
	{
		System.out.println(Thread.currentThread().getName() + " add token ");
		if(tokens.get() < 5)
			tokens.getAndIncrement();
		notifyAll();
				
	}
	
	public synchronized void getToken()
	{
		if(tokens.get() == 0)
		{
			try {
				wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else
		{
			tokens.getAndDecrement();
			System.out.println(Thread.currentThread().getName() + " got token ");
			
		}
	}
	
	class ProducerThread implements Runnable
	{

		@Override
		public void run() {
			while(true)
			{
				addToken();
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
	}
	
	class ConsumerThread implements Runnable
	{

		@Override
		public void run() {
			getToken();
		}
		
	}
	
	public Thread createProducerThread()
	{
		return new Thread(new ProducerThread());
	}
	
	public Thread createConsumerThread()
	{
		return new Thread(new ConsumerThread());
	}
	
	public static void main(String[] args)
	{
		ThreadRateLimit obj = new ThreadRateLimit();
		Thread pt = obj.createProducerThread();
		pt.setDaemon(true);
		pt.start();
		
		for(int i=0; i<20; i++)
		{
			Thread c = obj.createConsumerThread();
			c.start();
			try {
				c.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
