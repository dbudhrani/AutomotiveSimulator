import java.util.ArrayList;
import java.util.List;


public class ECU {

	public int id;
	public List<SWComponent> components;
	
	public List<Message> inputMessages;
	public List<Message> outputMessages;
	
	public List<Core> cores;
	
	public IntraECUBus bus;

	public ECU(int _id) {
		this.id = _id;
	}
	
	public ECU(int _id, IntraECUBus _bus) {
		this.id = _id;
		this.components = new ArrayList<SWComponent>();
		this.inputMessages = new ArrayList<Message>();
		this.outputMessages = new ArrayList<Message>();
		this.cores = new ArrayList<Core>();
		this.bus = _bus;
	}

	public void addComponent(SWComponent _component) {
		this.components.add(_component);
	}
	
	public boolean isTaskSameECU(int _id) {
		for (Core c : cores) {
			for (OsTask t : c.scheduler.tasks) {
				if (t.id == _id) {
					return true;
				}
			}
		}
		return false;
	}
	
//	public void addMessageToOutputQueue(Message _msg) {
//		outputMessages.add(_msg);
//		bus.broadcastMessage(_msg);
//		outputMessages.add(_msg);
//	}
	
	public void checkInputMessage(Message _msg) {
		for (Core c : cores) {
			for (OsTask t : c.scheduler.tasks) {
				if (t.id == _msg.dst) {
					ecuReceivedMessage(_msg);
					break;
				}
			}
		}
	}
	
	public void ecuReceivedMessage(Message _msg) {
		bus.broadcastMessage(_msg);
		inputMessages.remove(_msg);
	}
	
}
