package br.unb.unbiquitous.ubiquitos.uos.integration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.unb.unbiquitous.ubiquitos.uos.adaptabitilyEngine.Gateway;
import br.unb.unbiquitous.ubiquitos.uos.adaptabitilyEngine.ServiceCallException;
import br.unb.unbiquitous.ubiquitos.uos.adaptabitilyEngine.UosEventListener;
import br.unb.unbiquitous.ubiquitos.uos.application.UosApplication;
import br.unb.unbiquitous.ubiquitos.uos.driverManager.DriverData;
import br.unb.unbiquitous.ubiquitos.uos.messageEngine.messages.Notify;
import br.unb.unbiquitous.ubiquitos.uos.messageEngine.messages.ServiceCall;
import br.unb.unbiquitous.ubiquitos.uos.messageEngine.messages.ServiceResponse;
import br.unb.unbiquitous.ubiquitos.uos.ontologyEngine.api.OntologyDeploy;
import br.unb.unbiquitous.ubiquitos.uos.ontologyEngine.api.OntologyStart;
import br.unb.unbiquitous.ubiquitos.uos.ontologyEngine.api.OntologyUndeploy;

public class PingApp implements UosApplication, UosEventListener {

	public boolean run = true;
	public Map<String, Boolean> assertions = new HashMap<String, Boolean>();
	
	boolean eventReceived;
	
	public void start(Gateway gateway, OntologyStart ontology) {
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
					gateway.registerForEvent(this, data.getDevice(), data.getDriver().getName(), "reminder");
					
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
			ServiceCall echoGo = new ServiceCall(data.getDriver().getName(), "echo");
						echoGo.addParameter("text", "my text");
			ServiceResponse echoComeback = gateway.callService(data.getDevice(), echoGo);
			String echoMsg = echoComeback.getResponseData("text");
			if (!echoMsg.equals("my text")){
				throw new AssertionError("The returned message wasn't 'my text' it was '"+echoMsg+"'.");
			}
		}
	}

	public void stop() throws Exception {	run = false;	}

	public void init(OntologyDeploy ontology) {}

	public void tearDown(OntologyUndeploy ontology) throws Exception {}

}
