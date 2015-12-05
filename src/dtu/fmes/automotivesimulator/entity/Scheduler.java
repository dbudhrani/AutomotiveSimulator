package dtu.fmes.automotivesimulator.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;

import dtu.fmes.automotivesimulator.entity.enumeration.EventType;
import dtu.fmes.automotivesimulator.entity.enumeration.LogSeverity;
import dtu.fmes.automotivesimulator.entity.enumeration.LogType;
import dtu.fmes.automotivesimulator.entity.enumeration.OsTaskState;



public class Scheduler {

	public List<OsTask> tasks;
	public List<Event> events;
	public List<Log> logs;
	public Hashtable<String, String> data;
	public double maxTime;
	public double timer;
	public double currentTaskRunningTimeStart;
	public double currentTaskRunningTimeFinish;
	public double idleTime = 0;
	public double idleTimeStart = 0;
	public double idleTimeFinish = 0;
	public Hashtable<Integer, List<OsTask>> htSWCOsTasks;
	public Hashtable<Integer, List<Double>> e2eDelays;
	public List<Integer> swComponentIds;
	OsTask currentTask;
	Core core;
	
	public Scheduler(Core _core) {
		this.tasks = new ArrayList<OsTask>();
		this.events = new ArrayList<Event>();
		this.logs = new ArrayList<Log>();
		this.data = new Hashtable<String, String>();
		this.timer = 0;
		this.core = _core;
		this.htSWCOsTasks = new Hashtable<Integer, List<OsTask>>();
		this.e2eDelays = new Hashtable<Integer, List<Double>>();
		this.swComponentIds = new ArrayList<Integer>();
	}

	public void addOsTask(OsTask _osTask) {
		this.tasks.add(_osTask);
	}
	
	public void init() {
		loadSWComponentsOsTasks();
		for (int i=0; i<tasks.size(); i++) {
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
				if (tasks.get(0).state == OsTaskState.READY) {
					setTaskToRunning(tasks.get(0));
				} else {
					logs.add(new Log(-1, timer, LogType.START_IDLE, "", LogSeverity.NORMAL));
					idleTimeStart = timer;
					runNextTask(true);
				}
			} else {
				checkPreemption();
			}	
		}

		data.put("idle", Double.valueOf(((double) idleTime/(double) maxTime)*100).toString());
	}
	
	private double getNextPeriodStartOfTask(OsTask task) {
		for (int i=0; i<events.size(); i++) {
			if (events.get(i).taskId == task.id && events.get(i).eventType == EventType.NEW_PERIOD_START) {
				return events.get(i).time;
			}
		}

		return -1;
	}
	
	private void setNextPeriod(OsTask task) {
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
		logs.add(new Log(task.id, timer, LogType.TASK_WAITING, "", LogSeverity.NORMAL));
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
		logs.add(new Log(task.id, timer, LogType.TASK_READY, "", LogSeverity.NORMAL));
		if (!isPreemption || task.state == OsTaskState.WAITING) {
			//W->Rdy || Run->Rdy
			double t = -1;
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
		
		if (timer > 0 && !isPreemption) {
			task.incrementPeriodCounter();
		}
		
		sortTasks();
	}
	
	private void setTaskToRunning(OsTask task) {
		currentTask = task;
		currentTask.state = OsTaskState.RUNNING;		
		if (currentTask.currentExecTime == 0) {
			currentTask.computeTaskTime();	
		}
		currentTaskRunningTimeStart = timer;
		currentTaskRunningTimeFinish = timer + (currentTask.execTime - currentTask.currentExecTime);
		logs.add(new Log(task.id, timer, LogType.TASK_EXECUTING, "Execution time: " + currentTask.execTime + ". Estimated finishing time: " + currentTaskRunningTimeFinish, LogSeverity.NORMAL));
	}
	
	private void makePreemption(OsTask candidateTask) {
		if (candidateTask.id == tasks.get(0).id) {
			if (candidateTask.priority > currentTask.priority) {
				setTaskToReady(currentTask, true);
				logs.add(new Log(currentTask.id, timer, LogType.TASK_INTERRUPTED, "Current exec time: " + currentTask.currentExecTime, LogSeverity.NORMAL));
				setTaskToRunning(candidateTask);
			}
		}
	}
	
	private void taskFinished() {
		timer = currentTaskRunningTimeFinish;
		currentTask.currentExecTime = currentTask.execTime;
		logs.add(new Log(currentTask.id, timer, LogType.TASK_FINISHED, "", LogSeverity.NORMAL));
		if (!currentTask.firstPeriodExecuted) {
			addDelayComponent();	
			currentTask.firstPeriodExecuted = true;
		}

		double nextPeriodStartOfTask = getNextPeriodStartOfTask(currentTask);
		List<SWComponent> components = getSWComponentsFromCurrentTask();
		for (SWComponent c : components) {
			core.ecu.arc.addStartTime(c, currentTask.periodCounter, nextPeriodStartOfTask - currentTask.period);
			core.ecu.arc.addFinishTime(c, currentTask.periodCounter, timer);
		}
		
		if (timer < nextPeriodStartOfTask) {
			sendMessages();
			setTaskToWaiting(currentTask);
		} else if (timer == nextPeriodStartOfTask) {
			sendMessages();
			setTaskToReady(currentTask, false);
		} 
		else {
			// Deadline missed
			logs.add(new Log(currentTask.id, nextPeriodStartOfTask, LogType.DEADLINE_MISSED, "", LogSeverity.CRITICAL));
			setTaskToReady(currentTask, false);
			runNextTask(false);
		}
	}
	
	private void checkNewPeriods() {
		double firstTime = events.get(0).time;
		timer = firstTime;
		for (int i=0; i<events.size(); i++) {
			if (events.get(i).time == firstTime) {
				for (int j=0; j<tasks.size(); j++) {
					if (tasks.get(j).id == events.get(i).taskId) {
						if ((tasks.get(j).execTime != 0 && tasks.get(j).currentExecTime != 0) && (tasks.get(j).execTime == tasks.get(j).currentExecTime)) {
							setTaskToReady(tasks.get(j), false);	
						} else {
							logs.add(new Log(tasks.get(j).id, firstTime, LogType.DEADLINE_MISSED, "", LogSeverity.CRITICAL));
							setTaskToReady(tasks.get(j), false);
							runNextTask(false);
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
	
	private void runNextTask(boolean _isIdle) {
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
		if (_isIdle) {
			idleTimeFinish = timer;
			idleTime += (idleTimeFinish - idleTimeStart);
			logs.add(new Log(-1, timer, LogType.FINISH_IDLE, "Current idle time = " + (idleTimeFinish - idleTimeStart) + ". Total = " + idleTime, LogSeverity.NORMAL));
		}
		setTaskToReady(nextTask, false);
		setTaskToRunning(nextTask);
	}
	
	public boolean isTaskSameCore(int _id) {
		for (OsTask t : tasks) {
			if (t.id == _id) {
				return true;
			}
		}
		return false;
	}
	
	public void addDelayComponent() {
		List<SWComponent> tempComponents = new ArrayList<SWComponent>();
		for (Runnable r1 : currentTask.runnables) {
			for (SWComponent c : core.ecu.arc.getSWComponents()) {
					for (Runnable r2 : c.runnables) {
						if (r1.id == r2.id && !tempComponents.contains(c)) {
							tempComponents.add(c);
						}
					}	
			}
		}
		
		double delay = timer - (getNextPeriodStartOfTask(currentTask) - currentTask.period);
		for (SWComponent c : tempComponents) {
			if (delay > c.e2eDelay) {
				c.e2eDelay = delay;
			}
		}
	}
	
	public void addSWComponentId(int id) {
		this.swComponentIds.add(id);
	}
	
	public void sendMessages() {
		for (Runnable r : currentTask.runnables) {
			if (r.messageDst != -1) {
				OsTask t = findTaskOfRunnable(r.messageDst);
				if (!isTaskSameCore(t.id)) {
					Message msg = currentTask.createMessage(timer, r.messagePriority, r.messageSize, t.id, r.messageExtendedIdentifier);
					core.addMessageToOutputQueue(msg);
				}	
			} else {
				Message msg = currentTask.createMessage(timer, r.messagePriority, r.messageSize, -1, r.messageExtendedIdentifier);
				core.addMessageToOutputQueue(msg);
			}
		}
		currentTask.clearMessages();	
	}
	
	public OsTask findTaskOfRunnable(int _runnableId) {
		for (ECU e : core.ecu.arc.getECUs()) {
			for (Core c : e.cores) {
				for (OsTask t : c.scheduler.tasks) {
					for (Runnable r : t.runnables) {
						if (r.id == _runnableId) {
							return t;
						}
					}
				}		
			}
		}
		return null;
	}
	
	public List<SWComponent> getSWComponentsFromCurrentTask() {
		List<SWComponent> tmp = new ArrayList<SWComponent>();
		for (Runnable r1 : currentTask.runnables) {
			for (SWComponent c : core.ecu.arc.getSWComponents()) {
				for (Runnable r2 : c.runnables) {
					if (r1.id == r2.id && !tmp.contains(c)) {
						tmp.add(c);
					}
				}
			}
		}
		return tmp;
	}
	
	public void loadSWComponentsOsTasks() {
		for (OsTask t : tasks) {
			for (Runnable r1 : t.runnables) {
				for (SWComponent c : core.ecu.arc.getSWComponents()) {
					for (Runnable r2 : c.runnables) {
						if (r1.id == r2.id) {
							if (!this.htSWCOsTasks.containsKey(c.id)) {
								List<OsTask> tasks = new ArrayList<OsTask>();
								tasks.add(t);
								this.htSWCOsTasks.put(c.id, tasks);
							} else {
								if (!this.htSWCOsTasks.get(c.id).contains(t)) {
									this.htSWCOsTasks.get(c.id).add(t);
								}
							}
						}
					}
				}
			}
		}
	}
	
}
