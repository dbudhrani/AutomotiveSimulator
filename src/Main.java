
public class Main {

	static Mapping m;
	
	public static void main(String[] args) {
		
		//m = Util.parseMappingFile("io/input/config1/resultMediumSizeAutomotiveUseCase.xml");
		
		Core core = new Core(1);

		core.scheduler.tasks.add(new OsTask(20, 100));
		core.scheduler.tasks.add(new OsTask(10, 25));
		core.scheduler.tasks.add(new OsTask(10, 40));
		
		core.scheduler.start();
		
		
		
		
	}
	
}
