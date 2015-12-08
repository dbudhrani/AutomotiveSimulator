package dtu.fts.automotivesimulator;
import dtu.fts.automotivesimulator.entity.Architecture;
import dtu.fts.automotivesimulator.util.Util;


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
