
public class Message implements Comparable<Message> {

	public int priority;
	public int src;
	public int dst;
	public int size;
	public boolean extendedIdentifier;
	
	public Message(int _priority, int _src, int _dst, int _size, boolean _extendedIdentifier) {
		this.priority = _priority;
		this.src = _src;
		this.dst = _dst;
		this.size = _size;
		this.extendedIdentifier = _extendedIdentifier;
	}

	@Override
	public int compareTo(Message _msg) {
		return _msg.priority - this.priority;
	}

}
