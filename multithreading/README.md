# Multithreading Problems
## Basics
- Use Executors to create the thread pool
```
ExecutorService executorService = Executors.newFixedThreadPool(5);
ThreadSolution obj = new ThreadSolution();
Runnable r1 = () -> {
   obj.method();
};
executorService.submit(r1);
...            
executorService.shutdownNow();
``` 
- The method invoked by the thread should be synchronized with wait/notify or should have locks.
```
private ReentrantLock lock = new ReentrantLock();
public void mymethod() throws InterruptedException, BrokenBarrierException
{
	lock.lock();
	...
	lock.unlock();
}

public synchronized void mymethod2()
{
	while(conditionNotMet)
	{
		try {
			wait();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
		
	...
	
	notifyAll();
}
```
- Use cyclic barrier if you want to wait for all threads to sync up

## Print Sequence
We need one thread to wake up another specific thread, so that the sequence is printed in order. 
#### Semaphores
- Use a semaphore for each thread.
  - THread should be given its own semaphore and the next thread's semaphore
  - Thread would do its processing when it is able to acquire its semaphore.
  - Once it is done, it will release the next thread's semaphore.
- Acquire all semaphores at the beginning
- Submit the threads to the executor
- Release the semaphore for the first thread, so that the process is kick started.
Example: [ThreadSequencing2.java](https://github.com/harippriyas/multithreading/blob/main/multithreading/src/ThreadSequencing2.java)

#### AtomicIntegers
- Define static atomic integers for thread counter
- Each thread has its own ID
- Execute processing if the counter equals its ID
- Reset/Increment ID as needed
Example:  [ThreadSequencing3.java](https://github.com/harippriyas/multithreading/blob/main/multithreading/src/ThreadSequencing3.java)

## Read Heavy System

## Write Heavy System