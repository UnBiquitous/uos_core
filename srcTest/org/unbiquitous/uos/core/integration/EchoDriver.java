package org.unbiquitous.uos.core.integration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.unbiquitous.uos.core.Logger;
import org.unbiquitous.uos.core.adaptabitilyEngine.Gateway;
import org.unbiquitous.uos.core.application.UOSMessageContext;
import org.unbiquitous.uos.core.driverManager.UosEventDriver;
import org.unbiquitous.uos.core.messageEngine.dataType.UpDevice;
import org.unbiquitous.uos.core.messageEngine.dataType.UpDriver;
import org.unbiquitous.uos.core.messageEngine.dataType.UpService.ParameterType;
import org.unbiquitous.uos.core.messageEngine.messages.Notify;
import org.unbiquitous.uos.core.messageEngine.messages.ServiceCall;
import org.unbiquitous.uos.core.messageEngine.messages.ServiceResponse;


public class EchoDriver implements UosEventDriver {
	private static final Logger logger = Logger.getLogger(EchoDriver.class);

	public Map<String, Boolean> assertions = new HashMap<String, Boolean>();
	
	private Gateway gateway;
	private String id;


	private static final UpDriver driver = new UpDriver("br.unbiquitous.Echo");
	{
		driver.addService("echo").addParameter("text", ParameterType.MANDATORY);
		driver.addEvent("reminder").addParameter("text",
				ParameterType.MANDATORY);
	}

	public UpDriver getDriver() {
		return driver;
	}

	public void init(Gateway gateway, String instanceId) {
		this.gateway = gateway;
		this.id = instanceId;
	}

	public void destroy() {
	}

	public void echo(ServiceCall call, ServiceResponse response,
			UOSMessageContext ctx) {
		response.addParameter("text", (String) call.getParameter("text"));
	}

	@Override
	public void registerListener(ServiceCall call, ServiceResponse response,
			final UOSMessageContext ctx) {
		// if (call.getParameter("event").equals(reminder)){ //TODO:Validate the event
		new Thread() {
			public void run() {
				try {
					Thread.sleep(100);// Wait a little bit
					Notify reminder = new Notify("reminder","br.unbiquitous.Echo", id);
					gateway.sendEventNotify(reminder, new UpDevice("dummy").addNetworkInterface(ctx.getCallerDevice().getNetworkDeviceName(), ctx.getCallerDevice().getNetworkDeviceType()));
					Thread.sleep(100);// Wait a little bit
				} catch (Exception e) {
					logger.error(e);
					assertions.put("Some unexpected Exception occurred:"+e.getMessage(), false);
				}
			}
		}.start();
	}

	@Override
	public void unregisterListener(ServiceCall call, ServiceResponse response,
			UOSMessageContext ctx) {
		// TODO Auto-generated method stub

	}

	@Override
	public List<UpDriver> getParent() {
		// TODO Auto-generated method stub
		return null;
	}

}
