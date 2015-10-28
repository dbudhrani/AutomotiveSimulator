import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class Scheduler {

	public List<OsTask> tasks;
	public List<Event> events;
	public int timer;
	
	public Scheduler() {
		this.tasks = new ArrayList<OsTask>();
		this.timer = 0;
	}

	public void addOsTask(OsTask _osTask) {
		this.tasks.add(_osTask);
	}
	
	public void init() {
		for (int i=0; i<tasks.size(); i++) {
			tasks.get(i).state = OsTaskStateEnum.READY;				
			events.add(new Event(tasks.get(i).id, EventType.NEW_PERIOD, 0));
		}
		sortTasks();
		sortEvents();
	}
	
	public void sortTasks() {
		Collections.sort(tasks);
	}
	
	public void sortEvents() {
		Collections.sort(events);
	}
	
	public void execute() {
		OsTask currentTask = tasks.get(0).state == OsTaskStateEnum.READY ? tasks.get(0) : null;
		if (currentTask != null) {
			currentTask.state = OsTaskStateEnum.RUNNING;
			tasks.get(0).state = OsTaskStateEnum.RUNNING;
			//tasks.remove(0);
			sortTasks();
		}
		
		//timer += currentTask.wcet;
		events.add(new Event(currentTask.id, EventType.TASK_FINISHED, timer += currentTask.wcet));
		//currentTask.periodNext = (((int) timer/currentTask.period) + 1) * currentTask.period;
		changeNextPeriod(currentTask.id, (((int) timer/currentTask.period) + (timer % currentTask.period == 0 ? 0 : 1)) * currentTask.period);
	}
	
	private void changeNextPeriod(int taskId, int nextPeriod) {
		for (int i=0; i<events.size(); i++) {
			if (events.get(i).taskId == taskId && events.get(i).eventType == EventType.NEW_PERIOD) {
				events.get(i).time = nextPeriod;
				break;
			}
		}
	}
	
}
