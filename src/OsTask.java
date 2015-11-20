
public class OsTask implements Comparable<OsTask> {

	public int id;
	
	public Runnable runnable;
	public OsTaskState state;
	
	public int wcet;
	public int period;
	
	public int nextPeriod;
	
//	public double periodInit;
	public int currentExecTime;
	
	public double priority;
	
	public Message message;
	public MessageParams msgParams;
	public Core core;
	
	public OsTask(Runnable _runnable) {
		this.runnable = _runnable;
	}

	public OsTask(int _id, int _wcet, int _period, MessageParams _msgParams, Core _core) {
		this.id = _id;
		this.wcet = _wcet;
		this.period = _period;
		this.priority = 1/((double)_period);
		this.state = OsTaskState.WAITING;
//		this.periodInit = 0;
		this.currentExecTime = 0;
		this.msgParams = _msgParams;
		this.core = _core;
	}

	@Override
	public int compareTo(OsTask _task) {

		int stateInfluenceThis = this.state == OsTaskState.READY ? 1 : 0;
		int stateInfluenceParam = _task.state == OsTaskState.READY ? 1 : 0;
		
		// order by period to avoid the double issue
		return (int) (((double) ((_task.priority * stateInfluenceParam) - (this.priority * stateInfluenceThis)))*1000);
	}
	
	public void createMessage() {
		this.message = new Message(id, msgParams.dst, msgParams.size, false);
		
	}
	
	public Message getMessage() {
		return this.message;
	}
	
}
