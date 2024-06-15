/**
 * Design an algorithm whereby either an Uber ride can have all democrats or republicans or 2 Dems and 2 Republicans.
 * Model the ride requestors as threads. When all the threads are seated,
 * any one of the four threads can invoke the method drive()
 */
import java.util.ArrayList;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

public class ThreadUberRide {
	private Semaphore demSemaphore = new Semaphore(0);
	private Semaphore repSemaphore = new Semaphore(0);
	private ReentrantLock lock = new ReentrantLock();
	private int numDemocrats = 0;
	private int numRepublicans = 0;
	
	public void seatRider(boolean isDemocrat) throws InterruptedException, BrokenBarrierException
	{
	
		boolean readyToRide = false;
		lock.lock();
		if(isDemocrat)
		{
			numDemocrats++;
		}
		else
		{
			numRepublicans++;
		}
		
		
		if(numDemocrats == 4)
		{
			demSemaphore.release(3);
			numDemocrats = 0;
			readyToRide = true;
		}
		else if(numRepublicans == 4)
		{
			repSemaphore.release(3);
			numRepublicans = 0;
			readyToRide = true;
		}
		else if(numRepublicans >=2 && numDemocrats >=2)
		{
			demSemaphore.release(3);
			repSemaphore.release(3);
			numDemocrats -= 2;
			numRepublicans -= 2;
			readyToRide = true;
		}
		else if(isDemocrat)
		{
			lock.unlock();
			demSemaphore.acquire();
		}
		else
		{
			lock.unlock();
			repSemaphore.acquire();
		}
		
		
		
		System.out.println("Seated " + (isDemocrat? "Democrat" : "Republican"));
		
		if(readyToRide)
		{
			System.out.println("On the way");
			lock.unlock();
		}
 	}
	
	public static void main(String[] args)
	{
		ThreadUberRide obj = new ThreadUberRide();
		ArrayList<Thread> threads = new ArrayList<Thread>();
		for(int i=0; i<20; i++)
		{
			Runnable r1 = () -> {
				try {
					obj.seatRider(true);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			};
			threads.add(new Thread(r1));
			
			Runnable r2 = () -> {
				try {
					obj.seatRider(false);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			};
			threads.add(new Thread(r2));
		}
		
		for(int i=0; i<4; i++)
		{
		
			Runnable r2 = () -> {
				try {
					obj.seatRider(false);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			};
			threads.add(new Thread(r2));
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
