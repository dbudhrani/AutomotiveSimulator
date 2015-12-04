
public class Main {

	public static String ARCHITECTURE_PATH = "io/input/architecture4.xml";
	
	public Main() {}

	public static void main(String[] args) {

    	Architecture a = Util.parseInputFile(ARCHITECTURE_PATH);
		a.execute(1000);
		
		Util.printLog(a, ARCHITECTURE_PATH);
		
	}

}
