
public class Main {

	static Mapping m;
	
	public static void main(String[] args) {
		
		//m = Util.parseMappingFile("io/input/config1/resultMediumSizeAutomotiveUseCase.xml");
		
		Core core = new Core(1); 

		core.scheduler.tasks.add(new OsTask(0, 20, 69));
		core.scheduler.tasks.add(new OsTask(1, 10, 25));
		core.scheduler.tasks.add(new OsTask(2, 10, 40));

		core.scheduler.init();		
		core.scheduler.execute(100);	
		
	}
	
}
