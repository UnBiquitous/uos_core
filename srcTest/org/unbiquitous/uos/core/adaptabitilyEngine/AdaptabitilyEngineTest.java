package org.unbiquitous.uos.core.adaptabitilyEngine;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.ListResourceBundle;
import java.util.ResourceBundle;
import java.util.TreeMap;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.unbiquitous.uos.core.InitialProperties;
import org.unbiquitous.uos.core.applicationManager.ApplicationManager;
import org.unbiquitous.uos.core.applicationManager.CallContext;
import org.unbiquitous.uos.core.applicationManager.DummyApp;
import org.unbiquitous.uos.core.deviceManager.DeviceManager;
import org.unbiquitous.uos.core.driverManager.DriverManager;
import org.unbiquitous.uos.core.messageEngine.MessageEngine;
import org.unbiquitous.uos.core.messageEngine.dataType.UpDevice;
import org.unbiquitous.uos.core.messageEngine.messages.Call;
import org.unbiquitous.uos.core.messageEngine.messages.Notify;
import org.unbiquitous.uos.core.messageEngine.messages.Response;
import org.unbiquitous.uos.core.network.model.NetworkDevice;



public class AdaptabitilyEngineTest {

	private AdaptabilityEngine engine ;
	private ResourceBundle properties;
	
	
	@Before public void setUp() throws IOException{
		engine = new AdaptabilityEngine();
		new File("resources/owl/uoscontext.owl").createNewFile();
		properties = new ListResourceBundle() {
			protected Object[][] getContents() {
				return new Object[][] {
						{"ubiquitos.ontology.path","resources/owl/uoscontext.owl"},
//						{"ubiquitos.ontology.reasonerFactory","br.unb.unbiquitous.ubiquitos.ontology.OntologyReasonerTest"},
				};
			}
		};
	}
	
	@After public void tearDown(){
		new File("resources/owl/uoscontext.owl").delete();
	}
	
//	public void init(
//			ConnectionManagerControlCenter connectionManagerControlCenter, 
//			DriverManager driverManager, 
//			UpDevice currentDevice,
//			UOSApplicationContext applicationContext,
//			MessageEngine messageEngine,
//			ConnectivityManager connectivityManager) {
	
//	public ServiceResponse callService(
//			UpDevice device,
//			String serviceName, 
//			String driverName, 
//			String instanceId,
//			String securityType,
//			Map<String,String> parameters) throws ServiceCallException{
	@Test(expected=IllegalArgumentException.class)
	public void callService_shouldFailWithoutAServiceCall() throws ServiceCallException{
		engine.callService(null, null);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void callService_shouldFailWithoutADriverSpecified() throws ServiceCallException{
		engine.callService(null, new Call());
		engine.callService(null, new Call("",null));
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void callService_shouldFailWithoutAServiceSpecified() throws ServiceCallException{
		engine.callService(null, new Call());
		engine.callService(null, new Call(null,""));
	}
	
	@Test public void callService_shouldRedirectLocalCallToDriverManagerForNullDevice() throws Exception {
		final DriverManager _driverManager = mock(DriverManager.class);
		Response response = new Response();
		when(_driverManager.handleServiceCall((Call)anyObject(), (CallContext)anyObject())).thenReturn(response);
		
		engine = new AdaptabilityEngine(){
			public void init(org.unbiquitous.uos.core.UOSComponentFactory factory) {
				this.driverManager = _driverManager;
				this.deviceManager = mock(DeviceManager.class);
			}
		};
		engine.init(null);
		
		Call call = new Call("my.driver","myService");
		assertEquals(response,engine.callService(null, call));
	}
	

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test public void callService_shouldCallMethodOnAppWhenDriverIsApp() throws Exception{
		final ApplicationManager manager = new ApplicationManager(new InitialProperties(properties), null,null);
		DummyApp app = new DummyApp();
		manager.deploy(app, "myId");
		
		engine = new AdaptabilityEngine(){
			public void init(org.unbiquitous.uos.core.UOSComponentFactory factory) {
				this.applicationManager = manager;
				this.deviceManager = mock(DeviceManager.class);
			}
		};
		engine.init(null);
		
		Call serviceCall = new Call("app","callback","myId");
		TreeMap parameters = new TreeMap();
		serviceCall.setParameters(parameters);
		engine.callService(null,serviceCall);
		
		assertThat(app.callbackMap).isSameAs(parameters);
	}
	
	@Test public void callService_shouldRedirectLocalCallToDriverManagerForCurrentDevice() throws Exception {
		final DriverManager _driverManager = mock(DriverManager.class);
		Response response = new Response();
		final UpDevice _currentDevice = new UpDevice("me");
		when(_driverManager.handleServiceCall((Call)anyObject(), (CallContext)anyObject())).thenReturn(response);
		
		engine = new AdaptabilityEngine(){
			public void init(org.unbiquitous.uos.core.UOSComponentFactory factory) {
				this.driverManager = _driverManager;
				this.currentDevice = _currentDevice;
				this.deviceManager = mock(DeviceManager.class);
			}
		};
		engine.init(null);
		
		Call call = new Call("my.driver","myService");
		assertEquals(response,engine.callService(_currentDevice, call));
	}
	
	@Test public void callService_shouldCreateAMessageContextLocalCallToDriverManager() throws Exception {
		final CallContext[] ctx = {null};
		
		final DriverManager _driverManager = new DriverManager(null,null,null,null){
			public Response handleServiceCall(Call call, CallContext c){
				ctx[0] = c;
				return new Response();
			}
		};
		engine = new AdaptabilityEngine(){
			public void init(org.unbiquitous.uos.core.UOSComponentFactory factory) {
				this.driverManager = _driverManager;
				this.deviceManager = mock(DeviceManager.class);
			}
		};
		engine.init(null);
		Call call = new Call("my.driver","myService");
		engine.callService(null, call);
		
		assertNotNull(ctx[0]);
	}
	

	@Test public void callService_shouldRedirectRemoteCallToMessageEngineForOtherDevice() throws Exception {
		final MessageEngine _messageEngine = mock(MessageEngine.class);
		Response response = new Response();
		UpDevice callee = new UpDevice("other");
		Call call = new Call("my.driver","myService");
		when(_messageEngine.callService(callee, call)).thenReturn(response);
		engine = new AdaptabilityEngine(){
			public void init(org.unbiquitous.uos.core.UOSComponentFactory factory) {
				this.currentDevice = new UpDevice("me");
				this.messageEngine = _messageEngine;
			}
		};
		engine.init(null);
		assertEquals(response,engine.callService(callee, call));
	}
	
	@Test(expected=ServiceCallException.class) 
	public void callService_RemoteCallMustHandleNullResponseAsAnError() throws Exception {
		final MessageEngine _messageEngine = mock(MessageEngine.class);
		UpDevice callee = new UpDevice("other");
		Call call = new Call("my.driver","myService");
		when(_messageEngine.callService(callee, call)).thenReturn(null);
		engine = new AdaptabilityEngine(){
			public void init(org.unbiquitous.uos.core.UOSComponentFactory factory) {
				this.currentDevice = new UpDevice("me");
				this.messageEngine = _messageEngine;
			}
		};
		engine.init(null);
		engine.callService(callee, call);
	}
	
	//TODO : AdaptabilityEngine : callService : Test Stream Service (Local and Remote)
	
	@Test public void sendEventNotify_shouldDelagateToEventManager() throws Exception{
		final EventManager _eventManager = mock(EventManager.class);
		
		engine = new AdaptabilityEngine(){
			public void init(org.unbiquitous.uos.core.UOSComponentFactory factory) {
				this.eventManager = _eventManager;
			}
		};
		engine.init(null);
		
		Notify notify = new Notify();
		UpDevice device = new UpDevice();
		engine.notify(notify, device);
		verify(_eventManager).notify(notify, device);
	}
	
	@Test public void registerForEvent_shouldDelagateToEventManager() throws Exception{
		final EventManager _eventManager = mock(EventManager.class);
		engine = new AdaptabilityEngine(){
			public void init(org.unbiquitous.uos.core.UOSComponentFactory factory) {
				this.eventManager = _eventManager;
			}
		};
		engine.init(null);
		UosEventListener listener = mock(UosEventListener.class);
		UpDevice device = new UpDevice();
		engine.register(listener, device, "driver", "eventKey");
		verify(_eventManager).register(listener, device, "driver", null, "eventKey", null);
	}
	
	@Test public void registerForEvent_shouldDelagateToEventManagerWithId() throws Exception{
		final EventManager _eventManager = mock(EventManager.class);
		engine = new AdaptabilityEngine(){
			public void init(org.unbiquitous.uos.core.UOSComponentFactory factory) {
				this.eventManager = _eventManager;
			}
		};
		engine.init(null);
		UosEventListener listener = mock(UosEventListener.class);
		UpDevice device = new UpDevice();
		engine.register(listener, device, "driver", "id", "eventKey");
		verify(_eventManager).register(listener, device, "driver", "id", "eventKey", null);
	}
	
	@Test public void registerForEvent_shouldDelagateToEventManagerWithParameters() throws Exception{
		final EventManager _eventManager = mock(EventManager.class);
		engine = new AdaptabilityEngine(){
			public void init(org.unbiquitous.uos.core.UOSComponentFactory factory) {
				this.eventManager = _eventManager;
			}
		};
		engine.init(null);
		UosEventListener listener = mock(UosEventListener.class);
		UpDevice device = new UpDevice();
		HashMap<String, Object> params = new HashMap<String, Object>();
		engine.register(listener, device, "driver", "id", "eventKey",params);
		verify(_eventManager).register(listener, device, "driver", "id", 
				"eventKey", params);
	}
	
	@Test public void unregisterForEvent_shouldDelagateToEventManager() throws Exception{
		final EventManager _eventManager = mock(EventManager.class);
		engine = new AdaptabilityEngine(){
			public void init(org.unbiquitous.uos.core.UOSComponentFactory factory) {
				this.eventManager = _eventManager;
			}
		};
		engine.init(null);
		UosEventListener listener = mock(UosEventListener.class);
		engine.unregister(listener);
		verify(_eventManager).unregister(listener, null, null, null, null);
	}
	
	@Test public void unregisterForEvent_shouldDelagateToEventManagerWithId() throws Exception{
		final EventManager _eventManager = mock(EventManager.class);
		engine = new AdaptabilityEngine(){
			public void init(org.unbiquitous.uos.core.UOSComponentFactory factory) {
				this.eventManager = _eventManager;
			}
		};
		engine.init(null);
		UosEventListener listener = mock(UosEventListener.class);
		UpDevice device = new UpDevice();
		engine.unregister(listener, device, "driver", "id", "eventKey");
		verify(_eventManager).unregister(listener, device, "driver", "id", "eventKey");
	}
	
	@Test public void handleNofify_shouldDelagateToEventManager() throws Exception{
		final EventManager _eventManager = mock(EventManager.class);
		engine = new AdaptabilityEngine(){
			public void init(org.unbiquitous.uos.core.UOSComponentFactory factory) {
				this.eventManager = _eventManager;
			}
		};
		engine.init(null);
		UpDevice device = new UpDevice();
		Notify notify = new Notify();
		engine.handleNofify(notify,device);
		verify(_eventManager).handleNofify(notify,device);
	}
	
	@Test public void handleServiceCall_shouldDelagateToEventManager() throws Exception{
		final DriverManager _driverManager = mock(DriverManager.class);
		engine = new AdaptabilityEngine(){
			public void init(org.unbiquitous.uos.core.UOSComponentFactory factory) {
				this.driverManager = _driverManager;
				this.deviceManager = mock(DeviceManager.class);
			}
		};
		engine.init(null);
		Call serviceCall = new Call();
		CallContext messageContext = new CallContext();
		engine.handleServiceCall(serviceCall,messageContext);
		verify(_driverManager).handleServiceCall(serviceCall,messageContext);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test public void handleServiceCall_shouldCallMethodOnAppWhenDriverIsApp() throws Exception{
		final ApplicationManager manager = new ApplicationManager(new InitialProperties(properties), null,null);
		DummyApp app = new DummyApp();
		manager.deploy(app, "myId");
		
		engine = new AdaptabilityEngine(){
			public void init(org.unbiquitous.uos.core.UOSComponentFactory factory) {
				this.applicationManager = manager;
				this.deviceManager = mock(DeviceManager.class);
			}
		};
		engine.init(null);
		
		Call serviceCall = new Call("app","callback","myId");
		TreeMap parameters = new TreeMap();
		serviceCall.setParameters(parameters);
		engine.handleServiceCall(serviceCall,new CallContext());
		
		assertThat(app.callbackMap).isSameAs(parameters);
	}
	
	@Test public void handleServiceCall_setUpDeviceOnContext() throws Exception{
		final DriverManager _driverManager = mock(DriverManager.class);
		final DeviceManager _deviceManager = mock(DeviceManager.class);
		
		engine = new AdaptabilityEngine(){
			public void init(org.unbiquitous.uos.core.UOSComponentFactory factory) {
				this.driverManager = _driverManager;
				this.deviceManager = _deviceManager;
			}
		};
		when(_deviceManager.retrieveDevice("addr", "type"))
			.thenReturn(new UpDevice("MyGuy"));
		
		engine.init(null);
		CallContext ctx = new CallContext();
		ctx.setCallerNetworkDevice(new NetworkDevice() {
			public String getNetworkDeviceName() {	return "addr:port";	}
			public String getNetworkDeviceType() {	return "type";	}
		});
		engine.handleServiceCall(new Call(),ctx);
		assertThat(ctx.getCallerDevice()).isNotNull();
		assertThat(ctx.getCallerDevice().getName()).isEqualTo("MyGuy");
	}
	
}

