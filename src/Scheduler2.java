import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;


public class Scheduler2 {

	public List<OsTask> tasks;
	public List<Event> events;
	public List<Log> logs;
	public Hashtable<String, String> data;
	
	public int maxTime;
	
	public int timer;
	public int currentTaskRunningTimeStart;
	public int currentTaskRunningTimeFinish;
	
	public int idleTime = 0;
	public int idleTimeStart = 0;
	public int idleTimeFinish = 0;
	
	OsTask currentTask;
	Core core;
	
	public Scheduler2(Core _core) {
		this.tasks = new ArrayList<OsTask>();
		this.events = new ArrayList<Event>();
		this.logs = new ArrayList<Log>();
		this.data = new Hashtable<String, String>();
		this.timer = 0;
		this.core = _core;
	}

	public void addOsTask(OsTask _osTask) {
		this.tasks.add(_osTask);
	}
	
	public void init() {
		for (int i=0; i<tasks.size(); i++) {
//			tasks.get(i).state = OsTaskState.READY;
//			events.add(new Event(tasks.get(i).id, tasks.get(i).period, EventType.NEW_PERIOD_START));
			events.add(new Event(tasks.get(i).id, 0, EventType.NEW_PERIOD_START));
			setTaskToReady(tasks.get(i), false);
		}
		setTaskToRunning(tasks.get(0));
	}
	
	public void sortTasks() {
		Collections.sort(tasks);
	}
	
	public void sortEvents() {
		Collections.sort(events);
	}

	public void execute(int _maxTime) {
		this.maxTime = _maxTime;
		while (timer < maxTime) {
			if (currentTaskRunningTimeFinish <= events.get(0).time) {
				taskFinished();
//				checkNewPeriods();
				if (tasks.get(0).state == OsTaskState.READY) {
					setTaskToRunning(tasks.get(0));
				} else {
					// do stuff
					logs.add(new Log(-1, timer, LogType.START_IDLE, LogSeverity.NORMAL));
					idleTimeStart = timer;
					runNextTask();
				}
			} else {
				checkPreemption();
			}	
		}

		// print log
		finishExecution();
	}
	
	private int getNextPeriodStartOfTask(OsTask task) {
		for (int i=0; i<events.size(); i++) {
			if (events.get(i).taskId == task.id && events.get(i).eventType == EventType.NEW_PERIOD_START) {
				return events.get(i).time;
			}
		}
		// This should never happen
		return -1;
	}
	
	private void setNextPeriod(OsTask task) {
		// change to list of events
		for (int i=0; i<events.size(); i++) {
			if (task.id == events.get(i).taskId && events.get(i).eventType == EventType.NEW_PERIOD_START) {
				events.get(i).time += task.period;
				break;
			}
		}
	}
	
	private void setTaskToWaiting(OsTask task) {
		OsTask newTask = task;
		newTask.state = OsTaskState.WAITING;
		for (int i=0; i<tasks.size(); i++) {
			if (task.id == tasks.get(i).id) {
				tasks.set(i, newTask);
				break;
			}
		}
		sortTasks();
	}
	
	private void setTaskToReady(OsTask task, boolean isPreemption) {
		OsTask newTask = task;
		newTask.state = OsTaskState.READY;
		logs.add(new Log(task.id, timer, LogType.TASK_READY, LogSeverity.NORMAL));
		if (!isPreemption || task.state == OsTaskState.WAITING) {
			//W->Rdy || Run->Rdy
			int t = -1;
			for (int i=0; i<events.size(); i++) {
				if (events.get(i).taskId == task.id) {
					t = events.get(i).time;
					break;
				}
			}
			if (timer == 0 || (timer == t)) {
				setNextPeriod(newTask);				
			}
			newTask.currentExecTime = 0;
			sortEvents();
		} else {
			// preemption
			newTask.currentExecTime += (timer - currentTaskRunningTimeStart);
		}
		for (int i=0; i<tasks.size(); i++) {
			if (task.id == tasks.get(i).id) {
				tasks.set(i, newTask);
				break;
			}
		}
		sortTasks();
	}
	
	private void setTaskToRunning(OsTask task) {
		currentTask = task;
		currentTask.state = OsTaskState.RUNNING;
		logs.add(new Log(task.id, timer, LogType.TASK_EXECUTING, LogSeverity.NORMAL));
		currentTaskRunningTimeStart = timer;
		currentTaskRunningTimeFinish = timer + (currentTask.wcet - currentTask.currentExecTime);
	}
	
	private void makePreemption(OsTask candidateTask) {
		if (candidateTask.id == tasks.get(0).id) {
			if (candidateTask.priority > currentTask.priority) {
				setTaskToReady(currentTask, true);
				logs.add(new Log(currentTask.id, timer, LogType.TASK_INTERRUPTED, LogSeverity.NORMAL));
				setTaskToRunning(candidateTask);
			}
		}
	}
	
	private void taskFinished() {
		timer = currentTaskRunningTimeFinish;
		currentTask.currentExecTime = currentTask.wcet;
		currentTask.createMessage();
		if (!isTaskSameCore(currentTask.getMessage().dst)) {
			core.addMessageToOutputQueue(currentTask.getMessage());
		}
		logs.add(new Log(currentTask.id, timer, LogType.TASK_FINISHED, LogSeverity.NORMAL));
		int nextPeriodStartOfTask = getNextPeriodStartOfTask(currentTask);
		if (timer < nextPeriodStartOfTask) {
			setTaskToWaiting(currentTask);
		} else if (timer == nextPeriodStartOfTask) {
			setTaskToReady(currentTask, false);
		} 
		else {
			// Deadline missed
			logs.add(new Log(currentTask.id, nextPeriodStartOfTask, LogType.DEADLINE_MISSED, LogSeverity.CRITICAL));
			data.put("idle", Double.valueOf((double) idleTime/(double) maxTime).toString());
			Util.printLog(logs, data);
			System.exit(-1);
		}
	}
	
	private void checkNewPeriods() {
		int firstTime = events.get(0).time;
		timer = firstTime;
		for (int i=0; i<events.size(); i++) {
			if (events.get(i).time == firstTime) {
				for (int j=0; j<tasks.size(); j++) {
					if (tasks.get(j).id == events.get(i).taskId) {
						if (tasks.get(j).wcet == tasks.get(j).currentExecTime) {
							setTaskToReady(tasks.get(j), false);	
						} else {
							logs.add(new Log(currentTask.id, firstTime, LogType.DEADLINE_MISSED, LogSeverity.CRITICAL));
							finishExecution();
						}
						break;
					}
				}
			} else {
				break;
			}
		}
	}
	
	private void checkPreemption() {
		OsTask priorityTask = tasks.get(0);
		checkNewPeriods();
		if (tasks.get(0).id != priorityTask.id) {
			makePreemption(tasks.get(0));
		}
	}
	
	private void runNextTask() {
		Event firstEvent = events.get(0);
		timer = firstEvent.time;
		OsTask nextTask = null;
		for (int i=0; i<tasks.size(); i++) {
			if (tasks.get(i).id == firstEvent.taskId) {
				nextTask = tasks.get(i);
			}
		}
		for (int i=0; i<events.size(); i++) {
			if (events.get(i).time == firstEvent.time) {
				for (int j=0; j<tasks.size(); j++) {
					if (tasks.get(j).id == events.get(i).taskId) {
						if (tasks.get(j).priority > nextTask.priority) {
							nextTask = tasks.get(j);
						}
						break;
					}
				}
			} else {
				break;
			}
		}
		logs.add(new Log(-1, timer, LogType.FINISH_IDLE, LogSeverity.NORMAL));
		idleTimeFinish = timer;
		idleTime += (idleTimeFinish - idleTimeStart);
		setTaskToRunning(nextTask);
	}
	
	private void finishExecution() {
		data.put("idle", Double.valueOf((double) idleTime/(double) maxTime).toString());
		Util.printLog(logs, data);
		System.exit(-1);
	}
	
	public boolean isTaskSameCore(int _id) {
		for (OsTask t : tasks) {
			if (t.id == _id) {
				return true;
			}
		}
		return false;
	}
	
	public void taskReceivedMessage(int _taskId) {
		
	}
	
}
