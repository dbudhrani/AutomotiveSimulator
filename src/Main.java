
public class Main {

	public Main() {

	}

	public static void main(String[] args) {

    	Architecture a = Util.parseInputFile("io/input/architecture1_old.xml");
		a.execute(1000);
		
		Util.printLog(a);
		
	}

}
