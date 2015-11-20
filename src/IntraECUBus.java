import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class IntraECUBus {

	public List<Core> cores;
	public int bandwith;
	public int delay;
	public boolean isBusy;
	
	public IntraECUBus(int _bandwith) {
		this.bandwith = _bandwith;
		this.isBusy = false;
	}
	
	public double computeDelay(Message _msg) {
		// connect this delay with the delay of the SW component
		return ((_msg.extendedIdentifier ? 80 : 55) + 10*_msg.size)/bandwith;
	}

	public void broadcastMessage(Message _msg) {
		isBusy = true;
		for (Core c : cores) {
			if (!c.scheduler.isTaskSameCore(_msg.dst)) {
				c.inputMessages.add(_msg);
				c.checkInputMessages(_msg);	
			}
		}
		isBusy = false;
		selectMessage();
	}
	
	public void selectMessage() {
		if (!isBusy) {
			List<Message> msgs = new ArrayList<Message>();
			for (Core c : cores) {
				if (c.outputMessages.size() > 0) {
					msgs.add(c.outputMessages.get(0));				
				}
			}
			if (msgs.size() > 0) {
				Collections.sort(msgs);
				broadcastMessage(msgs.get(0));	
			}
		}
	}
	
}
