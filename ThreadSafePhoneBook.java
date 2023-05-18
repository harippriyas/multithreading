import java.util.concurrent.*;
import java.util.*;

public class ThreadSafePhoneBook {
	private volatile ConcurrentHashMap<String, LinkedList<Integer>> phonebook = new ConcurrentHashMap<>();
	private CountDownLatch latch = new CountDownLatch(1);
	
	public void execute() throws InterruptedException
	{
		Runnable addThread1 = () -> { 
			addEntry("John Smith", 1234234234);
			addEntry("Jane Smith", 234234235);
			addEntry("Pat Malfoy", 78563458);
			addEntry("Robert H", 34536675);
		};
		Runnable addThread2 = () -> { 
			addEntry("John Smith", 87975678);
			addEntry("Jane Doe", 234234235);
			addEntry("Robert H", 78563458);
			addEntry("Tom S", 34536675);
		};
		Runnable lookupThread = () -> { findNumbers(); };
		ScheduledExecutorService executor = Executors.newScheduledThreadPool(2);
		executor.submit(addThread1);
		executor.submit(addThread2);
		executor.schedule(lookupThread, 2, TimeUnit.SECONDS);
		latch.await();
		executor.shutdown();
	}

	private void addEntry(String name, int number) {
		
		// atomically check if the name exists in the map and insert the value.
		phonebook.compute(name, (key, value) -> {
			value = (value != null) ? value : new LinkedList<Integer>();
			value.add(Integer.valueOf(number));
			return value;
		});
		
		
	}
	
	private void findNumbers() {
		System.out.println(Arrays.asList(phonebook.get("John Smith")).toString());
		latch.countDown();
		
	}
	
	public static void main(String[] args)
	{
		ThreadSafePhoneBook obj = new ThreadSafePhoneBook();
		try {
			obj.execute();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
