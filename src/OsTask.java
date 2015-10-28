
public class OsTask implements Comparable<OsTask> {

	public int id;
	
	public Runnable runnable;
	public OsTaskStateEnum state;
	
	public double wcet;
	public int period;
	
//	public double periodInit;
	public double periodNext;
	public double currentExecTime;
	
	public double priority;
	
	public OsTask(Runnable _runnable) {
		this.runnable = _runnable;
	}

	public OsTask(int _id, double _wcet, int _period) {
		this.id = _id;
		this.wcet = _wcet;
		this.period = _period;
		this.priority = 1/_period;
		this.state = OsTaskStateEnum.WAITING;
//		this.periodInit = 0;
		this.periodNext = period;
		this.currentExecTime = 0;
	}

	@Override
	public int compareTo(OsTask _task) {

		int stateInfluenceThis = this.state == OsTaskStateEnum.READY ? 1 : 0;
		int stateInfluenceParam = _task.state == OsTaskStateEnum.READY ? 1 : 0;
		
		// order by period to avoid the double issue
		return (int) (((double) ((_task.priority * stateInfluenceParam) - (this.priority * stateInfluenceThis)))*1000);
	}
	
}
