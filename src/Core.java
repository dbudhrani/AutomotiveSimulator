import java.util.ArrayList;
import java.util.List;


public class Core {

	public int id;
	public List<Message> inputMessages;
	public List<Message> outputMessages;
	public Scheduler scheduler;
	public OsTask currentTask;
	public boolean isBusy;
	public ECU ecu;
	
	public Core(int _id) {
		this.id = _id;
	}
	
	public Core(int _id, ECU _ecu) {
		this.id = _id;
		this.scheduler = new Scheduler(this);
		this.inputMessages = new ArrayList<Message>();
		this.outputMessages = new ArrayList<Message>();
		this.ecu = _ecu;
	}
	
	public void addMessageToOutputQueue(Message _msg) {
		scheduler.logs.add(new Log(_msg.src, scheduler.timer, LogType.MESSAGE_SENT, "Message destination: " + (_msg.dst == -1 ? "ACTUATOR" : "task " + _msg.dst), LogSeverity.NORMAL));
		outputMessages.add(_msg);
		if (ecu.isTaskSameECU(_msg.dst)) {
			ecu.bus.broadcastMessage(_msg);
		} else {
			ecu.bus.computeDelay(_msg);
			_msg.updateTimestamp(ecu.bus.delay);
			ecu.addMessageToOutputQueue(_msg);
		}
		outputMessages.remove(_msg);
	}
	
	public void checkInputMessages(Message _msg) {
		boolean isSameCore = false;
		for (OsTask t : scheduler.tasks) {
			if (t.id == _msg.dst) {
				this.inputMessages.add(_msg);
				scheduler.logs.add(new Log(_msg.dst, _msg.updTs, LogType.MESSAGE_RECEIVED, "Message source: task " + _msg.src, LogSeverity.NORMAL));			
				isSameCore = true;
				break;
			}
		}
		if (!isSameCore) {
			inputMessages.remove(_msg);	
		}
	}
	
}
