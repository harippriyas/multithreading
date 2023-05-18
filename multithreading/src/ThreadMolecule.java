import java.util.concurrent.atomic.*;

public class ThreadMolecule {
	public AtomicBoolean needH1 = new AtomicBoolean(true);
	public AtomicBoolean needH2 = new AtomicBoolean(true);
	public AtomicBoolean needO = new AtomicBoolean(true);
	public AtomicInteger count = new AtomicInteger(0);
	public volatile StringBuffer molecule = new StringBuffer();
	public Object lock = new Object();
	
	public Runnable createThread(String type)
	{
		return new BuilderThread(type);
	}
	
	public synchronized void checkAndPrint()
	{
			if(!needH1.get() && !needH2.get() && !needO.get())
			{
				System.out.println(molecule.toString() + " ");
				count.incrementAndGet();
				needH1.set(true);
				needH2.set(true);
				needO.set(true);
				molecule.setLength(0);
			}
	}
	
	public synchronized void build(String type)
	{
		if(type.equals("H") && (needH1.get() || needH2.get()))
		{
			if(needH1.get())
			{
				molecule.append("H");
				needH1.set(false);
				notifyAll();
			}
			else if(needH2.get())
			{
				molecule.append("H");
				needH2.set(false);
				checkAndPrint();
				notifyAll();
			}
		}
		else if(type.equals("O") && needO.get())
		{
			molecule.append("O");
			needO.set(false);
			checkAndPrint();
			notifyAll();
		}
		else
		{
			try {
				wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}
	
	class BuilderThread implements Runnable
	{
		String type;
		public BuilderThread(String t)
		{
			type = t;
		}
		
		@Override
		public void run() {
			while(count.get() < 10)
			{
				build(type);
			}
			
		}
		
	}
	
	public static void main(String[] args)
	{
		ThreadMolecule obj = new ThreadMolecule();
		Thread t1 = new Thread(obj.createThread("H"));
		Thread t2 = new Thread(obj.createThread("H"));
		Thread t3 = new Thread(obj.createThread("O"));
		
		t1.start();
		t2.start();
		t3.start();
	}
	
}
