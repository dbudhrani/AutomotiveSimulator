import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;


public class Scheduler {

	public List<OsTask> tasks;
	public List<Event> events;
	public List<Log> logs;
	
	public Hashtable<Integer, Integer> periods = new Hashtable<Integer, Integer>();
	
	public int timer;
	
	OsTask currentTask;
	public int currentTaskRunningTimeStart;
	
	
	public Scheduler() {
		this.tasks = new ArrayList<OsTask>();
		this.events = new ArrayList<Event>();
		this.logs = new ArrayList<Log>();
		this.timer = 0;
	}

	public void addOsTask(OsTask _osTask) {
		this.tasks.add(_osTask);
	}
	
	public void init() {
		for (int i=0; i<tasks.size(); i++) {
			tasks.get(i).state = OsTaskState.READY;				
			//events.add(new Event(tasks.get(i).id, tasks.get(i).period, EventType.NEW_PERIOD_START));
			periods.put(tasks.get(i).id, tasks.get(i).period);
		}
		sortTasks();
		currentTask = tasks.get(0).state == OsTaskState.READY ? tasks.get(0) : null;
		events.add(new Event(currentTask.id, currentTask.execTime, EventType.TASK_FINISHED));
		sortEvents();
	}
	
	public void sortTasks() {
		Collections.sort(tasks);
	}
	
	public void sortEvents() {
		Collections.sort(events);
	}
	
	public void execute() {
		
		for (int i=0; i<events.size(); i++) {
			if (timer == 0 || events.get(i).eventType == EventType.TASK_FINISHED) {
				// execute current task, no preemption needed
				timer = events.get(i).time;
				logs.add(new Log(currentTask.id, timer, LogType.TASK_FINISHED, LogSeverity.NORMAL));
//				int nextPeriodStartOfTask = getNextPeriodStartOfTask(currentTask);
				int nextPeriodStartOfTask = getNextPeriod(currentTask);
				if (timer < nextPeriodStartOfTask) {
					currentTask.state = OsTaskState.WAITING;
				} else if ( timer == nextPeriodStartOfTask) {
//					currentTask.state = OsTaskState.READY;
//					currentTask.currentExecTime = 0;
//					sortTasks();
					setTaskToReady(currentTask, false);
				} else {
					// Deadline missed
					logs.add(new Log(currentTask.id, events.get(i).time, LogType.DEADLINE_MISSED, LogSeverity.CRITICAL));
				}
				
				// run new task
				currentTaskRunningTimeStart = timer;
//				updateCurrentTaskInList();
//				sortTasks();
//				setNextPeriodOfTask(currentTask);
//				currentTask = tasks.get(0).state == OsTaskState.READY ? tasks.get(0) : null;
//				setTaskFinishedEvent();
//				sortEvents();
				break;
			} else if (events.get(i).eventType == EventType.NEW_PERIOD_START) {
				
				
				
//				int taskId = events.get(i).taskId;
//				if (taskId != currentTask.id) {
////					for (int j=0; j<tasks.size(); j++) {
////						if (tasks.get(j).priority > currentTask.priority) {
////							// preempt current task!
////							timer = events.get(i).time;
////							switchTask(i, j);
////						} else {
////							// do nothing, continue executing the current task
////							if (tasks.get(j).state == OsTaskState.WAITING) {
////								tasks.get(j).state = OsTaskState.READY;
////								currentTask.currentExecTime = 0;
////							} else {
////								// Task j has missed the deadline!
////								logs.add(new Log(tasks.get(j).id, events.get(i).time, LogType.DEADLINE_MISSED, LogSeverity.CRITICAL));
////							}
////						}
////					}	
//					
//					setStateOfTask(taskId, OsTaskState.READY);
//					
//					sortTasks();
//					
//					OsTask eventTask = getTaskFromId(taskId);
//					if (eventTask.priority > currentTask.priority) {
//						timer = events.get(i).time;
//						// switch tasks
//					} else {
//						// do nothing, continue executing the current task
//					}
//					
//				} else {
//					// The current task is missing the deadline!
//					logs.add(new Log(currentTask.id, events.get(i).time, LogType.DEADLINE_MISSED, LogSeverity.CRITICAL));
//				}
			}
		}
				

	}
		
	private void switchTask(int eventPosition, int taskPosition) {
		OsTask newTask = tasks.get(taskPosition);
		Event newTaskEvent = events.get(eventPosition);
		logs.add(new Log(currentTask.id, newTaskEvent.time, LogType.TASK_INTERRUPTED, LogSeverity.NORMAL));
		logs.add(new Log(newTask.id, newTaskEvent.time, LogType.TASK_READY, LogSeverity.NORMAL));
		currentTask.currentExecTime += (timer);
	}
	
	

	private void updateCurrentTaskInList() {
		for (int i=0; i<tasks.size(); i++) {
			if (tasks.get(i).id == currentTask.id) {
				tasks.set(i, currentTask);
				break;
			}
		}
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
	
	private int getNextPeriodStartOfTask2(OsTask task) {
		return task.nextPeriod;
	}
	
	private void setNextPeriodOfTask(OsTask task) {
		for (int i=0; i<events.size(); i++) {
			if (events.get(i).taskId == task.id && events.get(i).eventType == EventType.NEW_PERIOD_START) {
				if (timer > task.period) {
					// the last if is a temporary workaround
					events.get(i).time = events.get(i).time + task.period;
				}
			}
		}
	}
	
	private void setStateOfTask(int taskId, OsTaskState newState) {
		for (int i=0; i<tasks.size(); i++) {
			if (tasks.get(i).id == taskId) {
				tasks.get(i).state = newState;
				break;
			}
		}
	}
	
	private void setTaskFinishedEvent() {
		for (int i=0; i<events.size(); i++) {
			if (events.get(i).eventType == EventType.TASK_FINISHED) {
				events.get(i).time = timer + (currentTask.execTime - currentTask.currentExecTime);
				break;
			}
		}
	}
	
	private OsTask getTaskFromId(int id) {
		for (int i=0; i<tasks.size(); i++) {
			if (tasks.get(i).id == id) {
				return tasks.get(i);
			}
		}
		return null;
	}
	
	private int getNextPeriod(OsTask task) {
		return periods.get(task.id);
	}
	
	private void setNextPeriod(OsTask task) {
		periods.put(task.id, periods.get(task.id) + task.period);
	}
	
	private void setTaskToReady(OsTask task, boolean isPreemption) {
		OsTask newTask = task;
		newTask.state = OsTaskState.READY;
		logs.add(new Log(task.id, timer, LogType.TASK_READY, LogSeverity.NORMAL));
		if (!isPreemption || task.state == OsTaskState.WAITING) {
			//W->Rdy || Run->Rdy
			newTask.currentExecTime = 0;
			setNextPeriod(newTask);
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
		setTaskFinishedEvent();
	}
	
	private void checkPreemption(OsTask candidateTask) {
		if (candidateTask.id == tasks.get(0).id) {
			if (candidateTask.priority > currentTask.priority) {
				setTaskToReady(currentTask, true);
				logs.add(new Log(currentTask.id, timer, LogType.TASK_INTERRUPTED, LogSeverity.NORMAL));
				setTaskToRunning(candidateTask);
			}
		}
	}
	
	
	
	private void comparePeriods() {
//		int i=0;
//		while (i<periods.size()) {
//			periods.
//			i++;
//		}
		
		
		for (Integer key : periods.keySet()) {
			Integer value = periods.get(key);
			int cmp = Integer.valueOf(timer).compareTo(value);
			if (cmp == 0) {
				
			} 
		}

		
	}
	
}
