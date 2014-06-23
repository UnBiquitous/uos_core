package org.unbiquitous.uos.core.integration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.unbiquitous.uos.core.InitialProperties;
import org.unbiquitous.uos.core.adaptabitilyEngine.Gateway;
import org.unbiquitous.uos.core.adaptabitilyEngine.ServiceCallException;
import org.unbiquitous.uos.core.adaptabitilyEngine.UosEventListener;
import org.unbiquitous.uos.core.applicationManager.UosApplication;
import org.unbiquitous.uos.core.driverManager.DriverData;
import org.unbiquitous.uos.core.messageEngine.messages.Call;
import org.unbiquitous.uos.core.messageEngine.messages.Notify;
import org.unbiquitous.uos.core.messageEngine.messages.Response;
import org.unbiquitous.uos.core.ontologyEngine.api.OntologyDeploy;
import org.unbiquitous.uos.core.ontologyEngine.api.OntologyStart;
import org.unbiquitous.uos.core.ontologyEngine.api.OntologyUndeploy;


public class PingApp implements UosApplication, UosEventListener {

	public boolean run = true;
	public Map<String, Boolean> assertions = new HashMap<String, Boolean>();
	
	boolean eventReceived;
	static public PingApp instance;
	
	public void start(Gateway gateway, OntologyStart ontology) {
		PingApp.instance = this;
		try {
			boolean started = false;
			while(run){
				if (!gateway.getCurrentDevice().getName().equals("my.cell")){
					throw new AssertionError("PingApp should run on 'my.cell' .");
				}
				List<DriverData> echoDriver = gateway.listDrivers("br.unbiquitous.Echo");
				if ( echoDriver != null){
					started = true;
					testEcho(gateway, echoDriver);
					
					DriverData data = echoDriver.get(0);
					gateway.register(this, data.getDevice(), data.getDriver().getName(), "reminder");
					
					run = false;
				}
				Thread.sleep(100);
			}
			if(!started){
				throw new AssertionError("No 'br.unbiquitous.Echo' found");
			}
			/*  TODO : Fabs : Finish this test
			 * The problem seems to be on the IntegrationConnectionManager
			 * regarding the PipedStreams
			if(!eventReceived){
				throw new AssertionError("No 'reminder' event received found");
			}*/
		} catch (AssertionError e) {
			assertions.put(e.getMessage(), false);
		} catch (Exception e) {
			assertions.put("Some unexpected Exception occurred:"+e.getMessage(), false);
		}
		synchronized (PingApp.instance) {
			PingApp.instance.notifyAll();
		}
	}

	public void handleEvent(Notify event) {
		if (event.getEventKey().equals("reminder")){
			eventReceived = true;
		}
		
	}
	
	public void testEcho(Gateway gateway, List<DriverData> echoDriver)
			throws AssertionError, ServiceCallException {
		if (echoDriver.size() != 1){
			throw new AssertionError("More than one EchoDriver found. Should be only 1.");
		}else if (!echoDriver.get(0).getDevice().getName().equals("my.pc")){
			throw new AssertionError("The driver must be on 'my.pc'");
		}else{
			DriverData data = echoDriver.get(0);
			Call echoGo = new Call(data.getDriver().getName(), "echo");
						echoGo.addParameter("text", "my text");
			Response echoComeback = gateway.callService(data.getDevice(), echoGo);
			String echoMsg = echoComeback.getResponseString("text");
			if (!echoMsg.equals("my text")){
				throw new AssertionError("The returned message wasn't 'my text' it was '"+echoMsg+"'.");
			}
		}
	}

	public void stop() throws Exception {	run = false;	}

	public void init(OntologyDeploy ontology, InitialProperties props, String appId) {}

	public void tearDown(OntologyUndeploy ontology) throws Exception {}

}
