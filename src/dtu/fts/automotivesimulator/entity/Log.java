package dtu.fts.automotivesimulator.entity;
import dtu.fts.automotivesimulator.entity.enumeration.LogSeverity;
import dtu.fts.automotivesimulator.entity.enumeration.LogType;


public class Log implements Comparable<Log>  {
	
	public int taskId;
	public LogType logType;
	public String message;
	public double time;
	public LogSeverity logSeverity;
	
	public Log(int _taskId, double _time, LogType _logType, String _message, LogSeverity _logSeverity) {
		this.taskId = _taskId;
		this.logType = _logType;
		this.time = _time;
		this.logSeverity = _logSeverity;
		this.message = _message;
	}

	@Override
	public int compareTo(Log _log) {
		return ((int)this.time*10000 - (int)_log.time*10000);
	}

	
}
