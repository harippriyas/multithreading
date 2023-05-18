import java.util.ArrayList;
import java.util.concurrent.Semaphore;

public class ThreadBarberShop {

	private Semaphore customerArrived = new Semaphore(0);
	private Semaphore barberStart = new Semaphore(0);
	private Semaphore barberDone = new Semaphore(0);
	private int numCustomers = 0;
	
	public void customerArrived() throws InterruptedException
	{
		synchronized(this)
		{
			if(numCustomers == 3)
			{
				System.out.println(Thread.currentThread().getName() + " is denied");
				return;
			}
			
			numCustomers++;
		}
		customerArrived.release();
		barberStart.acquire();
		barberDone.acquire();
		synchronized(this)
		{
			numCustomers--;
		}
		System.out.println(Thread.currentThread().getName() + " is done");
	}
	
	public void getService() throws InterruptedException
	{
		int servicedClients = 0;
		while(servicedClients < 10)
		{
			customerArrived.acquire();
			barberStart.release();
			Thread.sleep(200);
			barberDone.release();
			servicedClients++;
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
		
		
	}
}
