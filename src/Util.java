import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
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
	
	public static void printLog(Architecture arc) {
		try {

			String _logPath = "io/output/log";
			File _logDir = new File(_logPath);
			
			if (_logDir.exists()) {
				delete(_logDir);
			}
			
			_logDir.mkdir();
			
			for (ECU e : arc.getECUs()) {
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
//					builder.append("<br/><b>End to end delay SWC0: </b>" + c.scheduler.data.get("e2e0"));
//					builder.append("<br/><b>End to end delay SWC1: </b>" + c.scheduler.data.get("e2e1"));
//					builder.append("<br/><b>End to end delay SWC2: </b>" + c.scheduler.data.get("e2e2"));
//					builder.append("<br/><b>End to end delay SWC3: </b>" + c.scheduler.data.get("e2e3"));
					
					Hashtable<Integer, Double> averageE2EDelays = new Hashtable<Integer, Double>();
					Hashtable<Integer, Double> worstE2EDelays = new Hashtable<Integer, Double>();
					for (Integer k : c.scheduler.e2eDelays.keySet()) {
						double avg = 0;
						for (Double e2e : c.scheduler.e2eDelays.get(k)) {
							avg += e2e;
							if (!worstE2EDelays.containsKey(k) || worstE2EDelays.get(k) < e2e) {
								worstE2EDelays.put(k, e2e);
							}
						}
						avg = avg/(double)c.scheduler.e2eDelays.get(k).size();
						averageE2EDelays.put(k, avg);
						builder.append("<br/><b>End to end delay SWC" + k + " (average): </b>" + averageE2EDelays.get(k)); 
						builder.append("<br/><b>End to end delay SWC" + k + " (worst): </b>" + worstE2EDelays.get(k));
					}
					
					builder.append("<br/><b>Intra ECU Bus: </b>" + e.bus.delay);
					builder.append("<br/><b>Inter ECU Bus: </b>" + arc.bus.delay);
//					if (c.scheduler.tasks.get(0).getMessages() != null) {
//						builder.append("<br/><b>Message age: </b>" + c.scheduler.tasks.get(0).getMessage().getMessageAge());	
//					}
					pw.print(builder.toString());
					fw.close();
					builder = new StringBuilder();
					fw = new FileWriter(_eventsFile, false);
					pw = new PrintWriter(fw);
					builder.append("<u><b>Events</b></u><br/><br/>");
					builder.append("<table border=\"1\"><tr><td><b>ID</b></td><td><b>Time</b></td><td><b>Log type</b></td><td><b>Description</b></td><td><b>Severity</b></td></tr>");
					for (int i=0; i<c.scheduler.logs.size(); i++) {
						String color = "black";
						Log currentLog = c.scheduler.logs.get(i);
						if (c.scheduler.logs.get(i).logSeverity == LogSeverity.CRITICAL) {
							color = "red";
						} else if (c.scheduler.logs.get(i).logType == LogType.MESSAGE_SENT || c.scheduler.logs.get(i).logType == LogType.MESSAGE_RECEIVED) {
							color = "blue";
						} else if (c.scheduler.logs.get(i).logType == LogType.APPLICATION_START || c.scheduler.logs.get(i).logType == LogType.APPLICATION_FINISHED) {
							color = "orange";
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
	
	public static Architecture parseInputFile(String _path) {
		Architecture a = new Architecture();
		
		try {
			File inputFile = new File(_path);
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(inputFile);
			
			doc.getDocumentElement().normalize();
			System.out.println("Root element: " + doc.getDocumentElement().getNodeName());
			
			NodeList mappingNodeList = doc.getElementsByTagName("Architecture");
			Node architectureNode = mappingNodeList.item(0);
			
			NodeList architectureChildren = architectureNode.getChildNodes();
			Node ecusNode = null;
			Node interecubusNode = null;
			Node swcomponentsNode = null;
			for (int i=0; i < architectureChildren.getLength(); i++) {
				if (architectureChildren.item(i).getNodeName().equalsIgnoreCase("ECUs")) {
					ecusNode = architectureChildren.item(i);
				} else if (architectureChildren.item(i).getNodeName().equalsIgnoreCase("InterECUBus")) {
					interecubusNode = architectureChildren.item(i);
				} else if (architectureChildren.item(i).getNodeName().equalsIgnoreCase("SWComponents")) {
					swcomponentsNode = architectureChildren.item(i);
				}
			}
			
			Hashtable<Integer, Runnable> runnablesHT = new Hashtable<Integer, Runnable>();
			if (swcomponentsNode != null) {
				NodeList swcomponentsChilden = swcomponentsNode.getChildNodes();
				for (int i=0; i<swcomponentsChilden.getLength(); i++) {
					Node swcomponentNode = swcomponentsChilden.item(i);
					if (swcomponentNode.getNodeType() == 1) {
						Integer _cid = Integer.valueOf(swcomponentNode.getAttributes().getNamedItem("id").getNodeValue());
						String _cname = swcomponentNode.getAttributes().getNamedItem("name").getNodeValue();
						SWComponent _component = new SWComponent(_cid, _cname);
						Node runnablesNode = null;
						NodeList swcomponentChildren = swcomponentNode.getChildNodes();
						for (int j=0; j<swcomponentChildren.getLength(); j++) {
							if (swcomponentChildren.item(j).getNodeName().equalsIgnoreCase("Runnables")) {
								runnablesNode = swcomponentChildren.item(j);
							}
						}
						
						if (runnablesNode != null) {
							NodeList runnablesChilden = runnablesNode.getChildNodes();
							for (int j=0; j<runnablesChilden.getLength(); j++) {
								Node runnableNode = runnablesChilden.item(j);
								if (runnableNode.getNodeType() == 1) {
									Integer _rid = Integer.valueOf(runnableNode.getAttributes().getNamedItem("id").getNodeValue());
									String _rname = runnableNode.getAttributes().getNamedItem("name").getNodeValue();
									Integer _rmsg = Integer.valueOf(runnableNode.getAttributes().getNamedItem("messageSize").getNodeValue());
									Integer _rmpr = Integer.valueOf(runnableNode.getAttributes().getNamedItem("messagePriority").getNodeValue());
									Boolean _rmxt = Boolean.valueOf(runnableNode.getAttributes().getNamedItem("messageExtendedIdentifier").getNodeValue());
									Integer _dst = Integer.valueOf(runnableNode.getAttributes().getNamedItem("dst").getNodeValue());
									Double _rbcet = Double.valueOf(runnableNode.getAttributes().getNamedItem("bcet").getNodeValue());
									Double _rwcet = Double.valueOf(runnableNode.getAttributes().getNamedItem("wcet").getNodeValue());
									Runnable r = new Runnable(_rid, _rname, _rbcet, _rwcet, _rmsg, _rmpr, _dst, _rmxt);
									_component.addRunnable(r);
									runnablesHT.put(_rid, r);
								}
							}
							a.addSWComponent(_component);
						}
						
					}
					
				}
				
			}
			
			Hashtable<Integer, ECU> ecusHT = new Hashtable<Integer, ECU>();
			if (ecusNode != null) {
				NodeList ecusChildren = ecusNode.getChildNodes();
				for (int i=0; i<ecusChildren.getLength(); i++) {
					Node ecuNode = ecusChildren.item(i);
					if (ecuNode.getNodeType() == 1) {
						Integer _eid = Integer.valueOf(ecuNode.getAttributes().getNamedItem("id").getNodeValue());
						Double _processorSpeed = Double.valueOf(ecuNode.getAttributes().getNamedItem("processorSpeed").getNodeValue());
						ECU e = new ECU(_eid, _processorSpeed, a);
						NodeList ecuChildren = ecuNode.getChildNodes();
						Node coresNode = null;
						Node intraECUBusNode = null;
						for (int j=0; j<ecuChildren.getLength(); j++) {
							if (ecuChildren.item(j).getNodeName().equalsIgnoreCase("Cores")) {
								coresNode = ecuChildren.item(j);
							} else if (ecuChildren.item(j).getNodeName().equalsIgnoreCase("IntraECUBus")) {
								intraECUBusNode = ecuChildren.item(j);
							}
						}
						
						Hashtable<Integer, Core> coresHT = new Hashtable<Integer, Core>();
						if (coresNode != null) {
							NodeList coresChildren = coresNode.getChildNodes();
							for (int j=0; j<coresChildren.getLength(); j++) {
								Node coreNode = coresChildren.item(j);
								if (coreNode.getNodeType() == 1) {
									Integer _cid = Integer.valueOf(coreNode.getAttributes().getNamedItem("id").getNodeValue());
									Core c = new Core(_cid, e);
									NodeList coreChildren = coreNode.getChildNodes();
									
									Node _osTasksNode = null;
									Node _swComponentsNode = null;
									
									for (int k=0; k<coreChildren.getLength(); k++) {
										System.out.println("Name: " + coreChildren.item(k).getNodeName());
										if (coreChildren.item(k).getNodeName().equalsIgnoreCase("SWComponents")) {
											_swComponentsNode = coreChildren.item(k);
										} else if (coreChildren.item(k).getNodeName().equalsIgnoreCase("OsTasks")) {
											_osTasksNode = coreChildren.item(k);
										}
									}
									
									if (_osTasksNode != null) {
										NodeList osTasksChildren = _osTasksNode.getChildNodes();
										for (int l=0; l<osTasksChildren.getLength(); l++) {
											Node osTaskNode = osTasksChildren.item(l);
											if (osTaskNode.getNodeType() == 1) {
												Integer _otid = Integer.valueOf(osTaskNode.getAttributes().getNamedItem("id").getNodeValue());
												Integer _otperiod = Integer.valueOf(osTaskNode.getAttributes().getNamedItem("period").getNodeValue());
												OsTask _osTask = new OsTask(_otid, _otperiod, c);
												NodeList osTaskChildren = osTaskNode.getChildNodes();
												List<Runnable> _taskRunnables = new ArrayList<Runnable>();
												for (int m=0; m<osTaskChildren.getLength(); m++) {
													Node runnablesNode = osTaskChildren.item(m);
													if (runnablesNode.getNodeType() == 1) {
														NodeList runnablesChildren = runnablesNode.getChildNodes();
														for (int n=0; n<runnablesChildren.getLength(); n++) {
															Node runnableNode = runnablesChildren.item(n);
															if (runnableNode.getNodeType() == 1) {
																Integer _rid = Integer.valueOf(runnableNode.getAttributes().getNamedItem("id").getNodeValue());
																Runnable r = runnablesHT.get(_rid);
																_taskRunnables.add(r);
																_osTask.addRunnable(r);
															}
														}
													}
												}
												c.scheduler.addOsTask(_osTask);
											}
										}	
									}
									
									if (_swComponentsNode != null) {
										NodeList swComponentsChildren = _swComponentsNode.getChildNodes();
										for (int l=0; l<swComponentsChildren.getLength(); l++) {
											Node swComponentNode = swComponentsChildren.item(l);
											if (swComponentNode.getNodeType() == 1) {
												Integer _swcid = Integer.valueOf(swComponentNode.getAttributes().getNamedItem("id").getNodeValue());
												Double _swctime = Double.valueOf(swComponentNode.getAttributes().getNamedItem("time").getNodeValue());
												c.scheduler.loadSWComponentPeriod(_swcid, _swctime);	
											}
										}
									}
									
									coresHT.put(_cid, c);
									e.addCore(c);
								}
							}
						}
						
						if (intraECUBusNode != null) {
							Integer _iebbw = Integer.valueOf(intraECUBusNode.getAttributes().getNamedItem("bandwidth").getNodeValue());
							IntraECUBus ieBus = new IntraECUBus(_iebbw);
							NodeList intraECUBusChildren = intraECUBusNode.getChildNodes();
							for (int j=0; j<intraECUBusChildren.getLength(); j++) {
								Node _iebcoresNode = intraECUBusChildren.item(j);
								if (_iebcoresNode.getNodeType() == 1) {
									NodeList _iebecusChildren = _iebcoresNode.getChildNodes();
									for (int k=0; k<_iebecusChildren.getLength(); k++) {
										Node _iebcoreNode = _iebecusChildren.item(k);
										if (_iebcoreNode.getNodeType() == 1) {
											Integer _iebcid = Integer.valueOf(_iebcoreNode.getAttributes().getNamedItem("id").getNodeValue());
											Core _iebcore = coresHT.get(_iebcid);
											ieBus.cores.add(_iebcore);
										}
									}
								}
							}
							
							e.setBus(ieBus);
							
						}
						
						a.addECU(e);
						ecusHT.put(e.id, e);
					}
					
				}
			}
				
			// Parse InterECUBus
			if (interecubusNode != null) {
				Integer _iebbw = Integer.valueOf(interecubusNode.getAttributes().getNamedItem("bandwidth").getNodeValue());
				InterECUBus ieBus = new InterECUBus(_iebbw);
				NodeList interecubusChildren = interecubusNode.getChildNodes();
				for (int i=0; i<interecubusChildren.getLength(); i++) {
					Node _iebecusNode = interecubusChildren.item(i);
					if (_iebecusNode.getNodeType() == 1) {
						NodeList _iebecusChildren = _iebecusNode.getChildNodes();
						for (int j=0; j<_iebecusChildren.getLength(); j++) {
							Node _iebecuNode = _iebecusChildren.item(j);
							if (_iebecuNode.getNodeType() == 1) {
								Integer _iebecuid = Integer.valueOf(_iebecuNode.getAttributes().getNamedItem("id").getNodeValue());
								ECU e = ecusHT.get(_iebecuid);
								ieBus.addECU(e);	
							}
						}
					}
				}
				a.setInterECUBus(ieBus);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	
		return a;
	}
	
}
