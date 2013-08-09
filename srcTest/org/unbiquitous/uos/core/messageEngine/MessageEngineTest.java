package org.unbiquitous.uos.core.messageEngine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.unbiquitous.json.JSONObject;
import org.unbiquitous.uos.core.SecurityManager;
import org.unbiquitous.uos.core.UOSComponentFactory;
import org.unbiquitous.uos.core.adaptabitilyEngine.AdaptabilityEngine;
import org.unbiquitous.uos.core.applicationManager.UOSMessageContext;
import org.unbiquitous.uos.core.deviceManager.DeviceManager;
import org.unbiquitous.uos.core.messageEngine.dataType.UpDevice;
import org.unbiquitous.uos.core.messageEngine.messages.Notify;
import org.unbiquitous.uos.core.messageEngine.messages.ServiceCall;
import org.unbiquitous.uos.core.messageEngine.messages.ServiceResponse;
import org.unbiquitous.uos.core.network.connectionManager.ConnectionManagerControlCenter;
import org.unbiquitous.uos.core.network.model.NetworkDevice;


public class MessageEngineTest {

	private MessageEngine engine;
	private ServiceCallHandler callHandler;
	private NotifyHandler eventHandler;
	private DeviceManager deviceManager;
	private ConnectionManagerControlCenter connManager;
	private SecurityManager securityManager;
	
	@Before public void setUp(){
		engine = new MessageEngine();
		UOSComponentFactory factory = new UOSComponentFactory(null);
		
		AdaptabilityEngine adaptabilityEngine = mock(AdaptabilityEngine.class);
		callHandler = adaptabilityEngine;
		eventHandler = adaptabilityEngine;
		factory.set(AdaptabilityEngine.class, adaptabilityEngine);
		factory.set(AdaptabilityEngine.class, adaptabilityEngine);
		
		deviceManager = mock(DeviceManager.class);
		factory.set(DeviceManager.class, deviceManager);
		engine.setDeviceManager(deviceManager);
		
		connManager = mock(ConnectionManagerControlCenter.class);
		factory.set(ConnectionManagerControlCenter.class, connManager);
		
		securityManager = mock(SecurityManager.class);
		factory.set(SecurityManager.class, securityManager);
		
		
		engine.init(factory);
	}
	
	// handleIncomingMessage
	
	// Negative cases
	@Test public void handleIncomingMessage_returnNullForNullMessage() throws Exception{
		assertNull(engine.handleIncomingMessage(null, mock(NetworkDevice.class))); 
	}
	
	@Test public void handleIncomingMessage_returnNullForNullDevice() throws Exception{
		assertNull(engine.handleIncomingMessage("", null)); 
	}
	
	@Test public void handleIncomingMessage_returnErrorForMalformedJson() throws Exception{
		JSONObject response = new JSONObject(engine.handleIncomingMessage("not a json", mock(NetworkDevice.class)));
		assertTrue(response.has("error"));
		assertFalse(response.optString("error").isEmpty());
	}
	
	@Test public void handleIncomingMessage_returnErrorFoMessageWithoutKnowType() throws Exception{
		assertNull(engine.handleIncomingMessage("{type:\"NotKnownType\"}", mock(NetworkDevice.class)));
	}
	
	// SERVICE CALL
		// TODO: Test JSONServiceCall
	@Test public void handleIncomingMessage_delegateServiceCallToHandler() throws Exception{
		JSONObject call = new JSONObject();
			call.put("type", "SERVICE_CALL_REQUEST");
			call.put("driver", "my.driver");
			call.put("service", "my.service");
		NetworkDevice caller = mock(NetworkDevice.class);
		when(callHandler.handleServiceCall(any(ServiceCall.class), any(UOSMessageContext.class)))
			.thenReturn(new ServiceResponse().addParameter("bla", "0"));
		
		JSONObject response = new JSONObject(engine.handleIncomingMessage(call.toString(), caller));
		
		assertFalse(response.has("error"));
		assertEquals("SERVICE_CALL_RESPONSE",response.optString("type"));
		assertEquals("0",response.optJSONObject("responseData").optString("bla"));
		
		ArgumentCaptor<ServiceCall> callCatcher = ArgumentCaptor.forClass(ServiceCall.class);
		ArgumentCaptor<UOSMessageContext> ctxCatcher = ArgumentCaptor.forClass(UOSMessageContext.class);
		verify(callHandler).handleServiceCall(callCatcher.capture(), ctxCatcher.capture());
		
		assertEquals("my.driver",callCatcher.getValue().getDriver());
		assertEquals("my.service",callCatcher.getValue().getService());
		assertEquals(caller,ctxCatcher.getValue().getCallerDevice());
	}
	
	@Test public void handleIncomingMessage_returnErrorWhenServiceCallHandlerFails() throws Exception{
		JSONObject call = new JSONObject();
		call.put("type", "SERVICE_CALL_REQUEST");
		call.put("driver", "my.driver");
		call.put("service", "my.service");
		NetworkDevice caller = mock(NetworkDevice.class);
		when(callHandler.handleServiceCall(any(ServiceCall.class), any(UOSMessageContext.class)))
		.thenThrow(new RuntimeException());
		
		JSONObject response = new JSONObject(engine.handleIncomingMessage(call.toString(), caller));
		
		assertEquals("SERVICE_CALL_RESPONSE",response.optString("type"));
		assertTrue(response.has("error"));
		assertFalse(response.optString("error").isEmpty());
	}
	
	// SERVICE NOTIFY
	@Test public void handleIncomingMessage_delegateNotifyToHandler() throws Exception{
		JSONObject call = new JSONObject();
			call.put("type", "NOTIFY");
			call.put("driver", "my.driver");
			call.put("eventKey", "my.event");
		NetworkDevice caller = mock(NetworkDevice.class);
		when(deviceManager.retrieveDevice(null, null)).thenReturn(new UpDevice("oi"));
		
		assertNull(engine.handleIncomingMessage(call.toString(), caller));
		
		ArgumentCaptor<Notify> eventCatcher = ArgumentCaptor.forClass(Notify.class);
		ArgumentCaptor<UpDevice> deviceCatcher = ArgumentCaptor.forClass(UpDevice.class);
		verify(eventHandler).handleNofify(eventCatcher.capture(), deviceCatcher.capture());
		
		assertEquals("my.driver",eventCatcher.getValue().getDriver());
		assertEquals("my.event",eventCatcher.getValue().getEventKey());
		assertEquals("oi",deviceCatcher.getValue().getName());
	}
	
	@Test public void handleIncomingMessage_ErrorsDuringEventsDontGenerateConsequences() throws Exception{
		JSONObject call = new JSONObject();
			call.put("type", "NOTIFY");
			call.put("driver", "my.driver");
			call.put("eventKey", "my.event");
		NetworkDevice caller = mock(NetworkDevice.class);
		when(deviceManager.retrieveDevice(null, null)).thenThrow(new RuntimeException());
		
		assertNull(engine.handleIncomingMessage(call.toString(), caller));
		//Nothing Happens
	}
	// SERVICE ENCAPSULATED MESSAGE
	
	@Test public void handleIncomingMessage_AnEncapsulatedMessageMustBeDelegatedToTheAppropriateKindOfHanlderAfterTranslation_ServiceCall() throws Exception{
		JSONObject call = new JSONObject();
			call.put("type", "ENCAPSULATED_MESSAGE");
			call.put("innerMessage", "my.msg");
			call.put("securityType", "abacate");
		JSONObject innerCall = new JSONObject();
			innerCall.put("type", "SERVICE_CALL_REQUEST");
			innerCall.put("driver", "my.driver");
			innerCall.put("service", "my.service");
		TranslationHandler translator = mock(TranslationHandler.class);
		when(translator.decode("my.msg", "my.cell")).thenReturn(innerCall.toString());
		when(translator.encode(any(String.class), eq("my.cell"))).thenReturn("my.return");
		when(callHandler.handleServiceCall(any(ServiceCall.class), any(UOSMessageContext.class))).thenReturn(new ServiceResponse());
		when(securityManager.getTranslationHandler("abacate")).thenReturn(translator);
		when(deviceManager.retrieveDevice(null, null)).thenReturn(new UpDevice("my.cell"));
		
		NetworkDevice caller = mock(NetworkDevice.class);
		JSONObject response = new JSONObject(engine.handleIncomingMessage(call.toString(), caller));
		
		assertEquals("ENCAPSULATED_MESSAGE",response.getString("type"));
		assertEquals("abacate",response.getString("securityType"));
		assertEquals("my.return",response.getString("innerMessage"));
	}
	
	@Test public void handleIncomingMessage_AnEncapsulatedMessageMustBeDelegatedToTheAppropriateKindOfHanlderAfterTranslation_Notify() throws Exception{
		JSONObject call = new JSONObject();
		call.put("type", "ENCAPSULATED_MESSAGE");
		call.put("innerMessage", "my.msg");
		call.put("securityType", "abacate");
		JSONObject innerCall = new JSONObject();
		innerCall.put("type", "NOTIFY");
		innerCall.put("driver", "my.driver");
		innerCall.put("eventKey", "my.event");
		TranslationHandler translator = mock(TranslationHandler.class);
		when(translator.decode("my.msg", "my.cell")).thenReturn(innerCall.toString());
		when(translator.encode(any(String.class), eq("my.cell"))).thenReturn("my.return");
		when(securityManager.getTranslationHandler("abacate")).thenReturn(translator);
		when(deviceManager.retrieveDevice(null, null)).thenReturn(new UpDevice("my.cell"));
		
		NetworkDevice caller = mock(NetworkDevice.class);
		assertNull(engine.handleIncomingMessage(call.toString(), caller));
		
		ArgumentCaptor<Notify> eventCatcher = ArgumentCaptor.forClass(Notify.class);
		verify(eventHandler).handleNofify(eventCatcher.capture(), any(UpDevice.class));
		
		assertEquals("my.event",eventCatcher.getValue().getEventKey());
	}
	
	@Test public void handleIncomingMessage_AnEncapsulatedMessageNeverGenerateAnReturnInCaseOfError() throws Exception{
		JSONObject call = new JSONObject();
		call.put("type", "ENCAPSULATED_MESSAGE");
		call.put("innerMessage", "my.msg");
		call.put("securityType", "abacate");
		
		JSONObject innerCall = new JSONObject();
		innerCall.put("type", "BUGGEDINNERMESSAGE");
		innerCall.put("driver", "my.driver");
		innerCall.put("eventKey", "my.event");
		
		TranslationHandler translator = mock(TranslationHandler.class);
		
		when(translator.decode("my.msg", "my.cell")).thenReturn(innerCall.toString());
		when(translator.encode(any(String.class), eq("my.cell"))).thenReturn("my.return");
		when(securityManager.getTranslationHandler("abacate")).thenReturn(translator);
		when(deviceManager.retrieveDevice(null, null)).thenThrow(new RuntimeException());
		
		NetworkDevice caller = mock(NetworkDevice.class);
		assertNull(engine.handleIncomingMessage(call.toString(), caller));
	}
	
	// TODO: notifyEvent delegates to messageHandler
//	@Test public void notifyEvent_delegatesToMessageHandles() throws Exception{
//		Notify event = new Notify();
//		UpDevice target = new UpDevice();
//		engine.notifyEvent(event, target);
//		verify(messageHandler).notifyEvent(event, target);
//	}
	// TODO: callService delegates to messageHandler
//	@Test public void callService_delegatesToMessageHandles() throws Exception{
//		ServiceCall call = new ServiceCall();
//		UpDevice target = new UpDevice();
//		ServiceResponse response = new ServiceResponse();
//		when(messageHandler.callService(target, call)).thenReturn(response);
//		assertEquals(response,engine.callService(target, call));
//		verify(messageHandler).callService(target, call);
//	}
}
