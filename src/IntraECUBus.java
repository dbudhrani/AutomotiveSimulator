import java.util.List;


public class IntraECUBus {

	public List<Core> cores;
	public int bandwith;
	public int delay;
	
	public IntraECUBus(int _bandwith) {
		this.bandwith = _bandwith;
	}
	
	public double computeDelay(Message _msg) {
		// connect this delay with the delay of the SW component
		return ((_msg.extendedIdentifier ? 80 : 55) + 10*_msg.size)/bandwith;
	}

	public void broadcastMessage(Message _msg) {
		for (Core c : cores) {
			c.inputMessages.add(_msg);
			c.checkInputMessages(_msg);
		}
	}
	
}
