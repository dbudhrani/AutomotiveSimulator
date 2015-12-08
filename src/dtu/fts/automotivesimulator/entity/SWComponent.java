package dtu.fts.automotivesimulator.entity;
import java.util.ArrayList;
import java.util.List;


public class SWComponent {

	public int id;
	public String name;
	public double e2eDelay;
	public List<Runnable> runnables;

	public SWComponent(String _name) {
		this.name = _name;
	}
	
	public SWComponent(int _id, String _name) {
		this.id = _id;
		this.name = _name;
		this.e2eDelay = 0;
		this.runnables = new ArrayList<Runnable>();
	}
	
	public void addDelay(double _delay) {
		this.e2eDelay += _delay;
	}
	
	public void addRunnable(Runnable r) {
		this.runnables.add(r);
	}
	
}
