import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
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
								Component component = new Component(componentNode.getTextContent());
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
	
	public static void printLog(List<Log> log, Hashtable<String, String> data) {
		try {
			File logFile = new File("io/output/log.html");
			FileWriter fw = new FileWriter(logFile, false);
			PrintWriter pw = new PrintWriter(fw);
			StringBuilder builder = new StringBuilder();
			builder.append("<html><body>");
			builder.append("% time idle: " + data.get("idle") + "%");
			builder.append("<table border=\"1\"><tr><td><b>Task ID</b></td><td><b>Time</b></td><td><b>Log type</b></td><td><b>Severity</b></td></tr>");
			for (int i=0; i<log.size(); i++) {
				String color = "black";
				Log currentLog = log.get(i);
				if (log.get(i).logSeverity == LogSeverity.CRITICAL) {
					color = "red";
				}
				builder.append("<tr>");
				builder.append(addLogCell(color, Integer.valueOf(currentLog.taskId).toString()));
				builder.append(addLogCell(color, Integer.valueOf(currentLog.time).toString()));
				builder.append(addLogCell(color, currentLog.logType.toString()));
				builder.append(addLogCell(color, currentLog.logSeverity.toString()));
				builder.append("</tr>");
			}
			builder.append("</table></body></html>");
			pw.print(builder.toString());
			pw.close();
			fw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static String addLogCell(String color, String text) {
		return "<td><font color = \"" + color + "\">" + text + "</font></td>";
	}
	
}
