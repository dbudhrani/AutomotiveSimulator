import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class Scheduler {

	public List<OsTask> tasks;
	
	public Scheduler() {
		this.tasks = new ArrayList<OsTask>();
	}

	public void addOsTask(OsTask _osTask) {
		this.tasks.add(_osTask);
	}
	
	public void start() {
		for (int i=0; i<tasks.size(); i++) {
			tasks.get(i).state = OsTaskStateEnum.READY;				
		}
		Collections.sort(tasks);
		String a = "";
	}
	
	
}
