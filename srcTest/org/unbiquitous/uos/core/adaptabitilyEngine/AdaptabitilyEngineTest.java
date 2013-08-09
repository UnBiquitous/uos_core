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
import java.util.ListResourceBundle;
import java.util.ResourceBundle;
import java.util.TreeMap;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.unbiquitous.uos.core.applicationManager.ApplicationManager;
import org.unbiquitous.uos.core.applicationManager.DummyApp;
import org.unbiquitous.uos.core.applicationManager.UOSMessageContext;
import org.unbiquitous.uos.core.driverManager.DriverManager;
import org.unbiquitous.uos.core.messageEngine.MessageEngine;
import org.unbiquitous.uos.core.messageEngine.dataType.UpDevice;
import org.unbiquitous.uos.core.messageEngine.messages.Notify;
import org.unbiquitous.uos.core.messageEngine.messages.ServiceCall;
import org.unbiquitous.uos.core.messageEngine.messages.ServiceResponse;



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
		engine.callService(null, new ServiceCall());
		engine.callService(null, new ServiceCall("",null));
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void callService_shouldFailWithoutAServiceSpecified() throws ServiceCallException{
		engine.callService(null, new ServiceCall());
		engine.callService(null, new ServiceCall(null,""));
	}
	
	@Test public void callService_shouldRedirectLocalCallToDriverManagerForNullDevice() throws Exception {
		final DriverManager _driverManager = mock(DriverManager.class);
		ServiceResponse response = new ServiceResponse();
		when(_driverManager.handleServiceCall((ServiceCall)anyObject(), (UOSMessageContext)anyObject())).thenReturn(response);
		
		engine = new AdaptabilityEngine(){
			public void init(org.unbiquitous.uos.core.UOSComponentFactory factory) {
				this.driverManager = _driverManager;
			}
		};
		engine.init(null);
		
		ServiceCall call = new ServiceCall("my.driver","myService");
		assertEquals(response,engine.callService(null, call));
	}
	

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test public void callService_shouldCallMethodOnAppWhenDriverIsApp() throws Exception{
		final ApplicationManager manager = new ApplicationManager(properties, null);
		DummyApp app = new DummyApp();
		manager.deploy(app, "myId");
		
		engine = new AdaptabilityEngine(){
			public void init(org.unbiquitous.uos.core.UOSComponentFactory factory) {
				this.applicationManager = manager;
			}
		};
		engine.init(null);
		
		ServiceCall serviceCall = new ServiceCall("app","callback","myId");
		TreeMap parameters = new TreeMap();
		serviceCall.setParameters(parameters);
		engine.callService(null,serviceCall);
		
		assertThat(app.callbackMap).isSameAs(parameters);
	}
	
	@Test public void callService_shouldRedirectLocalCallToDriverManagerForCurrentDevice() throws Exception {
		final DriverManager _driverManager = mock(DriverManager.class);
		ServiceResponse response = new ServiceResponse();
		final UpDevice _currentDevice = new UpDevice("me");
		when(_driverManager.handleServiceCall((ServiceCall)anyObject(), (UOSMessageContext)anyObject())).thenReturn(response);
		
		engine = new AdaptabilityEngine(){
			public void init(org.unbiquitous.uos.core.UOSComponentFactory factory) {
				this.driverManager = _driverManager;
				this.currentDevice = _currentDevice;
			}
		};
		engine.init(null);
		
		ServiceCall call = new ServiceCall("my.driver","myService");
		assertEquals(response,engine.callService(_currentDevice, call));
	}
	
	@Test public void callService_shouldCreateAMessageContextLocalCallToDriverManager() throws Exception {
		final UOSMessageContext[] ctx = {null};
		
		final DriverManager _driverManager = new DriverManager(null,null,null,null){
			public ServiceResponse handleServiceCall(ServiceCall call, UOSMessageContext c){
				ctx[0] = c;
				return new ServiceResponse();
			}
		};
		engine = new AdaptabilityEngine(){
			public void init(org.unbiquitous.uos.core.UOSComponentFactory factory) {
				this.driverManager = _driverManager;
			}
		};
		engine.init(null);
		ServiceCall call = new ServiceCall("my.driver","myService");
		engine.callService(null, call);
		
		assertNotNull(ctx[0]);
	}
	

	@Test public void callService_shouldRedirectRemoteCallToMessageEngibeForOtherDevice() throws Exception {
		final MessageEngine _messageEngine = mock(MessageEngine.class);
		ServiceResponse response = new ServiceResponse();
		UpDevice callee = new UpDevice("other");
		ServiceCall call = new ServiceCall("my.driver","myService");
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
		engine.sendEventNotify(notify, device);
		verify(_eventManager).sendEventNotify(notify, device);
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
		engine.registerForEvent(listener, device, "driver", "eventKey");
		verify(_eventManager).registerForEvent(listener, device, "driver", null, "eventKey");
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
		engine.registerForEvent(listener, device, "driver", "id", "eventKey");
		verify(_eventManager).registerForEvent(listener, device, "driver", "id", "eventKey");
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
		engine.unregisterForEvent(listener);
		verify(_eventManager).unregisterForEvent(listener, null, null, null, null);
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
		engine.unregisterForEvent(listener, device, "driver", "id", "eventKey");
		verify(_eventManager).unregisterForEvent(listener, device, "driver", "id", "eventKey");
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
			}
		};
		engine.init(null);
		ServiceCall serviceCall = new ServiceCall();
		UOSMessageContext messageContext = new UOSMessageContext();
		engine.handleServiceCall(serviceCall,messageContext);
		verify(_driverManager).handleServiceCall(serviceCall,messageContext);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test public void handleServiceCall_shouldCallMethodOnAppWhenDriverIsApp() throws Exception{
		final ApplicationManager manager = new ApplicationManager(properties, null);
		DummyApp app = new DummyApp();
		manager.deploy(app, "myId");
		
		engine = new AdaptabilityEngine(){
			public void init(org.unbiquitous.uos.core.UOSComponentFactory factory) {
				this.applicationManager = manager;
			}
		};
		engine.init(null);
		
		ServiceCall serviceCall = new ServiceCall("app","callback","myId");
		TreeMap parameters = new TreeMap();
		serviceCall.setParameters(parameters);
		engine.handleServiceCall(serviceCall,new UOSMessageContext());
		
		assertThat(app.callbackMap).isSameAs(parameters);
	}
	
}
