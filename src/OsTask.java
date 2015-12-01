import java.util.ArrayList;
import java.util.List;


public class OsTask implements Comparable<OsTask> {

	public int id;
	
	public OsTaskState state;
	
	public double execTime;
	public int period;
	
	public int nextPeriod;
	
//	public double periodInit;
	public double currentExecTime;
	
	public double priority;
	
	public Message message;
	public MessageParams msgParams;
	public Core core;
	public List<Runnable> runnables;
	
	public boolean firstPeriodExecuted;
	
	public OsTask(Runnable _runnable) {
		
	}
	
	public OsTask(int _id, int _period, MessageParams _msgParams, Core _core) {
		this.id = _id;
		//this.wcet = _wcet;
		this.period = _period;
		this.priority = 1/((double)_period);
		this.state = OsTaskState.WAITING;
//		this.periodInit = 0;
		this.currentExecTime = 0;
		this.msgParams = _msgParams;
		this.core = _core;
		this.runnables = new ArrayList<Runnable>();
		this.execTime = 0;
		this.firstPeriodExecuted = false;
	}

	@Override
	public int compareTo(OsTask _task) {

		int stateInfluenceThis = this.state == OsTaskState.READY ? 1 : 0;
		int stateInfluenceParam = _task.state == OsTaskState.READY ? 1 : 0;
		
		// order by period to avoid the double issue
		return (int) (((double) ((_task.priority * stateInfluenceParam) - (this.priority * stateInfluenceThis)))*1000);
	}
	
	public void createMessage(double _ts) {
		this.message = new Message(9, id, 1, 23, false, _ts);
		// TODO check message priority
	}
	
	public Message getMessage() {
		return this.message;
	}
	
	private void computeExecTime(List<Runnable> _runnables) {
		this.execTime = 0;
		for (Runnable r : _runnables) {
			this.execTime += r.computeExecTime();
		}
	}

	public double computeTaskTime(List<Runnable> r) {
		computeExecTime(r);
		this.execTime = (2.4*this.execTime)/this.core.ecu.processorSpeed;
		return this.execTime;
	}
	
	public void addRunnable(Runnable r) {
		this.runnables.add(r);
	}
	
}
