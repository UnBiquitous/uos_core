package org.unbiquitous.uos.core.adaptabitilyEngine;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.unbiquitous.uos.core.messageEngine.MessageEngine;
import org.unbiquitous.uos.core.messageEngine.MessageEngineException;
import org.unbiquitous.uos.core.messageEngine.dataType.UpDevice;
import org.unbiquitous.uos.core.messageEngine.messages.Notify;
import org.unbiquitous.uos.core.messageEngine.messages.Call;
import org.unbiquitous.uos.core.messageEngine.messages.Response;

public class EventManagerTest {

	private MessageEngine engine;
	private EventManager manager;
	private UosEventListener listener;
	private ArgumentCaptor<Call> call;

	@Before
	public void setUp() throws MessageEngineException {
		engine = mock(MessageEngine.class);
		when(engine.callService((UpDevice)any(), (Call)any()))
					.thenReturn(new Response());
		manager = new EventManager(engine);
		listener = mock(UosEventListener.class);
		call = ArgumentCaptor.forClass(Call.class);
	}
	
	@Test
	public void registeringDelegatesToMessageEngine() throws Exception{
		UpDevice device = new UpDevice("the_device");
		
		manager.register(listener, device, "driver", "id", "key", null);
		
		verify(engine).callService(eq(device), call.capture());
		assertThat(call.getAllValues()).hasSize(1);
		assertThat(call.getValue().getService()).isEqualTo("registerListener");
	}
	
	@Test
	public void registeringDelegatesToMessageEngineWithParams() throws Exception{
		UpDevice device = new UpDevice("the_device");
		@SuppressWarnings("serial")
		Map<String,Object> parameters = new HashMap<String, Object>(){{
			put("example","this");
		}};
		
		manager.register(listener, device, "driver", "id", "key",parameters);
		
		verify(engine).callService(eq(device), call.capture());
		assertThat(call.getAllValues()).hasSize(1);
		assertThat(call.getValue().getService()).isEqualTo("registerListener");
		assertThat(call.getValue().getDriver()).isEqualTo("driver");
		assertThat(call.getValue().getInstanceId()).isEqualTo("id");
		assertThat(call.getValue().getParameterString("eventKey")).isEqualTo("key");
		assertThat(call.getValue().getParameterString("example")).isEqualTo("this");
	}
	
	@Test(expected=NotifyException.class)
	public void registeringRejectsParameterWithReservedKey() throws Exception{
		UpDevice device = new UpDevice("the_device");
		@SuppressWarnings("serial")
		Map<String,Object> parameters = new HashMap<String, Object>(){{
			put("eventKey","this");
		}};
		
		manager.register(listener, device, "driver", "id", "key",parameters);
	}
	
	@Test
	public void registeringDontDelegatesForNullDevice() throws Exception{
		manager.register(listener, null, "driver", "id", "key", null);
		
		verify(engine,never()).callService((UpDevice)any(),(Call)any());
	}
	
	@Test
	public void notifyDelegatesToMessageEngine() throws Exception{
		UpDevice device = new UpDevice("the_device");
		
		Notify notify = new Notify("a");
		manager.notify(notify, device);
		
		verify(engine).notify(eq(notify),eq(device));
	}
	
	@Test
	public void notifiesToThelistenerWhenDeviceIsNull() throws Exception{
		manager.register(listener, null, "driver", "id", "key", null);
		
		Notify notify = new Notify("key","driver","id");
		manager.notify(notify, null);
		
		verify(listener).handleEvent(eq(notify));
	}
	
	//TODO: test other combinations of driver/id/key

	@Test
	public void unregisteringDelegatesToMessageEngine() throws Exception{
		UpDevice device = new UpDevice("the_device");
		
		manager.register(listener, device, "driver", "id", "key", null);
		manager.unregisterForEvent(listener, device, "driver", "id", "key");
		
		verify(engine,times(2)).callService(eq(device), call.capture());
		assertThat(call.getAllValues()).hasSize(2);
		assertThat(call.getAllValues().get(1).getService()).isEqualTo("unregisterListener");
	}
	
	@Test
	public void stopNotifyingToThelistenerWhenDeviceIsNull() throws Exception{
		manager.register(listener, null, "driver", "id", "key", null);
		manager.unregisterForEvent(listener, null, "driver", "id", "key");
		
		Notify notify = new Notify("key","driver","id");
		manager.notify(notify, null);
		
		verify(listener,never()).handleEvent((Notify)any());
	}
	
	@Test
	public void dontFailWehnUnregisteringWithoutRegistering() throws Exception{
		manager.unregisterForEvent(	listener, new UpDevice("the_device"), 
									"driver", "id", "key");
	}
	
}
