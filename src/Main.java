
public class Main {

	static Mapping m;
	public InterECUBus bus;
	
	public static void main(String[] args) {
		
		//m = Util.parseMappingFile("io/input/config1/resultMediumSizeAutomotiveUseCase.xml");
		
		IntraECUBus bus1 = new IntraECUBus(200);
		
		ECU ecu = new ECU(1, bus1);
		
		Core core = new Core(1, ecu); 

		core.scheduler.tasks.add(new OsTask(0, 20, 69, new MessageParams(1, 23), core));
		core.scheduler.tasks.add(new OsTask(1, 10, 25, new MessageParams(1, 23), core));
		core.scheduler.tasks.add(new OsTask(2, 10, 40, new MessageParams(1, 23), core));

		core.scheduler.init();		
		core.scheduler.execute(100);	
		
	}
	
}
