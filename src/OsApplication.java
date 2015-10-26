import java.util.ArrayList;
import java.util.List;


public class OsApplication {

	public String ASIL_LEVEL;
	public List<OsTask> osTasks;
	
	public OsApplication(String _asilLevel) {
		this.ASIL_LEVEL = _asilLevel;
		this.osTasks = new ArrayList<OsTask>();
	}
	
	public void addOsTask(OsTask _osTask) {
		this.osTasks.add(_osTask);
	}

}
