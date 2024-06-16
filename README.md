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
> Note:
> - Class loading may happen as soon as another class that references it is loaded or only when it is used (lazy loading). Even if loaded, it will be initialized only when it is accessed. Hence instantiation of the HOLDER_INSTANCE happens only when getInstance() is invoked.
> - The common approach is double checked locking. that is not suitable if constructor does longer init. Another thread will see that the instance object is not null and will start using it even while init is in progress.

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
- Data: Heap is shared b/w threads and stack is not shared. Stack contains local primitive variables while heap contains objects.
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
- Deadlock happens in the OS, where a set of processes are blocked because each process is holding a resource and waiting for another resource acquired by some other process.
- Livelock happens in code, where the processes are running but keep checking for some resource and dont progress.
- Starvation happens when the low priority processes are blocked by high priority threads.
<br/>

#### Causes of Deadlock
- Nested Lock aquisition: The following code causes deadlock.<br/>
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
- Circular lock dependency between processes.
- resource contention

#### Detecting Deadlocks
<i>Amazon: How to detect deadlocks? What tools would you use?</i><br/>
Symptoms:
- Application becomes sluggish and is slow to respond.
- High CPU usage as the threads are busy checking locks
- High memory usage as the threads dont allow GC
- no activity in logs, indicating that work is not being done.
  
Tools that help take a thread dump like jstack or JConsole can help see deadlocks. 
Check for the following during code review:
- nested synchronization
- holds one lock while it tries to get another lock
- no consistency in the way the locks are ordered and
- there are no timeouts.

#### Debugging
- Take a thread dump using the jstack command-line tool or Java VisualVM tool. This generates a snapshot of the stack traces of all threads in the JVM and help you identify threads in BLOCKED or WAITING states, as well as locks or objects they are contending for.
- Use same tools to monitor live threads, spot threads with high CPU usage, memory usage, or low throughput.
- jdb command-line tool or Debugging feature in IDE can be used to attach to running JVM and inspect variables and expressions of threads. This will enable you to set breakpoints, watchpoints, or conditional expressions that will trigger when a deadlock occurs.

Once the tasks are identified, reviewing the resouces accessed, the dependencies, etc will help find the root cause.

#### Avoiding Deadlocks
<i>How to avoid deadlocks?</i><br/>
- Use higher-level concurrency constructs, such as java.util.concurrent locks, synchronizers, or executors, as well as lock-free or wait-free algorithms or data structures that rely on atomic operations or optimistic concurrency control.
- dont wait indefinitely and dont use thread joins. Using ReentrantLock's tryLock with a timeout is better than synchronized.
- If you can't get the lock, release your other lock too and retry after sometime.
- Avoid Nested or circular Lock. Use same lock ordering.
- Lock Only What is Required.

#### Using Deadlocks
<i>VMWare: In which scenario could we use deadlock?</i><br/>
Maybe for security. Prevent incorrect access. Maybe to assess about resources.

## Other Concepts
### JVM Memory Model & Guarantees
The memory model is about using stack for thread level isolation and heap for sharing between threads. Stack stores local primitive variables while heap stores objects. [Learn more](https://dip-mazumder.medium.com/java-memory-model-a-comprehensive-guide-ba9643b839e#:~:text=The%20Java%20Memory%20Model%20(JMM,framework%20for%20safe%20multi%2Dthreading).<br/>
<b>Visibility Guarantees</b><br/>
```volatile & synchronized``` guarantee that changes to a variable done by one thread is visible to other threads. Volatile reads from main memory and immediately flushes write to main memory. syncronized reads from main memory when it enters the block and flushes to main memory while exiting.

<b>happens-before consistency</b><br/>

### CompletableFuture
https://www.youtube.com/watch?v=ImtZgX1nmr8 <br/>
Order processing may involve multiple steps like FetchOrder, EnrichOrder, Payment, Dispatch, Notification. These are inter-related, so we would have to execute them sequentially. However we want the main thread tp process multiple orders in parallel. CompletableFuture allows us to combinee these 5 steps into one thread, each as a subtask that executes sequentially. This allows the main program to run many of these CompletableFutures in parallel. It uses ForkJoinPool internally to run these subtasks.<br/>
This is still clunky to use for large projects with much more logic between each step. Hence RxJava is used instead as it is feature rich and easy to read.

### Virtual Threads
In Java 21, there is Virtual threads:
- alternative implementation of Thread and ExecutorService. 
- OS doesn't know about them
  - jvm concept
  - stack lives on heap. regular thread needs to reserve stack space like 1MB per thread. with heap, you can use as little as you want.
- Cheap to create than platform threads.
Scenario: Lets say we want to get the price of 10000 products and update our DB. We have a for loop that creates 10 threads at a time. As the fetch and DB write are IO operations, most of the time is spent being idle. So CachedThreadPool might end up spawning more and more threads, which consumes memory. One solution is to use Reactive programming. The thread simply provides a callback to the reactive framework and ends. The reactive framework takes care of managing this and will invoke the callback method when it receives the response for the IO operation. The one downside is that reactive programming requires using another library like Spring Web Flux and is hard to read/debug. The solution is virtual threads that are natively supported by the Thread and Executor class.

Structured consistency is in experimental feature state. Helps with fork join and handle errors, race conditions, etc at parent level.

### Distributed Concurrency Control
Let's take a distributed movie booking application. User 1 and User 2 are trying to book seat 2. If their requests land on two different servers, none of the locking mechanisms described here will help.<br/>
<b>```synchronized``` and other locks are local to a process in a given machine.</b>. This is handled by concurrency control techniques at the DB layer. The options are:
- Optimistic Concurrency Control
- Pessimistic Concurrency Control
  
Learn more @ https://www.youtube.com/watch?v=D3XhDu--uoI

## Problems
Check out the README at https://github.com/harippriyas/multithreading/tree/main/multithreading
