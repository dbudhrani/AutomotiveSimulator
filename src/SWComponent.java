import java.util.List;


public class SWComponent {

	public int id;
	public String name;
	public double e2eDelay;
	public List<Runnable> runnables;
	
	public SWComponent(String _name) {
		this.name = _name;
	}
	
	public SWComponent(int _id, String _name, List<Runnable> _runnables) {
		this.id = _id;
		this.name = _name;
		this.e2eDelay = 0;
		this.runnables = _runnables;
	}
	
	public void addDelay(double _delay) {
		this.e2eDelay += _delay;
	}

}
