/**
 * Design a bathroom system with the following constraints:
 * Men and women cannot be in the bathroom at the same time.
 * There can be max 3 employees in the bathroom simultaneously.
 */

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadUnisexBathroom {
	private AtomicInteger numMales = new AtomicInteger();
	private AtomicInteger numFemales = new AtomicInteger();
	
	public synchronized void enterMale()
	{
		while(numFemales.get() > 0 || numMales.get() == 3)
		{
			try {
				wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		numMales.getAndIncrement();
		
		System.out.println(Thread.currentThread().getName() + " Entered male, " + numMales.get());
		notifyAll();
	}
	
	public synchronized void enterFemale()
	{
		while(numMales.get() > 0 || numFemales.get() == 3)
		{
			try {
				wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		numFemales.getAndIncrement();
		System.out.println(Thread.currentThread().getName() + " Entered female, " + numFemales.get());
		notifyAll();
	}
	
	public synchronized void exitMale()
	{
		while(numMales.get() == 0)
		{
			try {
				wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		numMales.getAndDecrement();
		System.out.println(Thread.currentThread().getName() + " Exit male, " + numMales.get());
		notifyAll();
	}
	
	public synchronized void exitFemale()
	{
		while(numFemales.get() == 0)
		{
			try {
				wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		numFemales.getAndDecrement();
		System.out.println(Thread.currentThread().getName() + " Exit female, " + numFemales.get());
		notifyAll();
	}
	
	
	public static void main(String[] args)
	{
		ThreadUnisexBathroom obj = new ThreadUnisexBathroom();
		ArrayList<Thread> threads = new ArrayList<Thread>();
		for(int i=0; i<10; i++)
		{
			Runnable r1 = () -> {
				obj.enterMale();
			};
			threads.add(new Thread(r1));
			
			Runnable r2 = () -> {
				obj.enterFemale();
			};
			threads.add(new Thread(r2));
			
			Runnable r3 = () -> {
				obj.exitMale();
			};
			threads.add(new Thread(r3));
			

			Runnable r4 = () -> {
				obj.exitFemale();
			};
			threads.add(new Thread(r4));

		}
		
		for(Thread t: threads)
		{
			t.start();
		}
		
		for(Thread t: threads)
		{
			try {
				t.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		

	}
}
