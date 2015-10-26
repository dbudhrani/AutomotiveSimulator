import java.util.ArrayList;
import java.util.List;


public class Core {

	public int id;
	//public List<Runnable> runnables;
	
	//public List<OsTask> tasks;
	public Scheduler scheduler;
	
	public OsTask currentTask;
	public boolean isBusy;
	
	public Core(int _id) {
		this.id = _id;
	//	this.runnables = new ArrayList<Runnable>();
		//this.tasks = new ArrayList<OsTask>();
		this.scheduler = new Scheduler();
	}
	
	public void addRunnable(Runnable _runnable) {
		//this.runnables.add(_runnable);
	}
	
	

}
