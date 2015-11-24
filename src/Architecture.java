import java.util.ArrayList;
import java.util.List;


public class Architecture {

	static Mapping m;
	public static InterECUBus bus;
	private static List<SWComponent> components;
	private static final Architecture singleton = new Architecture();
	private static List<ECU> ecus;
	
	public Architecture() {
		
	}
	
	public static Architecture getInstance() {
		return singleton;
	}
	
	public static void main(String[] args) {
	
		components = new ArrayList<SWComponent>();
		ecus = new ArrayList<ECU>();
		
		//m = Util.parseMappingFile("io/input/config1/resultMediumSizeAutomotiveUseCase.xml");
	
		IntraECUBus bus1 = new IntraECUBus(200);
		bus = new InterECUBus(400);
		
		
		ECU ecu = new ECU(1, 2.4, bus1, Architecture.getInstance());
		
		IntraECUBus bus2 = new IntraECUBus(200);
		ECU ecu2 = new ECU(2, 2.4, bus2, Architecture.getInstance());
		
		bus.ecus.add(ecu);
		bus.ecus.add(ecu2);
		
		
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
		
		Runnable r10 = new Runnable(10, "r10", 3, 5.7);
		Runnable r11 = new Runnable(11, "r11", 3, 5.7);
		Runnable r12 = new Runnable(12, "r12", 3, 5.7);
		Runnable r13 = new Runnable(13, "r13", 3, 5.7);
		Runnable r14 = new Runnable(14, "r14", 3, 5.7);
		
		
		List<Runnable> tr3 = new ArrayList<Runnable>();
		tr3.add(r7);
		tr3.add(r8);
		tr3.add(r9);
		

		List<Runnable> tr4 = new ArrayList<Runnable>();
		tr4.add(r10);
		tr4.add(r11);
		tr4.add(r12);
		tr4.add(r13);
		tr4.add(r14);
		
		components.add(new SWComponent(1, "s1", tr1));
		components.add(new SWComponent(2, "s2", tr2));
		components.add(new SWComponent(3, "s3", tr3));
		components.add(new SWComponent(3, "s3", tr4));
		
		
		List<Runnable> run1 = new ArrayList<Runnable>();
		run1.add(r1);
		run1.add(r2);
		run1.add(r7);
		
		List<Runnable> run2 = new ArrayList<Runnable>();
		run2.add(r3);
		run2.add(r10);
		run2.add(r8);
		
		List<Runnable> run3 = new ArrayList<Runnable>();
		run3.add(r14);
		run3.add(r11);
		run3.add(r4);
		
		List<Runnable> run4 = new ArrayList<Runnable>();
		run4.add(r9);
		run4.add(r12);
		
		List<Runnable> run5 = new ArrayList<Runnable>();
		run5.add(r5);
		run5.add(r6);
		
		List<Runnable> run6 = new ArrayList<Runnable>();
		run6.add(r13);
		
		core.scheduler.tasks.add(new OsTask(0, 69, new MessageParams(1, 23), core, run1));
		core.scheduler.tasks.add(new OsTask(1, 25, new MessageParams(1, 23), core, run2));
		core.scheduler.tasks.add(new OsTask(2, 40, new MessageParams(1, 23), core, run3));

		core.scheduler.init();		
		
		
		Core core2 = new Core(2, ecu2); 
		core2.scheduler.tasks.add(new OsTask(3, 69, new MessageParams(1, 23), core2, run4));
		core2.scheduler.tasks.add(new OsTask(4, 25, new MessageParams(1, 23), core2, run5));
		core2.scheduler.tasks.add(new OsTask(5, 40, new MessageParams(1, 23), core2, run6));

		core2.scheduler.init();		
		
		bus1.cores.add(core);
		bus2.cores.add(core2);
		
		ecu.cores.add(core);
		ecu2.cores.add(core2);
		
		ecus.add(ecu);
		ecus.add(ecu2);
		
		core.scheduler.execute(100);	
		core2.scheduler.execute(100);	

		Util.printLog();
		
	}

	public static List<SWComponent> getSWComponents() {
		return components;
	}

	public static List<ECU> getECUs() {
		return ecus;
	}
	
}
