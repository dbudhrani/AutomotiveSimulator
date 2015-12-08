package dtu.fts.automotivesimulator.entity;
import java.util.ArrayList;
import java.util.List;


public class Mapping {

	public List<ECU> ECUs;
	public List<Core> Cores;
	public List<OsApplication> OsApplications;
	
	public Mapping() {
		this.ECUs = new ArrayList<ECU>();
		this.Cores = new ArrayList<Core>();
		this.OsApplications = new ArrayList<OsApplication>();
	}

	public void addECU(ECU _ecu) {
		this.ECUs.add(_ecu);
	}
	
	public void addCore(Core _core) {
		this.Cores.add(_core);
	}
	
	public void addOsApplication(OsApplication _osApplication) {
		this.OsApplications.add(_osApplication);
	}
}
