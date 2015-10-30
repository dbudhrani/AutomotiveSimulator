
public class Log {
	
	public int taskId;
	public LogType logType;
	public int time;
	public LogSeverity logSeverity;
	
	public Log(int _taskId, int _time, LogType _logType, LogSeverity _logSeverity) {
		this.taskId = _taskId;
		this.logType = _logType;
		this.time = _time;
	}

}
