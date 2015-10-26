import java.util.ArrayList;
import java.util.List;


public class ECU {

	public int id;
	public List<Component> components;
	
	public ECU(int _id) {
		this.id = _id;
		this.components = new ArrayList<Component>();
	}

	public void addComponent(Component _component) {
		this.components.add(_component);
	}
	
}
