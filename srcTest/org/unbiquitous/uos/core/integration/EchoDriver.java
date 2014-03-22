package org.unbiquitous.uos.core.integration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.unbiquitous.uos.core.InitialProperties;
import org.unbiquitous.uos.core.UOSLogging;
import org.unbiquitous.uos.core.adaptabitilyEngine.Gateway;
import org.unbiquitous.uos.core.applicationManager.CallContext;
import org.unbiquitous.uos.core.driverManager.UosEventDriver;
import org.unbiquitous.uos.core.messageEngine.dataType.UpDevice;
import org.unbiquitous.uos.core.messageEngine.dataType.UpDriver;
import org.unbiquitous.uos.core.messageEngine.dataType.UpService.ParameterType;
import org.unbiquitous.uos.core.messageEngine.messages.Notify;
import org.unbiquitous.uos.core.messageEngine.messages.Call;
import org.unbiquitous.uos.core.messageEngine.messages.Response;


public class EchoDriver implements UosEventDriver {
	private static final Logger logger = UOSLogging.getLogger();

	public Map<String, Boolean> assertions = new HashMap<String, Boolean>();
	
	private Gateway gateway;
	private String id;

	static public EchoDriver instance;

	private static final UpDriver driver = new UpDriver("br.unbiquitous.Echo");
	{
		driver.addService("echo").addParameter("text", ParameterType.MANDATORY);
		driver.addEvent("reminder").addParameter("text",
				ParameterType.MANDATORY);
	}

	public UpDriver getDriver() {
		return driver;
	}

	public void init(Gateway gateway, InitialProperties properties, String instanceId) {
		EchoDriver.instance = this;
		this.gateway = gateway;
		this.id = instanceId;
	}

	public void destroy() {
	}

	public void echo(Call call, Response response,
			CallContext ctx) {
		response.addParameter("text", (String) call.getParameter("text"));
	}

	@Override
	public void registerListener(Call call, Response response,
			final CallContext ctx) {
		// if (call.getParameter("event").equals(reminder)){ //TODO:Validate the event
		new Thread() {
			public void run() {
				try {
					Thread.sleep(100);// Wait a little bit
					Notify reminder = new Notify("reminder","br.unbiquitous.Echo", id);
					gateway.notify(reminder, new UpDevice("dummy").addNetworkInterface(ctx.getCallerNetworkDevice().getNetworkDeviceName(), ctx.getCallerNetworkDevice().getNetworkDeviceType()));
					Thread.sleep(100);// Wait a little bit
				} catch (Exception e) {
					logger.log(Level.SEVERE,"Problems registering listener",e);
					assertions.put("Some unexpected Exception occurred:"+e.getMessage(), false);
				}
			}
		}.start();
	}

	@Override
	public void unregisterListener(Call call, Response response,
			CallContext ctx) {
		// TODO Auto-generated method stub

	}

	@Override
	public List<UpDriver> getParent() {
		// TODO Auto-generated method stub
		return null;
	}

}
