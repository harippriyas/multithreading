# Multithreading Problems
## Basics
- Use Executors to create the thread pool
```
ExecutorService executorService = Executors.newFixedThreadPool(5);
ThreadSolution obj = new ThreadSolution();
executorService.execute(obj::method);
// use the submit() method if we want Future.
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
			// TODO Auto-generated catch block9
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
Example: [ThreadSequencing4.java](https://github.com/harippriyas/multithreading/blob/main/multithreading/src/ThreadSequencing4.java)
> Note: This approach is brittle as you need to define the semaphores in code and hence cannot vary at runtime. Use semaphores to provide access to resources that are limited, like dining philosopher's fork or barber's chair, etc.

#### State object
Check out [ThreadSequencing2.java](https://github.com/harippriyas/multithreading/blob/main/multithreading/src/ThreadSequencing2.java) for example.
- Create enum for all states (like print 1, 2, 3 or odd/even or fizz/buzz)
- Create a lock and condition as instance variable.
- Create a State object as an instance object to keep of where we are at the execution, like num iterations or last value printed, etc.
- Create a method that takes thread state object as input (as in ThreadSequence2.java) or a separate class (as in FizzBuzz)
  - define exit condition
  - lock
  - try catch
    - while the thread condition is not met, await
    - execute logic
    - signalAll()
  - release lock in finally.
- Create diff state objects and invoke the method thru executors. 

#### AtomicIntegers
- Define static atomic integers for thread counter
- Each thread has its own ID
- Execute processing if the counter equals its ID
- Reset/Increment ID as needed
Example:  [ThreadSequencing3.java](https://github.com/harippriyas/multithreading/blob/main/multithreading/src/ThreadSequencing3.java)

## Read Heavy System

## Write Heavy System

## Producer Consumer
Reference: https://www.youtube.com/watch?v=4BEzgPlLKTo
#### Models
<b>Topic</b>: 
- variables: name, ID (UUID.randomUUID().toString()), List<Subscriber>, List<Message>.
- addMessage() - synchronized
- addSubscriber()

<b>Subscriber implements ISubscriber</b>
- variables: id, offset
- method: consume()

#### Handlers
<b>TopicHandler</b>
- variable: topic
- publish() - start the subscriber worker threads / notify existing threads

<b>SubscriberWorker thread</b>: 
In the run method:
- synchronize on Subscriber object
- wait until we have a message with ID > offset
- iterate through messages, invoke consume() on the subscriber. Then compareAndSet the new offset.

#### Entry point
<b>Queue object</b>
- variables: map of topicId, TopicHandler
- createTopic(name) -- create object and the handler. Add to map.
- subscribe(topicId, ISubscribe) -- get topic object and add to subscriber list
- publish(topicId, message) - get the TopicHandler and call its publish() in a thread
- resetOffset(topicId, ISubscribe) - get the topic object, get the subscriber object from it, set the offset and then start the worker.
