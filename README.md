# Concurrency
Fundamental concepts about multithreading.
## Basics
#### Concurrency vs Parallelism
Concurrency == multi threading. Concurrent execution alternates doing a little of each task until both are all complete.
Concurrency allows a program to make progress even when certain parts are blocked.
For instance, when one task is waiting for user input, the system can switch to another task and do calculations.
<br/>
Parallelism == multiple CPUs or multicore --> divides a computation into independent parallel processes which are run by
different CPUs or different cores on same CPU. Use Fork/join.

#### Mutex vs Semaphore vs Monitor
Mutex guards access to critical section - synchronized(lock)<br/>
Semaphore hands out limited number of permits like in a DB connection pool. Used to synchronize threads.<br/>
Monitor is an language concept while Mutex and Semaphore are OS concepts. All Java objects are actually monitors - mutex + wait()/notify() support. Thread A can call condition.wait() and go into wait state. Thread B can come in, update the condition and call notify() before leaving so Thread A can resume.

#### Sleep() vs wait()
- Thread.sleep() and obj.wait() - called on diff things.
- Thread in wait() gives up the lock, moves to waiting state until notify() is called. Thread in sleep state retains the lock while moving to waiting state until time is up.
- wait() is used to achieve synchronization and avoid race conditions.

#### User Thread vs daemon Thread
Daemon threads are low priority threads that run in the back ground doing system tasks. Call setDaemon() to create a daemon thread.<br/>
When a program exits, all user threads created by that program also exit. However daemon threads created by the program will continue to run. JVM is terminated only if there are no active user threads, but does not care about daemon threads.

#### ConcurrentHashMap vs Collections.synchronizedMap()
ConcurrentHashMap has better performance as reads are not blocked and 16 threads can write in separate segments simultaneously.<br/>
synchronizedMap() blocks the entire object for read and write. Hence suited where data consistency is important

#### Blocking vs Non-blocking I/O
Blocking: Main thread invokes the kernel thread to do the actual IO and then goes to sleep, until it is woken by the kernel. Wastes CPU time and limits scalability (as max thread is limited) if most are waiting for IO. Files in java.io package perform blocking IO.<br/>
Non-blocking: Main thread invokes the kernel thread and then continues its work while it waits for data. Files in java.nio package like Channel and Buffers.<br/>
Use Async or reactive to release the main thread while the processing is completed asynchronously.

#### Class level vs Object level synchronization
synchronized(Car.class) -- locks across all instances. Only option for static methods. Can be used in non-static code also.<br/>
synchronized(this) -- locks across all methods in an object.

#### Force thread to start or stop
Starting a thread depends on the OS. Even if you set priority, no guarantee that highest priority thread runs first. To stop a thread:
- Periodically check Thread.currentThread().isInterrupted() and exit.
- Call executor.shutdown() (terminate after running and queued tasks are done) or executor.shutdownNow() (terminate after the running threads are done).
- With Callable, call Future.get(timeout) or Future.cancel() where it tries to stop.

#### Singleton creation
Lazy loading, memory efficient and thread safe. The object is created only once, when the SingletonHolder class is loaded.
```
public class Singleton {
    public static class SingletonHolder {
        public static final Singleton HOLDER_INSTANCE = new Singleton();
    }

    public static Singleton getInstance() {
        return SingletonHolder.HOLDER_INSTANCE;
    }
}
```
## Executor Framework
The executor framework manages thread pool. It provides ways to create thread pools of different types.
<br>
| Scenario            | Thread pool type                                                                                                                                                |
|---------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------|
| CPU intensive tasks | Same no. of threads as CPU cores, so that CPU time is not wasted with context switching. <br/>```int cores = Runtime.getRuntime().availableProcessors(); Executors.newFixedThreadPool(cores);```<br/> If tasks > threads, they are added to LinkedBlockingQueue (unbounded) |
| Short tasks or I/O intensive tasks | CachedThreadPool - creates a thread as needed (no limit). Cleans up threads that are inactive for 60s. |
| Ensure order of tasks | SingleThreadedExecutor - Stores tasks in a linked blocking queue. Single thread that picks from queue in order and executes one after another. |
| Heavily loaded systems | WorkStealingPool - Single queue to hold tasks leads to contention, especially with short tasks since the queue must be locked before a task can be removed from it. With WorkStealingPool, each thread has its own queue. A free thread can steal a task from another thread's queue. |
| Truly parallel tasks | ForkJoinPool - Suitable if your task can be forked into subtasks and then join them when done to return a result. Example: sorting, matrix multiplication, tree traversal, move finder for a game. https://www.baeldung.com/java-fork-join |
| Run tasks based on frequency or time delay | ScheduledThreadPool <br>```TimeUnit t = TimeUnit.SECONDS;```<br/>```ScheduledExecutorService executor = Executors.newScheduledThreadPool(3);```<br/>```executor.submit(task1); -- starts immediately```<br/>```executor.schedule(task2, 5, t); -- starts after 5s and runs once```<br/>```executor.scheduleAtFixedRate(task3, 5, 10, t); -- starts after 5s and repeats every 10s```<br/>```executor.scheduleWithFixedDelay(task4, 5, 10, t); -- starts after 5s and runs 10s after previous run ends.``` |

#### Ideal Thread pool size
Factors to consider when determining the thread pool size for ```FixedThreadPool```:
- No. of CPU cores
- Type of task.
  - If it is a CPU bound task, just specify the pool size to align with the number of cores.
  - If it is an IO bound task, determine the approx time it takes and calculate it.
The general formula is ```No. of cores * [1 + wait time/CPU time]```. For CPU bound, wait time is 0.

https://www.youtube.com/watch?v=ErNre5varF8

#### Callable vs Future
Runnable threads cannot return any value, but Callable can.<br/>
Executor's execute() for runnable and submit() for callable and runnable, returns Future. Using Future, use isDone() to check the status and get() to get the returned Object. The get() is a blocking call.

## Inter-Thread communication
- State: Class/instance volatile variables
- Data: Heap is shared b/w threads and stack is not shared.
- Signal: wait/notify, condition, semaphore.
#### Volatile Keyword
Tells the threads to read/write shared variables from main memory instead of cache. Ensures that all threads see the latest value.<br/>
If there is just one read thread and one write thread, just volatile is enough. If you have multiple write threads, you need to synchronize the variable writes as well.
#### ThreadLocal
Way to store data that is private to the thread but accessible to all classes/methods that are invoked by the thread. 

## Thread Synchronization Methods
<i>Amazon: What are thread synchronization methods, how do they use CPU time slice and compare their efficiency?</i><br/>
- wait/notifyAll()
- condition locks
- Semaphore
- Countdown latch
- Cyclic barrier
TBD on the rest of the question
#### Condition Locks
```
Lock pubsubMonitor = new ReentrantLock();
Condition added = pubsubMonitor.newCondition();
public void producer() { ...
  pubsubMonitor.lock();
  try{
       added.signal();
  } finally {
   pubsubMonitor.unlock();
  }
}
public void consumer() { ...
   pubsubMonitor.lock();
   try{
      while(condition)  // while clause to handle when the thread is woken up without a signal (spurious wake)
        added.await();  
... 
```
Better than wait/notify - can be used to fine tune which thread to wake up when multiple are waiting on the same lock. For example, BlockingQueue implementation uses one lock for the queue but different conditions - full and empty. The put() cares about full condition while take() cares about empty. If we didnt have this granularity and just had notify(), then a take() thread might be woken up when queue is already empty. A notifyAll() would make all threads to unnecessarily contend for resources. Downside is that the signal could be missed f the consumer is not ready.

#### Countdown Latch
Wait for threads/operations to finish.
```
CountDownLatch latch = new CountDownLatch(3);
latch.await();
```
Blocks until latch.countDown() is called 3 times (like by each child thread when it exits). Used for Scatter Gather apps. Can be used outside of threads as well. For example, wait for Redis to be setup before sending messages.

#### Cyclic Barrier
Helps to synchronize threads. Wait for all threads to reach a particular point before proceeding. Can be reset for repeated sync.

## Locking Mechanisms
- synchronized
- ReentrantLock - added features
- ReadWriteLock - if reads > writes. Read locks blocked until write lock is held. Reads run in parallel when write lock is released.
- StampedLock -- Optimistic non-blocking read.
- Semaphores - limits the number of concurrent access.

#### ReentrantLock vs Synchronized
```
synchronized(obj) {
   // update resource
}
```
versus
```
ReentrantLock mylock = new ReentrantLock();
mylock.lock()
try {
   // update resource
}
finally {
   mylock.unlock();
}
```
ReentrantLock supports features like:
- trylock with option for timeout.
- lock interruptibly
- fair lock. All threads asking for a lock are stored in a queue. If you specify fairness=true when creating the lock object, it always gives the lock to threads in FIFO order. Default is unfair. Lets say Thread1 has the lock and 3 other threads are in a queue. Right when the lock is released if a Thread5 comes and asks for lock, it will be given. This allows faster operation as queue processing takes time.
- lock() can be called more than once. Nothing happens as the thread already has the lock. The hold count increasses and the lock must be released the same number of times.
  ```
  void accessResource(){
      lock.lock();
      if(condition)
           accessResource();
      lock.unlock()
  }
  ```
 
## Deadlocks
#### Deadlock vs Livelock vs Starvation
Deadlock happens in the OS, where a process is put in a wait state. Livelock happens in code, where the processes are running but keep checking for some resource and dont progress. Starvation happens when the low priority processes are blocked by high priority threads.
<br/>
The following code causes deadlock.<br/>
```
thread1 -> method 1:
synchronized(lockA){
...synchronized(lockB) {..}}

thread2 -> method 2:
synchronized(lockB){
...synchronized(lockA) {..}}
```
Thread 1 runs and acquires lock A. <br/>
Context switched<br/>
Thread 2 runs and acquires lock B<br/>
Deadlock!<br/>
<b>Always lock in the same order.</b>

#### Detecting Deadlocks
<i>Amazon: How to detect deadlocks? What tools would you use?</i><br/>
Tools that help take a thread dump like jstack or JConsole can help see deadlocks. Check for the following during code review:
- nested synchronization
- holds one lock while it tries to get another lock
- no consistency in the way the locks are ordered and
- there are no timeouts.

#### Avoiding Deadlocks
<i>How to avoid deadlocks?</i><br/>
- Using ReentrantLock's tryLock with a timeout is better than synchronized. If you can't get the lock, release your other lock too and retry after sometime.
- Avoid Nested Lock, dont wait indefinitely and dont use thread joins.
- Lock Only What is Required.

#### Using Deadlocks
<i>VMWare: In which scenario could we use deadlock?</i><br/>
Maybe for security. Prevent incorrect access. Maybe to assess about resources.

### Distributed Concurrency Control
Let's take a distributed movie booking application. User 1 and User 2 are trying to book seat 2. If their requests land on two different servers, none of the locking mechanisms described here will help. *** ```synchronized``` and other locks are local to a process in a given machine.***. This is handled by concurrency control techniques at the DB layer. The options are:
- Optimistic Concurrency Control
- Pessimistic Concurrency Control
Learn more @ https://www.youtube.com/watch?v=D3XhDu--uoI

#### Latest in Concurrency
In Java 21, there is Virtual threads:
- alternative implementation of Thread and ExecutorService. 
- OS doesn't know about them
  - jvm concept
  - stack lives on heap. regular thread needs to reserve stack space like 1MB per thread. with heap, you can use as little as you want.
- Cheap to create than platform threads.

Structured consistency is in experimental feature state. Helps with fork join and handle errors, race conditions, etc at parent level.

## Problems
Check out the README at https://github.com/harippriyas/multithreading/tree/main/multithreading
