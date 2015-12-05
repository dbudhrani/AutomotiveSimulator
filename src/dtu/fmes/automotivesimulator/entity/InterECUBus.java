package dtu.fmes.automotivesimulator.entity;
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
	}

	public void computeDelay(Message _msg) {
		this.delay = ((_msg.extendedIdentifier ? 80 : 55) + 10*_msg.size)/(double)bandwith;
	}

	public void broadcastMessage(Message _msg) {
		selectMessage();
		computeDelay(_msg);
		_msg.updateTimestamp(this.delay);
		for (ECU e : ecus) {
			if (!e.isTaskSameECU(_msg.src)) {
				e.inputMessages.add(_msg);	
				e.checkInputMessage(_msg);
			}
		}
	}
	
	public void selectMessage() {
		if (!isBusy) {
			isBusy = true;
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
			isBusy = false;
		}
	}
	
	public void setECUs(List<ECU> _ecus) {
		this.ecus = _ecus;
	}
	
}
