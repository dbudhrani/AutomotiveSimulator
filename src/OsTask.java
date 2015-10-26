
public class OsTask implements Comparable<OsTask> {

	public Runnable runnable;
	public OsTaskStateEnum state;
	
	public double wcet;
	public double period;
	
	public double priority;
	
	public OsTask(Runnable _runnable) {
		this.runnable = _runnable;
	}

	public OsTask(double _wcet, double _period) {
		this.wcet = _wcet;
		this.period = _period;
		this.priority = 1/_period;
		this.state = OsTaskStateEnum.WAITING;
	}

	@Override
	public int compareTo(OsTask _task) {

		int stateInfluenceThis = this.state == OsTaskStateEnum.READY ? 1 : 0;
		int stateInfluenceParam = _task.state == OsTaskStateEnum.READY ? 1 : 0;
		
		// order by period to avoid the double issue
		return (int) (((double) ((_task.priority * stateInfluenceParam) - (this.priority * stateInfluenceThis)))*1000);
	}
	
}
