import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeMap;


public class Scheduler {

	public List<OsTask> tasks;
	public List<Event> events;
	public List<Log> logs;
	public Hashtable<String, String> data;
	
	public double maxTime;
	
	public double timer;
	public double currentTaskRunningTimeStart;
	public double currentTaskRunningTimeFinish;
	
	public double currentSWComponentTimeStart;
	public double currentSWComponentTimeFinish;
	public double hyperperiodValue;
	
	int firstComponent = -1;
	public Hashtable<Integer, List<Double>> e2eDelays;
	
	public int currentSWComponent;
	public Hashtable<Integer, Double> swcomponentPeriods;
	public Hashtable<Integer, List<OsTask>> htSWCOsTasks;
	public NavigableMap<Integer, Double> swcomponentsStartTime;
	public Hashtable<Integer, Integer> SWComponentExecutionCounter;
	public Hashtable<Integer, Boolean> SWComponentInitiated;
	
	public double idleTime = 0;
	public double idleTimeStart = 0;
	public double idleTimeFinish = 0;
	
	OsTask currentTask;
	Core core;
	
	public Scheduler(Core _core) {
		this.tasks = new ArrayList<OsTask>();
		this.events = new ArrayList<Event>();
		this.logs = new ArrayList<Log>();
		this.data = new Hashtable<String, String>();
		this.timer = 0;
		this.core = _core;
		this.swcomponentPeriods = new Hashtable<Integer, Double>();
		this.hyperperiodValue = 0;
		this.htSWCOsTasks = new Hashtable<Integer, List<OsTask>>();
		this.swcomponentsStartTime = new TreeMap<Integer, Double>();
		this.currentSWComponent = -1;
		this.e2eDelays = new Hashtable<Integer, List<Double>>();
		this.SWComponentExecutionCounter = new Hashtable<Integer, Integer>();
		this.SWComponentInitiated = new Hashtable<Integer, Boolean>();
	}

	public void addOsTask(OsTask _osTask) {
		this.tasks.add(_osTask);
	}
	
	public void init() {
		System.out.println("Init - Core id: " + core.id);
		calculateHyperperiod();
		loadSWComponentsOsTasks();
		initSWCExecCounters();
		initSWCInitiated();
		loadSWComponentStartTime();
//		for (SWComponent c : core.ecu.arc.getSWComponents()) {
//			if (htSWCOsTasks.keySet().contains(c.id)) {
//				setApplicationStartEvent(c.id, swcomponentsStartTime.get(c.id));
//			}
//		}
		startSWComponent(getSWComponentFromId(firstComponent), 0);
	}
	
	public void sortTasks() {
//		Collections.sort(tasks);
		Collections.sort(htSWCOsTasks.get(currentSWComponent));
	}
	
	public void sortEvents() {
		Collections.sort(events);
	}
	
	public void execute2(int _maxTime) {
		System.out.println("Execute - Core id: " + core.id);
		this.maxTime = _maxTime;
		while (timer < maxTime) {
			Event nextApplicationStart = null;
			for (int i=0; i<events.size(); i++) {
				if (events.get(i).eventType == EventType.NEW_APPLICATION_START) {
					nextApplicationStart = events.get(i);
					break;
				}
			}
			if (currentTaskRunningTimeFinish <= nextApplicationStart.time) {
				taskFinished2();
				sortTasks();
				if (htSWCOsTasks.get(currentSWComponent).get(0).state == OsTaskState.READY) {
					setTaskToRunning2(htSWCOsTasks.get(currentSWComponent).get(0));
				} else {
//					double applicationStartTime = -1;
//					for (int i=0; i<events.size(); i++) {
//						if (events.get(i).eventType == EventType.NEW_APPLICATION_START && events.get(i).taskId == currentSWComponent) {
//							applicationStartTime = events.get(i).time - hyperperiodValue;
//							break;
//						}
//					}
//					if (!e2eDelays.containsKey(currentSWComponent)) {
//						List<Double> delays = new ArrayList<Double>();
//						delays.add(currentTaskRunningTimeFinish - applicationStartTime);
//						e2eDelays.put(currentSWComponent, delays);
//					} else {
//						e2eDelays.get(currentSWComponent).add(currentTaskRunningTimeFinish - applicationStartTime);
//					}
					
					// make idle
					
					logs.add(new Log(-1, timer, LogType.START_IDLE, "", LogSeverity.NORMAL));
					idleTimeStart = timer;
//					runNextTask2();
					timer = nextApplicationStart.time;
					idleTimeFinish = timer;
					idleTime += (idleTimeFinish - idleTimeStart);
					logs.add(new Log(-1, timer, LogType.FINISH_IDLE, "Current idle time = " + (idleTimeFinish - idleTimeStart) + ". Total = " + idleTime, LogSeverity.NORMAL));
					logs.add(new Log(currentSWComponent, timer, LogType.APPLICATION_FINISHED, "", LogSeverity.NORMAL));
					startSWComponent(getSWComponentFromId(nextApplicationStart.taskId), timer);
				}
			} else {
				logs.add(new Log(currentSWComponent, timer, LogType.APPLICATION_FINISHED, "", LogSeverity.NORMAL));
				interruptTask();
				// start new application
				startSWComponent(getSWComponentFromId(nextApplicationStart.taskId), nextApplicationStart.time);
			}
		}
		
		finishExecution();
	}
	
	private double getNextHyperperiodStart() {
		for (int i=0; i<events.size(); i++) {
			if (events.get(i).eventType == EventType.NEW_HYPERPERIOD_START) {
				return events.get(i).time;
			}
		}
		// This should never happen
		return -1;
	}
	
	private void setNewHyperperiod() {
		for (int i=0; i<events.size(); i++) {
			if (events.get(i).eventType == EventType.NEW_HYPERPERIOD_START) {
				events.get(i).time += hyperperiodValue;
				break;
			}
		}
	}
	
	private void finishExecution() {
		data.put("idle", Double.valueOf(((double) idleTime/(double) maxTime)*100).toString());
		data.put("e2e0", Double.valueOf(core.ecu.arc.getSWComponents().get(0).e2eDelay).toString());
		data.put("e2e1", Double.valueOf(core.ecu.arc.getSWComponents().get(1).e2eDelay).toString());
		data.put("e2e2", Double.valueOf(core.ecu.arc.getSWComponents().get(2).e2eDelay).toString());
		data.put("e2e3", Double.valueOf(core.ecu.arc.getSWComponents().get(3).e2eDelay).toString());
		//Util.printLog(logs, data);
//		System.exit(-1);
	}
	
	public boolean isTaskSameCore(int _id) {
		for (OsTask t : tasks) {
			if (t.id == _id) {
				return true;
			}
		}
		return false;
	}
	
	public void coreReceivedMessage(Message _msg) {
		if (isTaskSameCore(_msg.dst)) {
			this.logs.add(new Log(_msg.dst, _msg.updTs, LogType.MESSAGE_RECEIVED, "Message source: task " + _msg.src, LogSeverity.NORMAL));			
		}
		core.inputMessages.remove(_msg);
	}
	
	public void addDelayToSWComponent() {
		// TODO change
		List<Runnable> tempRunnables = new ArrayList<Runnable>();
		for (Runnable r1 : currentTask.runnables) {
			for (SWComponent c : core.ecu.arc.getSWComponents()) {
					for (Runnable r2 : c.runnables) {
						if (r1.id == r2.id && !tempRunnables.contains(r1)) {
//							c.addDelay(delay);
							tempRunnables.add(r1);
//							break;
						}
					}	
			}
		}
		
		double delay = 0;
		for (Runnable r : tempRunnables) {
			delay += r.getExecTime();
		}
		
//		_component.addDelay((getNextPeriodStartOfTask(currentTask) - currentTask.period) +  delay);

	}
	
	public void addDelayComponent() {
		List<SWComponent> tempComponents = new ArrayList<SWComponent>();
		for (Runnable r1 : currentTask.runnables) {
			for (SWComponent c : core.ecu.arc.getSWComponents()) {
					for (Runnable r2 : c.runnables) {
						if (r1.id == r2.id && !tempComponents.contains(c)) {
//							c.addDelay(delay);
							tempComponents.add(c);
//							break;
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
	
	private double getNextPeriodStartOfTask(OsTask task) {
		for (int i=0; i<events.size(); i++) {
			if (events.get(i).taskId == task.id && events.get(i).eventType == EventType.NEW_PERIOD_START) {
				return events.get(i).time;
			}
		}
		// This should never happen
		return -1;
	}

	public void calculateHyperperiod() {
		for (Integer k : swcomponentPeriods.keySet()) {
			hyperperiodValue += swcomponentPeriods.get(k);
		}
	}
	
	public void loadSWComponentPeriod(Integer SWComponentId, double period) {
		this.swcomponentPeriods.put(SWComponentId, period);
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
	
	public void loadSWComponentStartTime() {
		int lastKey = -1;
		for (Integer k : swcomponentPeriods.keySet()) {
			if (!swcomponentsStartTime.containsKey(k)) {
				if (lastKey != -1) {
					double _time = swcomponentsStartTime.get(lastKey) + swcomponentPeriods.get(lastKey);
					swcomponentsStartTime.put(k, _time);
					events.add(new Event(k, _time, EventType.NEW_APPLICATION_START));
				} else {
					this.firstComponent = k;
					swcomponentsStartTime.put(k, 0.0);
					events.add(new Event(k, 0.0, EventType.NEW_APPLICATION_START));					
				}
				lastKey = k;
			}
		}
	}
	
	public void setSWComponentToReady(SWComponent c) {
		currentSWComponent = c.id;
		List<OsTask> tasks = htSWCOsTasks.get(c.id);
		boolean allFinished = true;
		for (OsTask t : tasks) {
			if (t.state != OsTaskState.FINISHED) {
				allFinished = false;
				break;
			}
		}
		
		if (allFinished) {
			for (OsTask t : htSWCOsTasks.get(c.id)) {
				setTaskToReady2(t);
			}
		}
		
		sortTasks();
		setTaskToRunning2(htSWCOsTasks.get(c.id).get(0));
		
	}
	
	public void taskFinished2() {
		timer = currentTaskRunningTimeFinish;
		double taskDeadline = 99999999;
		for (int i=0; i<events.size(); i++) {
			if (events.get(i).eventType == EventType.NEW_PERIOD_START && events.get(i).taskId == currentTask.id) {
				taskDeadline = events.get(i).time;
				break;
			}
		}
		if (timer <= taskDeadline) {
			currentTask.state = OsTaskState.FINISHED;
			currentTask.currentExecTime = currentTask.execTime;
			logs.add(new Log(currentTask.id, timer, LogType.TASK_FINISHED, "Total execution time: " + currentTask.execTime + ". Current execution time: " + currentTask.currentExecTime, LogSeverity.NORMAL));
			
			for (int i=0; i<htSWCOsTasks.get(currentSWComponent).size(); i++) {
				if (currentTask.id == htSWCOsTasks.get(currentSWComponent).get(i).id) {
					htSWCOsTasks.get(currentSWComponent).set(i, currentTask);
					break;
				}
			}
			
			double applicationStartTime = -1;
			for (int i=0; i<events.size(); i++) {
				if (events.get(i).eventType == EventType.NEW_APPLICATION_START && events.get(i).taskId == currentSWComponent) {
					applicationStartTime = events.get(i).time - hyperperiodValue;
					break;
				}
			}
						
			double delayCurrentExec = currentTaskRunningTimeFinish - applicationStartTime;
			if (SWComponentExecutionCounter.get(currentSWComponent) == 0) {
				if (!e2eDelays.containsKey(currentSWComponent)) {
					List<Double> delays = new ArrayList<Double>();
					delays.add(delayCurrentExec);
					e2eDelays.put(currentSWComponent, delays);
				} else {
					e2eDelays.get(currentSWComponent).add(delayCurrentExec);
				}
			} else {
				double delay = SWComponentExecutionCounter.get(currentSWComponent) * swcomponentPeriods.get(currentSWComponent) + delayCurrentExec;
				if (!e2eDelays.containsKey(currentSWComponent)) {
					List<Double> delays = new ArrayList<Double>();
					delays.add(delay);
					e2eDelays.put(currentSWComponent, delays);
				} else {
					e2eDelays.get(currentSWComponent).add(delay);
				}
			}
			
			
			currentTask.createMessage(timer);
			if (!isTaskSameCore(currentTask.getMessage().dst)) {
				core.addMessageToOutputQueue(currentTask.getMessage());
			}	
		} else {
			logs.add(new Log(currentTask.id, taskDeadline, LogType.DEADLINE_MISSED, "", LogSeverity.CRITICAL));
		}
	}
	
	public void runNextTask2() {
		Event firstEvent = null;
		for (int i=0; i<events.size(); i++) {
			if (events.get(i).eventType == EventType.NEW_PERIOD_START) {
				firstEvent = events.get(i);
				break;
			}
		}
		timer = firstEvent.time;
		OsTask nextTask = null;
		for (int i=0; i<tasks.size(); i++) {
			if ( tasks.get(i).id == firstEvent.taskId) {
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
		setTaskToReady2(nextTask);
		setTaskToRunning2(nextTask);
	}
	
	public void setTaskToReady2(OsTask t) {
		OsTask newTask = t;
		newTask.state = OsTaskState.READY;
		logs.add(new Log(t.id, timer, LogType.TASK_READY, "", LogSeverity.NORMAL));
		newTask.currentExecTime = 0;
		for (int i=0; i<htSWCOsTasks.get(currentSWComponent).size(); i++) {
			if (t.id == htSWCOsTasks.get(currentSWComponent).get(i).id) {
				htSWCOsTasks.get(currentSWComponent).set(i, newTask);
				break;
			}
		}
	}
	
	public void interruptTask() {
//		currentTask.state = OsTaskState.READY;
		double previousExecTime = currentTask.currentExecTime;
		currentTask.currentExecTime += (timer - currentTaskRunningTimeStart);
		timer += (currentTask.currentExecTime - previousExecTime);
		setTaskToReady2(currentTask);
		logs.add(new Log(currentTask.id, timer, LogType.TASK_INTERRUPTED, "", LogSeverity.NORMAL));

	}
	
	public void setTaskToRunning2(OsTask t) {
		currentTask = t;
		currentTask.state = OsTaskState.RUNNING;
		List<Runnable> _runnables = findRunnables(t);
		StringBuilder b = new StringBuilder();
		for (int i=0; i<_runnables.size(); i++) {
			b.append(_runnables.get(i).id + ", ");
		}
		logs.add(new Log(t.id, timer, LogType.TASK_EXECUTING, "Runnables: " + b.toString(), LogSeverity.NORMAL));
		if (currentTask.currentExecTime == 0) {
			currentTask.computeTaskTime(_runnables);	
		}
		currentTaskRunningTimeStart = timer;
		currentTaskRunningTimeFinish = timer + (currentTask.execTime - currentTask.currentExecTime);
		
	}
	
	public void startSWComponent(SWComponent c, double _time) {
		currentSWComponent = c.id;
		
//		for (OsTask t : htSWCOsTasks.get(c.id)) {
//			setTaskToReady2(t);
//		}
		
		timer = _time;
		
		System.out.println("Starting SW component " + c.id + " at time " + timer);
		
		setApplicationStartEvent(c.id, timer + hyperperiodValue);
		logs.add(new Log(c.id, timer, LogType.APPLICATION_START, "", LogSeverity.NORMAL));

		boolean firstTimeComponent = false;
		if (!SWComponentInitiated.get(currentSWComponent)) {
			SWComponentExecutionCounter.put(currentSWComponent, 0);
			for (int i=0; i<htSWCOsTasks.get(c.id).size(); i++) {
				setTaskToReady2(htSWCOsTasks.get(c.id).get(i));
				setTaskNewPeriod(htSWCOsTasks.get(c.id).get(i), timer + htSWCOsTasks.get(c.id).get(i).period);
			}
			firstTimeComponent = true;
		}
		
		boolean allFinished = true;
		for (int i=0; i<htSWCOsTasks.get(c.id).size(); i++) {
			if (htSWCOsTasks.get(c.id).get(i).state != OsTaskState.FINISHED) {
				allFinished = false;
				System.out.println("Core: " + core.id + ". SWComponent: " + currentSWComponent + ". Task: " + htSWCOsTasks.get(c.id).get(i).id + ". State: " + htSWCOsTasks.get(c.id).get(i).state);
				if (!firstTimeComponent) {
					SWComponentExecutionCounter.put(currentSWComponent, SWComponentExecutionCounter.get(currentSWComponent) + 1);	
				}
				break;
			}
		}

		if (firstTimeComponent) {
			SWComponentInitiated.put(currentSWComponent, true);
		}
			
		if (allFinished) {
			SWComponentExecutionCounter.put(currentSWComponent, 0);
			for (int i=0; i<htSWCOsTasks.get(c.id).size(); i++) {
				setTaskToReady2(htSWCOsTasks.get(c.id).get(i));
				setTaskNewPeriod(htSWCOsTasks.get(c.id).get(i), timer + htSWCOsTasks.get(c.id).get(i).period);
			}	
		}
		
		
		
		sortTasks();		
		
		setTaskToRunning2(htSWCOsTasks.get(c.id).get(0));
	
	}
	
	public SWComponent getSWComponentFromId(int id) {
		for (SWComponent c : core.ecu.arc.getSWComponents()) {
			if (c.id == id) {
				return c;
			}
		}
		return null;
	}
	
	public void setApplicationStartEvent(int _id, double _time) {
		for (int i=0; i<events.size(); i++) {
			if (events.get(i).eventType == EventType.NEW_APPLICATION_START && events.get(i).taskId == _id) {
				events.remove(i);
			}
		}
		events.add(new Event(_id, _time, EventType.NEW_APPLICATION_START));
		sortEvents();
	}
	
	public void setTaskNewPeriod(OsTask _task, double _time) {
		for (int i=0; i<events.size(); i++) {
			if (events.get(i).eventType == EventType.NEW_PERIOD_START && events.get(i).taskId == _task.id) {
				events.remove(i);
			}
		}
		events.add(new Event(_task.id, _time, EventType.NEW_PERIOD_START));
		sortEvents();
	}
	
	public List<Runnable> findRunnables(OsTask t) {
		List<Runnable> runnables = new ArrayList<Runnable>();
		for (Runnable r1 : t.runnables) {
			for (SWComponent c : core.ecu.arc.getSWComponents()) {
				if (c.id == currentSWComponent) {
					for (Runnable r2 : c.runnables) {
						if (r1.id == r2.id) {
							runnables.add(r1);
						}
					}
					break;
				}
			}
		}
		return runnables;
	}

	public void initSWCExecCounters() {
		for (Integer k : htSWCOsTasks.keySet()) {
			SWComponentExecutionCounter.put(k, 0);
		}
	}
	
	public void initSWCInitiated() {
		for (Integer k : htSWCOsTasks.keySet()) {
			SWComponentInitiated.put(k, false);
		}
	}
	
}
