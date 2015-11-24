
public class Message implements Comparable<Message> {

	public int priority;
	public int src;
	public int dst;
	public int size;
	public boolean extendedIdentifier;
	public double ts;
	public double updTs;
	
	public Message(int _priority, int _src, int _dst, int _size, boolean _extendedIdentifier, double _ts) {
		this.priority = _priority;
		this.src = _src;
		this.dst = _dst;
		this.size = _size;
		this.extendedIdentifier = _extendedIdentifier;
		this.ts = _ts;
		this.updTs = _ts;
	}

	@Override
	public int compareTo(Message _msg) {
		return _msg.priority - this.priority;
	}

	public void updateTimestamp(double _delay) {
		this.updTs += _delay;
	}

	public double getMessageAge() {
		return this.updTs - this.ts;
	}
	
}
