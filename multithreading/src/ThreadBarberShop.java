/**
 * A barbershop consists of a waiting room with n chairs. If there are no customers, the barber goes to sleep.
 * Customer enters and wakes him up. If he is busy, the customer sits in the chair.
 * If there are no free chairs, he leaves.
 */

import java.util.ArrayList;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadBarberShop {

	// num permits = 0 means that the semaphore must be released first before it can be acquired
	private Semaphore barberSeat = new Semaphore(0);
	private static Semaphore customerArrived = new Semaphore(0);
	private static AtomicInteger numCustomers = new AtomicInteger(0);
	private static int MAX_SEATS = 5;

	public void customerArrived() throws InterruptedException
	{
		if(numCustomers.incrementAndGet() > MAX_SEATS) {
			System.out.println(Thread.currentThread().getName() + " is denied");
			return;
		}
		// this triggers the barber to start service for this customer
		customerArrived.release();
		System.out.println(Thread.currentThread().getName() + " is waiting");
		// this semaphore is released only after service is done. It simply blocks so that the print stmt is done correctly.
		barberSeat.acquire();
		System.out.println(Thread.currentThread().getName() + " is done");
	}
	
	public void getService() throws InterruptedException {
		// wait for the first customer to arrive
		customerArrived.acquire();
		// service waiting customers
		while (numCustomers.get() > 0) {
			Thread.sleep(20);
			numCustomers.decrementAndGet();
			barberSeat.release();
			// wait for another customer to arrive before checking counts
			// this ensures the barber thread is alive when there is a break between customers
			customerArrived.acquire();
		}

	}
	
	public static void main(String[] args)
	{
		ThreadBarberShop obj = new ThreadBarberShop();

		Runnable r1 = () -> {
			try {
				obj.getService();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		};
		Thread barber = new Thread(r1);
		barber.start();

		ArrayList<Thread> threads = new ArrayList<Thread>();
		for(int i=0; i<10; i++)
		{
			Runnable r2 = () -> {
				try {
					obj.customerArrived();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			};
			threads.add(new Thread(r2));
		}

		for(Thread t: threads)
		{
			t.start();
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
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
		// Allow the barber thread to exit
		customerArrived.release();

	}
}
