
public class Main {

	public static boolean USE_TEMPORAL_PARTITIONING = false;
	
	public Main() {

	}

	public static void main(String[] args) {
		
		Architecture a = Util.parseInputFile("io/input/architecture1.xml");
		a.execute(1000);
		
		
		Util.printLog(a);
		
	}

}
