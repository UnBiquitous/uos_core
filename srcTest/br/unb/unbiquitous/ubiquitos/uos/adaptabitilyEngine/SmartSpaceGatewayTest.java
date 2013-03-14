package br.unb.unbiquitous.ubiquitos.uos.adaptabitilyEngine;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;

import br.unb.unbiquitous.ubiquitos.uos.deviceManager.DeviceManager;
import br.unb.unbiquitous.ubiquitos.uos.driverManager.DriverManager;
import br.unb.unbiquitous.ubiquitos.uos.messageEngine.dataType.UpDevice;
import br.unb.unbiquitous.ubiquitos.uos.messageEngine.messages.Notify;
import br.unb.unbiquitous.ubiquitos.uos.messageEngine.messages.ServiceCall;

public class SmartSpaceGatewayTest {

	private AdaptabilityEngine engine;
	private DeviceManager deviceManager;
	private DriverManager driverManager;
	private SmartSpaceGateway gateway;
	
	@Before
	public void setUp(){
		engine = mock(AdaptabilityEngine.class);
		deviceManager = mock(DeviceManager.class);
		driverManager = mock(DriverManager.class);
		gateway = new SmartSpaceGateway();
		gateway.init(engine, null, null, null, deviceManager, driverManager, null, null);
	}
	
	
	@Test public void callServiceDelegatesToAdaptabilityEngine() throws Exception{
		UpDevice target = new UpDevice("a");
		ServiceCall call = new ServiceCall("d", "s");
		gateway.callService(target, call);
		verify(engine).callService(target, call);
	}
	@Test public void callServiceWithMultipleParamsAlsoDelegatesToAdaptabilityEngine() throws Exception{
		UpDevice target = new UpDevice("a");
		gateway.callService(target, "s", "d", "i", "t", new HashMap<String, String>());
		verify(engine).callService(target, "s", "d", "i", "t", new HashMap<String, String>());
	}
	
	private static class EventListener implements UosEventListener{
		public void handleEvent(Notify event) {}
	}
	
	@Test public void registerForEventDelegatesToAdaptabilityEngine() throws Exception{
		UpDevice target = new UpDevice("a");
		UosEventListener listener = new EventListener();
		gateway.registerForEvent(listener,target, "d", "e");
		verify(engine).registerForEvent(listener, target, "d", null, "e");
	}
	
	@Test public void registerForEventWithMultipleParamsAlsoDelegatesToAdaptabilityEngine() throws Exception{
		UpDevice target = new UpDevice("a");
		UosEventListener listener = new EventListener();
		gateway.registerForEvent(listener,target, "d", "i", "e");
		verify(engine).registerForEvent(listener, target, "d", "i", "e");
	}
	
	@Test public void sendEventNotifyDelegatesToAdaptabilityEngine() throws Exception{
		UpDevice target = new UpDevice("a");
		Notify event = new Notify("d", "s");
		gateway.sendEventNotify(event, target);
		verify(engine).sendEventNotify(event, target);
	}
	
	@Test public void unregisterForEventDelegatesToAdaptabilityEngine() throws Exception{
		UosEventListener listener = new EventListener();
		gateway.unregisterForEvent(listener);
		verify(engine).unregisterForEvent(listener);
	}
	
	@Test public void unregisterForEventWithMultipleParamsAlsoDelegatesToAdaptabilityEngine() throws Exception{
		UosEventListener listener = new EventListener();
		UpDevice target = new UpDevice("a");
		gateway.unregisterForEvent(listener,target,"d","i","e");
		verify(engine).unregisterForEvent(listener,target,"d","i","e");
	}
	
	@Test public void listDriversDelegatesToDeviceManagerConsideringAllDevices(){
		gateway.listDrivers("d");
		verify(driverManager).listDrivers("d", null);
	}
}
