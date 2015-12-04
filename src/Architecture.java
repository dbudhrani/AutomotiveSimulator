import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;


public class Architecture {

	public InterECUBus bus;
	private List<SWComponent> components;
	private List<ECU> ecus;
	
	public Hashtable<Integer, Hashtable<Integer, List<Double>>> startingTimes;
	public Hashtable<Integer, Hashtable<Integer, List<Double>>> finishingTimes;
	
	public Architecture() {
		components = new ArrayList<SWComponent>();
		ecus = new ArrayList<ECU>();
		this.startingTimes = new Hashtable<Integer, Hashtable<Integer, List<Double>>>();
		this.finishingTimes = new Hashtable<Integer, Hashtable<Integer, List<Double>>>();
	}
	
	public void execute(final int _maxTime) {
	
		initStartFinishTimes();
		
		for (ECU e : this.ecus) {
			for (Core c : e.cores) {
				c.scheduler.init();				
				c.scheduler.execute(_maxTime);
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

	public void initStartFinishTimes() {
		for (SWComponent c : this.components) {
			Hashtable<Integer, List<Double>> ht = new Hashtable<Integer, List<Double>>();
			Hashtable<Integer, List<Double>> ht2 = new Hashtable<Integer, List<Double>>();
			startingTimes.put(c.id, ht);
			finishingTimes.put(c.id, ht2);
		}
	}
	
	public void addStartTime(SWComponent _c, int _pc, double _t) {
		if (this.startingTimes.get(_c.id).get(_pc) == null) {
			this.startingTimes.get(_c.id).put(_pc, new ArrayList<Double>());
		}
		this.startingTimes.get(_c.id).get(_pc).add(_t);
	}
	
	public void addFinishTime(SWComponent _c, int _pc, double _t) {
		if (this.finishingTimes.get(_c.id).get(_pc) == null) {
			this.finishingTimes.get(_c.id).put(_pc, new ArrayList<Double>());
		}
		this.finishingTimes.get(_c.id).get(_pc).add(_t);
	}
	
}
