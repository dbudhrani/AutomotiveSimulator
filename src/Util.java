import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class Util {

	public Util() {

	}

	public static Mapping parseMappingFile(String _path) {
		Mapping m = new Mapping();
		
		try {
			
			File inputFile = new File(_path);
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(inputFile);
			
			doc.getDocumentElement().normalize();
			System.out.println("Root element: " + doc.getDocumentElement().getNodeName());
			
			NodeList mappingNodeList = doc.getElementsByTagName("Mapping");
			Node mappingNode = mappingNodeList.item(0);
			
			NodeList mappingChildren = mappingNode.getChildNodes();
			Node ecusNode = null;
			Node coresNode = null;
			Node osApplicationsNode = null;
			for (int i=0; i < mappingChildren.getLength(); i++) {
				if (mappingChildren.item(i).getNodeName().equals("Ecus")) {
					ecusNode = mappingChildren.item(i);
				} else if (mappingChildren.item(i).getNodeName().equals("Cores")) {
					coresNode = mappingChildren.item(i);
				} else if (mappingChildren.item(i).getNodeName().equals("Os-Applications")) {
					osApplicationsNode = mappingChildren.item(i);
				}
			}
			
			if (ecusNode != null) {
				NodeList ecusChildren = ecusNode.getChildNodes();
				for (int i=0; i<ecusChildren.getLength(); i++) {
					Node ecuNode = ecusChildren.item(i);
					if (ecuNode.getNodeType() == 1) {
						ECU ecu = new ECU(Integer.parseInt(ecuNode.getNodeName().split("ECU")[1]));
						NodeList ecuChildren = ecuNode.getChildNodes();
						for (int j=0; j<ecuChildren.getLength(); j++) {
							Node componentNode = ecuChildren.item(j);
							if (componentNode.getNodeType() == 1) {
								SWComponent component = new SWComponent(componentNode.getTextContent());
								ecu.addComponent(component);	
							}
						}
						m.ECUs.add(ecu);	
					}
				}
			}
			
			if (coresNode != null) {
				NodeList coresChildren = coresNode.getChildNodes();
				for (int i=0; i<coresChildren.getLength(); i++) {
					Node coreNode = coresChildren.item(i);
					if (coreNode.getNodeType() == 1) {
						Core core = new Core(Integer.parseInt(coreNode.getNodeName().split("Core")[1]));
						NodeList coreChildren = coreNode.getChildNodes();
						for (int j=0; j<coreChildren.getLength(); j++) {
							Node runnableNode = coreChildren.item(j);
							if (runnableNode.getNodeType() == 1) {
								Runnable runnable = new Runnable(runnableNode.getTextContent());
								core.addRunnable(runnable);	
							}
						}
						m.Cores.add(core);	
					}
				}
			}
			
			if (osApplicationsNode != null) {
				NodeList osApplicationsChildren = osApplicationsNode.getChildNodes();
				for (int i=0; i<osApplicationsChildren.getLength(); i++) {
					Node osApplicationNode = osApplicationsChildren.item(i);
					if (osApplicationNode.getNodeType() == 1) {
						OsApplication osApplication = new OsApplication(((Element) osApplicationNode).getAttribute("xmlns"));
						NodeList osApplicationChildren = osApplicationNode.getChildNodes();
						for (int j=0; j<osApplicationChildren.getLength(); j++) {
							Node osTasksNode = osApplicationChildren.item(j);
							if (osTasksNode.getNodeType() == 1) {
								NodeList osTasksChildren = osTasksNode.getChildNodes();
								for (int k=0; k<osTasksChildren.getLength(); k++) {
									Node osTaskNode = osTasksChildren.item(k);
									if (osTaskNode.getNodeType() == 1) {
										NodeList osTaskChildren = osTaskNode.getChildNodes();
										for (int l=0; l<osTaskChildren.getLength(); l++) {
											Node runnableNode = osTaskChildren.item(l);
											if (runnableNode.getNodeType() == 1) {
												Runnable runnable = new Runnable(runnableNode.getTextContent());
												OsTask osTask = new OsTask(runnable);
												osApplication.addOsTask(osTask);
											}
										}
									}
								}
							}
						}
						m.addOsApplication(osApplication);
					}
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return m;
	}
	
	public static void printLog() {
		try {

			String _logPath = "io/output/log";
			File _logDir = new File(_logPath);
			
			if (_logDir.exists()) {
				delete(_logDir);
			}
			
			_logDir.mkdir();
			
			for (ECU e : Architecture.getECUs()) {
				String _ecuPath = "io/output/log/ecu" + e.id;
				File _ecuDir = new File(_ecuPath);
				_ecuDir.mkdir();
				for (Core c : e.cores) {
					Collections.sort(c.scheduler.logs);
					String _corePath = _ecuPath + "/core" + c.id;
					File _coreDir = new File(_corePath);
					_coreDir.mkdir();
					File _eventsFile = new File(_corePath + "/events.html");
					File _statsFile = new File(_corePath + "/stats.html");
					FileWriter fw = new FileWriter(_statsFile, false);
					PrintWriter pw = new PrintWriter(fw);
					StringBuilder builder = new StringBuilder();
					builder.append("<html><body><u><b>General Stats</b></u><br/><br/>");
					builder.append("<b>CPU load: </b>" + (100.0-Double.valueOf(c.scheduler.data.get("idle"))) + "%");
					builder.append("<br/><b>End to end delay SWC0: </b>" + c.scheduler.data.get("e2e0"));
					builder.append("<br/><b>End to end delay SWC1: </b>" + c.scheduler.data.get("e2e1"));
					builder.append("<br/><b>End to end delay SWC2: </b>" + c.scheduler.data.get("e2e2"));
					builder.append("<br/><b>End to end delay SWC3: </b>" + c.scheduler.data.get("e2e3"));
					builder.append("<br/><b>Intra ECU Bus: </b>" + e.bus.delay);
					builder.append("<br/><b>Inter ECU Bus: </b>" + Architecture.bus.delay);
					if (c.scheduler.tasks.get(0).getMessage() != null) {
						builder.append("<br/><b>Message age: </b>" + c.scheduler.tasks.get(0).getMessage().getMessageAge());	
					}
					pw.print(builder.toString());
					fw.close();
					builder = new StringBuilder();
					fw = new FileWriter(_eventsFile, false);
					pw = new PrintWriter(fw);
					builder.append("<u><b>Events</b></u><br/><br/>");
					builder.append("<table border=\"1\"><tr><td><b>Task ID</b></td><td><b>Time</b></td><td><b>Log type</b></td><td><b>Message</b></td><td><b>Severity</b></td></tr>");
					for (int i=0; i<c.scheduler.logs.size(); i++) {
						String color = "black";
						Log currentLog = c.scheduler.logs.get(i);
						if (c.scheduler.logs.get(i).logSeverity == LogSeverity.CRITICAL) {
							color = "red";
						} else if (c.scheduler.logs.get(i).logType == LogType.MESSAGE_SENT || c.scheduler.logs.get(i).logType == LogType.MESSAGE_RECEIVED) {
							color = "blue";
						}
						builder.append("<tr>");
						builder.append(addLogCell(color, Integer.valueOf(currentLog.taskId).toString()));
						builder.append(addLogCell(color, Double.valueOf(currentLog.time).toString()));
						builder.append(addLogCell(color, currentLog.logType.toString()));
						builder.append(addLogCell(color, currentLog.message));
						builder.append(addLogCell(color, currentLog.logSeverity.toString()));
						builder.append("</tr>");
					}
					builder.append("</table></body></html>");
					pw.print(builder.toString());
					pw.close();
					fw.close();		
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static String addLogCell(String color, String text) {
		return "<td><font color = \"" + color + "\">" + text + "</font></td>";
	}
	
	private static void delete(File _file) throws IOException {
		
		if (_file.isDirectory()) {
			try {
				if (_file.list().length == 0) {
					_file.delete();
					System.out.println("Directory deleted: " + _file.getAbsolutePath());
				} else {
					String files[] = _file.list();
					for (String tmp : files) {
						File _del = new File(_file, tmp);
						delete(_del);
					}
					if (_file.list().length == 0) {
						_file.delete();
						System.out.println("Directory deleted: " + _file.getAbsolutePath());
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			_file.delete();
			System.out.println("File deleted: " + _file.getAbsolutePath());
		}
		
	}
	
	public static void parseInputFile() {
		
	}
	
}
