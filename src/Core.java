import java.util.ArrayList;
import java.util.List;


public class Core {

	public int id;
	//public List<Runnable> runnables;

	public List<Message> inputMessages;
	public List<Message> outputMessages;
	
	//public List<OsTask> tasks;
	public Scheduler2 scheduler;
	
	public OsTask currentTask;
	public boolean isBusy;
	public ECU ecu;
	
	public Core(int _id) {
		this.id = _id;
	}
	
	public Core(int _id, ECU _ecu) {
		this.id = _id;
	//	this.runnables = new ArrayList<Runnable>();
		//this.tasks = new ArrayList<OsTask>();
		this.scheduler = new Scheduler2(this);
		this.inputMessages = new ArrayList<Message>();
		this.outputMessages = new ArrayList<Message>();
		this.ecu = _ecu;
	}
	
	public void addRunnable(Runnable _runnable) {
		//this.runnables.add(_runnable);
	}
	
	public void addMessageToOutputQueue(Message _msg) {
		outputMessages.add(_msg);
		if (ecu.isTaskSameECU(_msg.dst)) {
			ecu.bus.broadcastMessage(_msg);
		} else {
//			ecu.addMessageToOutputQueue(_msg);
			
		}
		outputMessages.remove(_msg);
	}
	
	public void checkInputMessages(Message _msg) {
		for (OsTask t : scheduler.tasks) {
			if (t.id == _msg.dst) {
				scheduler.coreReceivedMessage(_msg);
				break;
			}
		}
		inputMessages.remove(_msg);
	}
	
}
