import java.util.ArrayList;
import java.util.List;


public class Architecture {

	static Mapping m;
	public InterECUBus bus;
	private static List<SWComponent> components;
	
	public static void main(String[] args) {
	
		components = new ArrayList<SWComponent>();
		
		//m = Util.parseMappingFile("io/input/config1/resultMediumSizeAutomotiveUseCase.xml");
		
		IntraECUBus bus1 = new IntraECUBus(200);
		
		ECU ecu = new ECU(1, bus1);
		
		Core core = new Core(1, ecu); 

		core.scheduler.tasks.add(new OsTask(0, 69, new MessageParams(1, 23), core, new ArrayList<Runnable>()));
		core.scheduler.tasks.add(new OsTask(1, 25, new MessageParams(1, 23), core, new ArrayList<Runnable>()));
		core.scheduler.tasks.add(new OsTask(2, 40, new MessageParams(1, 23), core, new ArrayList<Runnable>()));

		core.scheduler.init();		
		core.scheduler.execute(100);	
		
	}

	public static List<SWComponent> getSWComponents() {
		return components;
	}
	
}
