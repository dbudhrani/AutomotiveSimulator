import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class IntraECUBus {

	public List<Core> cores;
	public int bandwith;
	public double delay;
	public boolean isBusy;
	
	public IntraECUBus(int _bandwith) {
		this.bandwith = _bandwith;
		this.isBusy = false;
		this.cores = new ArrayList<Core>();
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
		for (Core c : cores) {
			if (!c.scheduler.isTaskSameCore(_msg.dst)) {
				c.scheduler.logs.add(new Log(-1, c.scheduler.timer, LogType.MESSAGE_SENT, LogSeverity.NORMAL));
				c.inputMessages.add(_msg);
				c.scheduler.logs.add(new Log(-1, c.scheduler.timer + this.delay, LogType.MESSAGE_RECEIVED, LogSeverity.NORMAL));
				c.checkInputMessages(_msg);	
			}
		}
		isBusy = false;
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
