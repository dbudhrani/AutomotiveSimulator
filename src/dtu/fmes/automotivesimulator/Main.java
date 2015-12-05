package dtu.fmes.automotivesimulator;
import dtu.fmes.automotivesimulator.entity.Architecture;
import dtu.fmes.automotivesimulator.util.Util;


public class Main {
	
	public Main() {}

	public static void main(String[] args) {

		String filePath = "io/input/" + args[0];
		Integer maxTime = Integer.valueOf(args[1]);
		
    	Architecture a = Util.parseInputFile(filePath);
		a.execute(maxTime);
		
		Util.printLog(a, filePath);
		
	}

}
