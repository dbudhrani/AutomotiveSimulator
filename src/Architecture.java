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
		
		ECU ecu = new ECU(1, 2.4, bus1);
		
		Core core = new Core(1, ecu); 

		Runnable r1 = new Runnable(1, "r1", 2.3, 4.5);
		Runnable r2 = new Runnable(2, "r2", 2.5, 4.2);
		Runnable r3 = new Runnable(3, "r3", 1.2, 3.4);
		
		List<Runnable> tr1 = new ArrayList<Runnable>();
		tr1.add(r1);
		tr1.add(r2);
		tr1.add(r3);
		
		Runnable r4 = new Runnable(4, "r4", 1.3, 4.5);
		Runnable r5 = new Runnable(5, "r5", 3.5, 4.2);
		Runnable r6 = new Runnable(6, "r6", 2.2, 3.4);
		
		List<Runnable> tr2 = new ArrayList<Runnable>();
		tr2.add(r4);
		tr2.add(r5);
		tr2.add(r6);
		
		Runnable r7 = new Runnable(7, "r7", 1.2, 3.5);
		Runnable r8 = new Runnable(8, "r8", 4.5, 5.2);
		Runnable r9 = new Runnable(9, "r9", 3, 5.7);
		
		List<Runnable> tr3 = new ArrayList<Runnable>();
		tr3.add(r7);
		tr3.add(r8);
		tr3.add(r9);
		
	
		components.add(new SWComponent(1, "s1", tr1));
		components.add(new SWComponent(2, "s2", tr2));
		components.add(new SWComponent(3, "s3", tr3));
		
		
		List<Runnable> run1 = new ArrayList<Runnable>();
		run1.add(r1);
		run1.add(r2);
		run1.add(r7);
		
		List<Runnable> run2 = new ArrayList<Runnable>();
		run2.add(r4);
		run2.add(r5);
		run2.add(r8);
		
		List<Runnable> run3 = new ArrayList<Runnable>();
		run3.add(r3);
		run3.add(r6);
		run3.add(r9);
		
		
		core.scheduler.tasks.add(new OsTask(0, 69, new MessageParams(1, 23), core, run1));
		core.scheduler.tasks.add(new OsTask(1, 25, new MessageParams(1, 23), core, run2));
		core.scheduler.tasks.add(new OsTask(2, 40, new MessageParams(1, 23), core, run3));

		core.scheduler.init();		
		core.scheduler.execute(100);	
		
	}

	public static List<SWComponent> getSWComponents() {
		return components;
	}
	
}
