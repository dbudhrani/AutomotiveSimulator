
public class Log implements Comparable<Log>  {
	
	public int taskId;
	public LogType logType;
	public double time;
	public LogSeverity logSeverity;
	
	public Log(int _taskId, double _time, LogType _logType, LogSeverity _logSeverity) {
		this.taskId = _taskId;
		this.logType = _logType;
		this.time = _time;
		this.logSeverity = _logSeverity;
	}

	@Override
	public int compareTo(Log _log) {
		return ((int)this.time*10000 - (int)_log.time*10000);
	}

	
}
