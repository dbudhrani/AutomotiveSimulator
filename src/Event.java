
public class Event implements Comparable<Event>  {
	
	public int taskId;
	public EventType eventType;
	public int time;
	
	public Event(int _taskId, EventType _eventType, int _time) {
		this.taskId = _taskId; 
		this.eventType = _eventType;
		this.time = _time;
	}
	
	@Override
	public int compareTo(Event _event) {

		return _event.time - this.time;
		
	}

}
