import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class InterECUBus {

	public List<ECU> ecus;
	public int bandwith;
	public double delay;
	public boolean isBusy;
	
	public InterECUBus(int _bandwith) {
		this.bandwith = _bandwith;
		this.isBusy = false;
//		this.ecus = new ArrayList<ECU>();
	}

	public void computeDelay(Message _msg) {
		// connect this delay with the delay of the SW component
		this.delay = ((_msg.extendedIdentifier ? 80 : 55) + 10*_msg.size)/(double)bandwith;
	}

	public void broadcastMessage(Message _msg) {
		isBusy = true;
		selectMessage();
		computeDelay(_msg);
		_msg.updateTimestamp(this.delay);
		for (ECU e : ecus) {
			if (!e.isTaskSameECU(_msg.src)) {
				e.inputMessages.add(_msg);	
				e.checkInputMessage(_msg);
			}
		}
		isBusy = false;
	}
	
	public void selectMessage() {
		if (!isBusy) {
			List<Message> msgs = new ArrayList<Message>();
			for (ECU e : ecus) {
				if (e.outputMessages.size() > 0) {
					msgs.add(e.outputMessages.get(0));				
				}
			}
			if (msgs.size() > 0) {
				Collections.sort(msgs);
				broadcastMessage(msgs.get(0));	
			}
		}
	}

//	public void addECU(ECU e) {
//		this.ecus.add(e);
//	}
	
	public void setECUs(List<ECU> _ecus) {
		this.ecus = _ecus;
	}
	
}
