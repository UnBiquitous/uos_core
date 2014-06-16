package org.unbiquitous.uos.core.messageEngine;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.ListResourceBundle;
import java.util.ResourceBundle;

import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.unbiquitous.json.JSONObject;
import org.unbiquitous.uos.core.AuthenticationHandler;
import org.unbiquitous.uos.core.InitialProperties;
import org.unbiquitous.uos.core.SecurityManager;
import org.unbiquitous.uos.core.connectivity.ConnectivityManager;
import org.unbiquitous.uos.core.messageEngine.dataType.UpDevice;
import org.unbiquitous.uos.core.messageEngine.dataType.UpNetworkInterface;
import org.unbiquitous.uos.core.messageEngine.messages.Call;
import org.unbiquitous.uos.core.messageEngine.messages.Capsule;
import org.unbiquitous.uos.core.messageEngine.messages.Notify;
import org.unbiquitous.uos.core.messageEngine.messages.Response;
import org.unbiquitous.uos.core.network.connectionManager.ConnectionManagerControlCenter;
import org.unbiquitous.uos.core.network.model.connection.ClientConnection;


public class MessageHandlerTest {

	private MessageHandler handler ;
	private ConnectivityManager connManager;
	private ConnectionManagerControlCenter controlCenter;
	private SecurityManager securityManager;
	
	@Before public void setUp() throws Exception{
		connManager =  mock(ConnectivityManager.class);
		controlCenter = mock(ConnectionManagerControlCenter.class);
		when(controlCenter.sendControlMessage(anyString(), anyBoolean(), anyString(), anyString())).thenCallRealMethod();
		doAnswer(new Answer<Void>() {
			public Void answer(InvocationOnMock invocation) throws Throwable {
				invocation.callRealMethod();
				return null;
			}
		}).when(controlCenter).create(any(InitialProperties.class));
		securityManager = mock(SecurityManager.class);
		ResourceBundle bundle = new ListResourceBundle() {
			protected Object[][] getContents() {
				return new Object[][] {
		             {"ubiquitos.message.response.timeout", "1"},
		             {"ubiquitos.message.response.retry", "1"},
		        };
			}
		};
		InitialProperties props = new InitialProperties(bundle);
		controlCenter.create(props);
		handler = new MessageHandler(props,controlCenter, securityManager, connManager);
	}
	
	// callService
		//Negative Cases
	@Test(expected=IllegalArgumentException.class) public void callService_ShouldRejectANullCall() throws Exception{
		handler.callService(mock(UpDevice.class),null);
	}
	@Test(expected=IllegalArgumentException.class) public void callService_ShouldRejectANullDevice() throws Exception{
		handler.callService(null,new Call("d", "s"));
	}
	@Test(expected=IllegalArgumentException.class) public void callService_ShouldRejectNullDriverAndServiceOnCall() throws Exception{
		handler.callService(mock(UpDevice.class),new Call());
	}
	@Test(expected=IllegalArgumentException.class) public void callService_ShouldRejectNullDriverOnCall() throws Exception{
		handler.callService(mock(UpDevice.class),new Call(null,"s"));
	}
	@Test(expected=IllegalArgumentException.class) public void callService_ShouldRejectNullServiceOnCall() throws Exception{
		handler.callService(mock(UpDevice.class),new Call("d",null));
	}
	@Test(expected=IllegalArgumentException.class) public void callService_ShouldRejectEmptyDriverAndServiceOnCall() throws Exception{
		handler.callService(mock(UpDevice.class),new Call("",""));
	}
	@Test(expected=IllegalArgumentException.class) public void callService_ShouldRejectEmptyDriverOnCall() throws Exception{
		handler.callService(mock(UpDevice.class),new Call("","s"));
	}
	@Test(expected=IllegalArgumentException.class) public void callService_ShouldRejectEmptyServiceOnCall() throws Exception{
		handler.callService(mock(UpDevice.class),new Call("d",""));
	}
	
	// CallServiceVariables
	private class SnapshotScenario{
		private UpDevice target;
		private Call snapshot;
		private UpNetworkInterface wifi;
		private PipedOutputStream wifiInterfaceIn;
		private PipedInputStream wifiInterfaceOut;
	
		SnapshotScenario() throws Exception{
			//Create Parameters for simulation of a snapshot service call
			target = new UpDevice("my.cell");
			snapshot = new Call("camera", "snapshot");
			snapshot.addParameter("resolution", "800x600");
			wifi = new UpNetworkInterface("Etherenet:TCP", "127.0.0.66");
			
			//Prepare Streams so we could capture what is going to the networklayer
			wifiInterfaceIn = new PipedOutputStream();
			wifiInterfaceOut = new PipedInputStream();
			
			DataInputStream in = new DataInputStream(new PipedInputStream(wifiInterfaceIn));
			DataOutputStream out = new DataOutputStream(new PipedOutputStream(wifiInterfaceOut));
			ClientConnection conn = mock(ClientConnection.class);
			when(conn.getDataInputStream()).thenReturn(in);
			when(conn.getDataOutputStream()).thenReturn(out);
			
			when(connManager.getAppropriateInterface(target, snapshot)).thenReturn(wifi);
			when(connManager.getAppropriateInterface(target)).thenReturn(wifi);
			when(controlCenter.openActiveConnection(wifi.getNetworkAddress(), wifi.getNetType()))
				.thenReturn(conn);
		}
		
		private String grabSentString() throws IOException {
			StringBuilder callBuilder = new StringBuilder();
			for (int i = wifiInterfaceOut.read() ; (char) i != '\n'; i = wifiInterfaceOut.read()){
				callBuilder.append((char) i);
			}
			String returnedString = callBuilder.toString();
			return returnedString;
		}
	}
	
	@Test public void callService_aSimpleCallMustBeSentAndItsResponseRetrieved() throws Exception{
		SnapshotScenario scenario = new SnapshotScenario();
		//Simulate a response
		for (char c : "{type:\"SERVICE_CALL_RESPONSE\", responseData:{pic:\"NotAvailable\"}}\n".toCharArray()){
			scenario.wifiInterfaceIn.write(c);
		}
		
		Response response = handler
								.callService(scenario.target, scenario.snapshot);
		
		assertEquals("Should return the same response that was stimulated.",
						"NotAvailable",response.getResponseData("pic"));
		assertThat(Call.fromJSON(new JSONObject(scenario.grabSentString())))
			.isEqualTo(scenario.snapshot);
	}

	@Test public void callService_aSimpleCallMustBeSentButWhenNoResponseIsRetrievedNullShouldBeReturned() throws Exception{
		SnapshotScenario scenario = new SnapshotScenario();
		
		assertNull("No response must be returned.", 
				handler.callService(scenario.target, scenario.snapshot));
		assertThat(Call.fromJSON(new JSONObject(scenario.grabSentString())))
				.isEqualTo(scenario.snapshot);
	}
	
	@Test public void callService_aSimpleCallMustBeSentButWhenNoConnectionIsPossibleNullShouldBeReturned() throws Exception{
		SnapshotScenario scenario = new SnapshotScenario();
		when(controlCenter.openActiveConnection(
				scenario.wifi.getNetworkAddress(), 
				scenario.wifi.getNetType())).thenReturn(null);
		
		assertNull("No response must be returned.", handler.callService(scenario.target, scenario.snapshot));
	}
	
	@Test public void callService_aSimpleCallMustBeSentButWhenNoValidConnectionIsReturnedNullShouldBeReturned() throws Exception{
		SnapshotScenario scenario = new SnapshotScenario();
		ClientConnection mock = mock(ClientConnection.class);
		when(controlCenter.openActiveConnection(
				scenario.wifi.getNetworkAddress(), 
				scenario.wifi.getNetType())).thenReturn(mock);
		
		assertNull("No response must be returned.", handler.callService(scenario.target, scenario.snapshot));
	}
	
	@Test public void callService_ACallWithSecurityTypeMustResultOnAnEncapsulatedMessage() throws Exception{
		SnapshotScenario scenario = new SnapshotScenario();
		scenario.snapshot.setSecurityType("Pig-Latin");
		
		AuthenticationHandler auth = mock(AuthenticationHandler.class); 
		when(securityManager.getAuthenticationHandler("Pig-Latin")).thenReturn(auth);
		TranslationHandler translator = mock(TranslationHandler.class);
		when(securityManager.getTranslationHandler("Pig-Latin")).thenReturn(translator);
		when(translator.encode(any(String.class), eq(scenario.target.getName())))
				.thenReturn("Encoded Output Message");
		
		//Simulate a response
		for (char c : "{type:\"ENCAPSULATED_MESSAGE\", innerMessage:\"Encoded Input Message\", securityType:\"Pig-Latin\"}\n".toCharArray()){
			scenario.wifiInterfaceIn.write(c);
		}
		
		when(translator.decode(eq("Encoded Input Message"), eq(scenario.target.getName())))
				.thenReturn("{type:\"SERVICE_CALL_RESPONSE\", responseData:{pic:\"StillNotAvailable\"}}");
		
		Response response = handler.callService(scenario.target, scenario.snapshot);
		
		assertEquals("StillNotAvailable",response.getResponseData("pic"));
		
		//Should call for authentication before proceed with the encapsulation
		verify(auth).authenticate(scenario.target, handler);
		
		Capsule sentMessage = Capsule.fromJSON(new JSONObject(scenario.grabSentString()));
		assertEquals("The security type should be unchanged.","Pig-Latin",sentMessage.getSecurityType());
		assertEquals("Should have sent the encoded message.","Encoded Output Message",sentMessage.getInnerMessage());
	}
	
	@Test(expected=MessageEngineException.class) public void callService_ACallWithProblemsThrowsAnException() throws Exception{
		SnapshotScenario scenario = new SnapshotScenario();
		
		when(controlCenter.openActiveConnection(any(String.class), any(String.class)))
													.thenThrow(new RuntimeException());
		handler.callService(scenario.target, scenario.snapshot);
	}
	
	@Test(expected=MessageEngineException.class) public void callService_ACallWithSecurityTypeFailsWihtoutProperAuthenticator() throws Exception{
		SnapshotScenario scenario = new SnapshotScenario();
		scenario.snapshot.setSecurityType("Pig-Latin");
		
		AuthenticationHandler auth = mock(AuthenticationHandler.class); 
		when(securityManager.getAuthenticationHandler("Not-Pig-Latin")).thenReturn(auth);
		TranslationHandler translator = mock(TranslationHandler.class);
		when(securityManager.getTranslationHandler("Pig-Latin")).thenReturn(translator);
		
		handler.callService(scenario.target, scenario.snapshot);
	}
	
	@Test(expected=MessageEngineException.class) public void callService_ACallWithSecurityTypeFailsWihtoutProperTranslator() throws Exception{
		SnapshotScenario scenario = new SnapshotScenario();
		scenario.snapshot.setSecurityType("Pig-Latin");
		
		AuthenticationHandler auth = mock(AuthenticationHandler.class); 
		when(securityManager.getAuthenticationHandler("Pig-Latin")).thenReturn(auth);
		TranslationHandler translator = mock(TranslationHandler.class);
		when(securityManager.getTranslationHandler("NotPig-Latin")).thenReturn(translator);
		
		handler.callService(scenario.target, scenario.snapshot);
	}
	
	// notifyEvent
		// Negative cases
	@Test(expected=IllegalArgumentException.class) public void notifyEvent_ShouldRejectANullEvent() throws Exception{
		handler.notifyEvent(null,mock(UpDevice.class));
	}
	@Test(expected=IllegalArgumentException.class) public void notifyEvent_ShouldRejectANullDevice() throws Exception{
		handler.notifyEvent(new Notify("d", "s"),null);
	}
	@Test(expected=IllegalArgumentException.class) public void notifyEvent_ShouldRejectNullDriverAndKeyOnEvent() throws Exception{
		handler.notifyEvent(new Notify(),mock(UpDevice.class));
	}
	@Test(expected=IllegalArgumentException.class) public void notifyEvent_ShouldRejectNullDriverOnEvent() throws Exception{
		handler.notifyEvent(new Notify(null,"s"),mock(UpDevice.class));
	}
	@Test(expected=IllegalArgumentException.class) public void notifyEvent_ShouldRejectNullKeyOnEvent() throws Exception{
		handler.notifyEvent(new Notify("d",null),mock(UpDevice.class));
	}
	@Test(expected=IllegalArgumentException.class) public void notifyEvent_ShouldRejectEmptyDriverAndKeyOnEvent() throws Exception{
		handler.notifyEvent(new Notify("",""),mock(UpDevice.class));
	}
	@Test(expected=IllegalArgumentException.class) public void notifyEventShouldRejectEmptyDriverOnEvent() throws Exception{
		handler.notifyEvent(new Notify("","s"),mock(UpDevice.class));
	}
	@Test(expected=IllegalArgumentException.class) public void notifyEvent_ShouldRejectEmptyKeyOnEvent() throws Exception{
		handler.notifyEvent(new Notify("d",""),mock(UpDevice.class));
	}
	
	private class UserEnteredScenario{
		private UpDevice target;
		private Notify userEntered;
		private UpNetworkInterface wifi;
		private PipedOutputStream wifiInterfaceIn;
		private PipedInputStream wifiInterfaceOut;
	
		UserEnteredScenario() throws Exception{
			//Create Parameters for simulation of a snapshot service call
			target = new UpDevice("my.cell");
			userEntered = new Notify("User", "entered");
			userEntered.addParameter("user-id", "bruce.wayne");
			wifi = new UpNetworkInterface("Etherenet:TCP", "127.0.0.66");
			
			//Prepare Streams so we could capture what is going to the networklayer
			wifiInterfaceIn = new PipedOutputStream();
			wifiInterfaceOut = new PipedInputStream();
			
			DataInputStream in = new DataInputStream(new PipedInputStream(wifiInterfaceIn));
			DataOutputStream out = new DataOutputStream(new PipedOutputStream(wifiInterfaceOut));
			ClientConnection conn = mock(ClientConnection.class);
			when(conn.getDataInputStream()).thenReturn(in);
			when(conn.getDataOutputStream()).thenReturn(out);
			
			when(connManager.getAppropriateInterface(target)).thenReturn(wifi);
			when(controlCenter.openActiveConnection(wifi.getNetworkAddress(), wifi.getNetType()))
				.thenReturn(conn);
		}
		
		private String grabSentString() throws IOException {
			StringBuilder callBuilder = new StringBuilder();
			for (int i = wifiInterfaceOut.read() ; (char) i != '\n'; i = wifiInterfaceOut.read()){
				callBuilder.append((char) i);
			}
			String returnedString = callBuilder.toString();
			return returnedString;
		}
	}
	
	@Test public void notifyEvent_shouldSendForASimpleEvent() throws Exception{
		UserEnteredScenario scenario = new UserEnteredScenario();
		
		handler.notifyEvent(scenario.userEntered,scenario.target);
		
		assertEquals("The JSON sent should be compatible with the snapshot created.",
				scenario.userEntered,
				Notify.fromJSON(new JSONObject(scenario.grabSentString())));
		
	}
	
	@Test(expected=MessageEngineException.class) public void notifyEvent_ACallWithProblemsThrowsAnException() throws Exception{
		UserEnteredScenario scenario = new UserEnteredScenario();
		
		when(controlCenter.openActiveConnection(any(String.class), any(String.class)))
													.thenThrow(new RuntimeException());
		handler.notifyEvent(scenario.userEntered, scenario.target);
	}

	//TODO: Do Events have the capability to use security channels? If so create tests based on callService
		
}
