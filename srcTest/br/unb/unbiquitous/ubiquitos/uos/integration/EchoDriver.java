package br.unb.unbiquitous.ubiquitos.uos.integration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.unb.unbiquitous.ubiquitos.Logger;
import br.unb.unbiquitous.ubiquitos.uos.adaptabitilyEngine.Gateway;
import br.unb.unbiquitous.ubiquitos.uos.application.UOSMessageContext;
import br.unb.unbiquitous.ubiquitos.uos.driverManager.UosEventDriver;
import br.unb.unbiquitous.ubiquitos.uos.messageEngine.dataType.UpDevice;
import br.unb.unbiquitous.ubiquitos.uos.messageEngine.dataType.UpDriver;
import br.unb.unbiquitous.ubiquitos.uos.messageEngine.dataType.UpService.ParameterType;
import br.unb.unbiquitous.ubiquitos.uos.messageEngine.messages.Notify;
import br.unb.unbiquitous.ubiquitos.uos.messageEngine.messages.ServiceCall;
import br.unb.unbiquitous.ubiquitos.uos.messageEngine.messages.ServiceResponse;

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
		response.addParameter("text", call.getParameter("text"));
	}

	@Override
	public void registerListener(ServiceCall call, ServiceResponse response,
			final UOSMessageContext ctx) {
		// if (call.getParameter("event").equals(reminder)){ //TODO:Validate the event
		System.out.println(ctx.getCallerDevice().getNetworkDeviceName()+" trying to register on "+ gateway.getCurrentDevice().getName());
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
