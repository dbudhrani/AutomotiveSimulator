import java.util.Random;


public class Runnable {

	public int id;
	public String name;
	
	public int bcet;
	public int wcet;
	public double execTime;
	
	public Runnable(String _name) {
		this.name = _name;
	}
	
	public Runnable(int _id, String _name, int _bcet, int _wcet) {
		this.id = _id;
		this.name = _name;
		this.bcet = _bcet;
		this.wcet = _wcet;
	}
	
	public double computeExecTime() {
		Random rand = new Random();
		this.execTime = bcet + (wcet - bcet) * rand.nextDouble();
		return execTime;
	}
	
	public double getExecTime() {
		return execTime;
	}

}
