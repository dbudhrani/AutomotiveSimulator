
public class Message {

	public int src;
	public int dst;
	public int size;
	public boolean extendedIdentifier;
	
	public Message(int _src, int _dst, int _size, boolean _extendedIdentifier) {
		this.src = _src;
		this.dst = _dst;
		this.size = _size;
		this.extendedIdentifier = _extendedIdentifier;
	}

}
