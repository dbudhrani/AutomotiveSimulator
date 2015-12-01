
public class Event implements Comparable<Event>  {
	
	public int taskId;
	public EventType eventType;
	public double time;
	
	public Event(int _taskId, double _time, EventType _eventType) {
		this.taskId = _taskId; 
		this.eventType = _eventType;
		this.time = _time;
	}
	
	@Override
	public int compareTo(Event _event) {

		return ((int)this.time*10000) - ((int)_event.time*10000);
		
	}

}
