import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Random;


public class Runnable {

	public int id;
	public String name;
	
	public double bcet;
	public double wcet;
	public double execTime;
	public double messageSize;
	
	public Runnable(String _name) {
		this.name = _name;
	}
	
	public Runnable(int _id, String _name, double _bcet, double _wcet, int _messageSize) {
		this.id = _id;
		this.name = _name;
		this.bcet = _bcet;
		this.wcet = _wcet;
		this.messageSize = _messageSize;
	}
	
	public double computeExecTime() {
		Random rand = new Random();
		// TODO round 3 decimals
		this.execTime = bcet + (wcet - bcet) * rand.nextDouble();
//		DecimalFormat df = new DecimalFormat("#.###");
//		df.setRoundingMode(RoundingMode.CEILING);
//		this.execTime  = Double.valueOf(df.format(this.execTime));
		this.execTime = Math.round(this.execTime*1000)/1000d;
		return execTime;
	}
	
	public double getExecTime() {
		return execTime;
	}

}
