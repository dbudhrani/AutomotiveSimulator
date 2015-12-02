import java.util.ArrayList;
import java.util.List;


public class Architecture {

	static Mapping m;
	public static InterECUBus bus;
	private List<SWComponent> components;
	private List<ECU> ecus;
	
	public Architecture() {
		components = new ArrayList<SWComponent>();
		ecus = new ArrayList<ECU>();
	}
	
	public void execute(int _maxTime) {
	
		for (ECU e : this.ecus) {
			for (Core c : e.cores) {
				c.scheduler.init();
				c.scheduler.execute2(_maxTime);
			}
		}
		
	}

	public List<SWComponent> getSWComponents() {
		return this.components;
	}

	public List<ECU> getECUs() {
		return this.ecus;
	}
	
	public void addECU(ECU e) {
		this.ecus.add(e);
	}
	
	public void addSWComponent(SWComponent c) {
		this.components.add(c);
	}
	
	public void setInterECUBus(InterECUBus _bus) {
		this.bus = _bus;
	}

}
