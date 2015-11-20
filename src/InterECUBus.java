import java.util.List;


public class InterECUBus {

	public List<ECU> ecus;
	public int bandwith;
	public int delay;
	
	public InterECUBus(int _bandwith) {
		this.bandwith = _bandwith;
	}

	public double computeDelay(Message _msg) {
		// connect this delay with the delay of the SW component
		return ((_msg.extendedIdentifier ? 80 : 55) + 10*_msg.size)/bandwith;
	}

	public void broadcastMessage(Message _msg) {
		for (ECU e : ecus) {
			e.inputMessages.add(_msg);
			
		}
	}
	
}
