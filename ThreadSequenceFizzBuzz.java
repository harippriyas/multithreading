import java.util.concurrent.atomic.*;
import java.util.concurrent.*;

public class ThreadSequenceFizzBuzz {

	public volatile AtomicInteger count = new AtomicInteger(1);
	
	public synchronized void printFizz()
	{
		while(count.get() < 30)
		{
			if(count.get() % 3 == 0 && count.get() % 5 != 0)
			{
				System.out.print("Fizz ");
				count.incrementAndGet();
				notifyAll();
			}
			else {
				try {
						wait();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			}
		}

	}
	
	public synchronized void printBuzz()
	{
		while(count.get() < 30)
		{
			if(count.get() % 3 != 0 && count.get() % 5 == 0)
			{
				System.out.print("Buzz ");
				count.incrementAndGet();
				notifyAll();
			}
			else {
				try {
						wait();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			}
		}

	}
	
	public synchronized void printFizzBuzz()
	{
		while(count.get() < 30)
		{
			if(count.get() % 15 == 0)
			{
				System.out.print("FizzBuzz ");
				count.incrementAndGet();
				notifyAll();
			}
			else {
				try {
						wait();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			}
		}

	}
	
	public synchronized void printNum()
	{
		while(count.get() < 30)
		{
			if(count.get() % 5 != 0 && count.get() % 3 != 0)
			{
				System.out.print(count.getAndIncrement() + " ");
				notifyAll();
			}
			else {
				try {
						wait();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			}
		}
	}
	
	/*public synchronized void printValue(String type)
	{
		while(count.get() < 30)
		{
			boolean foundMatch = false;
		
			if(type.equals("fizz"))
			{
				if(count.get() % 3 == 0 && count.get() % 5 != 0)
				{
					System.out.print("Fizz ");
					count.incrementAndGet();
					foundMatch = true;
				}
			}
			else if(type.equals("buzz"))
			{
				if(count.get() % 5 == 0 && count.get() % 3 != 0)
				{
					System.out.print("Buzz ");
					count.incrementAndGet();
					foundMatch = true;
				}
			}
			else if(type.equals("fizzbuzz"))
			{
				if(count.get() % 15 == 0)
				{
					System.out.print("FizzBuzz ");
					count.incrementAndGet();
					foundMatch = true;
				}
			}
			else
			{
				System.out.print(count.getAndIncrement() + " ");
				foundMatch = true;
			}
			
			notifyAll();
			
			try {
					wait();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		notifyAll();
		System.out.println("End of run for " + type);
	}*/
	
	public void createThreads()
	{
		Thread t1 = new Thread(() -> printFizz());
		Thread t2 = new Thread(() -> printBuzz());
		Thread t3 = new Thread(() -> printFizzBuzz());
		Thread t4 = new Thread(() -> printNum());
		t4.start();
		t3.start();
		t2.start();
		t1.start();
		
	}
	
	public static void main(String[] args)
	{
		ThreadSequenceFizzBuzz obj = new ThreadSequenceFizzBuzz();
		obj.createThreads();

	}

}
