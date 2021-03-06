package dtu.fmes.automotivesimulator.entity;

import java.util.ArrayList;
import java.util.List;

import dtu.fmes.automotivesimulator.entity.enumeration.OsTaskState;



public class OsTask implements Comparable<OsTask> {

	public int id;
	public OsTaskState state;
	public double execTime;
	public double period;
	public int nextPeriod;
	public int periodCounter;	
	public double currentExecTime;
	public double priority;
	public List<Message> messages;
	public Core core;
	public List<Runnable> runnables;
	public boolean firstPeriodExecuted;
	
	public OsTask(Runnable _runnable) {}
	
	public OsTask(int _id, double _period, Core _core) {
		this.id = _id;
		this.period = _period;
		this.priority = 1/((double)_period);
		this.state = OsTaskState.WAITING;
		this.currentExecTime = 0;
		this.core = _core;
		this.runnables = new ArrayList<Runnable>();
		this.execTime = 0;
		this.firstPeriodExecuted = false;
		this.messages = new ArrayList<Message>();
		this.periodCounter = 0;
	}

	@Override
	public int compareTo(OsTask _task) {
		int stateInfluenceThis = this.state == OsTaskState.READY ? 1 : 0;
		int stateInfluenceParam = _task.state == OsTaskState.READY ? 1 : 0;
		
		return (int) (((double) ((_task.priority * stateInfluenceParam) - (this.priority * stateInfluenceThis)))*1000);
	}
	
	public Message createMessage(double _ts, int _pr, int _sz, int _dst, boolean _ext) {
		Message msg = new Message(_pr, id, _dst, _sz, false, _ts);
		this.messages.add(msg);
		return msg;
	}
	
	public void clearMessages() {
		this.messages.clear();
	}
	
	public List<Message> getMessages() {
		return this.messages;
	}
	
	private void computeExecTime() {
		this.execTime = 0;
		for (Runnable r : this.runnables) {
			this.execTime += r.computeExecTime();
		}
	}

	public double computeTaskTime() {
		computeExecTime();
		this.execTime = (2.4*this.execTime)/this.core.ecu.processorSpeed;
		return this.execTime;
	}
	
	public void addRunnable(Runnable r) {
		this.runnables.add(r);
	}
	
	public int getPeriodCounter() {
		return this.periodCounter;
	}
	
	public void incrementPeriodCounter() {
		this.periodCounter++;
	}
	
}
