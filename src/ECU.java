import java.util.ArrayList;
import java.util.List;


public class ECU {

	public int id;
	public List<SWComponent> components;
	
	public List<Message> inputMessages;
	public List<Message> outputMessages;
	
	public List<Core> cores;
	
	public IntraECUBus bus;

	public Architecture arc;
	
	public double processorSpeed;
	
	public ECU(int _id) {
		this.id = _id;
	}
	
	public ECU(int _id, double _processorSpeed, Architecture _arc) {
		this.id = _id;
		this.components = new ArrayList<SWComponent>();
		this.inputMessages = new ArrayList<Message>();
		this.outputMessages = new ArrayList<Message>();
		this.cores = new ArrayList<Core>();
		this.processorSpeed = _processorSpeed;
		this.bus = null;
		this.arc = _arc;
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
	
	public void addMessageToOutputQueue(Message _msg) {
		outputMessages.add(_msg);
		arc.bus.broadcastMessage(_msg);
	}
	
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
	
	public void setBus(IntraECUBus _bus) {
		this.bus = _bus;
	}

	public void addCore(Core c) {
		this.cores.add(c);
	}
	
}
