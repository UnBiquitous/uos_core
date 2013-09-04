package org.unbiquitous.uos.core.messageEngine.messages;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;

import org.junit.Test;
import org.unbiquitous.json.JSONException;
import org.unbiquitous.json.JSONObject;
import org.unbiquitous.uos.core.messageEngine.messages.ServiceCall.ServiceType;

public class ServiceCallTest {
	@Test
	public void equalsNull() {
		assertFalse(new ServiceCall().equals(null));
	}
	
	@Test public void notEquals(){
		assertFalse(new ServiceCall().equals("somthing"));
	}
	
	@Test public void notEqualsToOtherThing(){
		assertFalse(new ServiceCall("d","s").equals(new ServiceCall("dd","s")));
		assertFalse(new ServiceCall("d","s").equals(new ServiceCall("d","ss")));
	}
	
	@Test public void equalsWithEmpty(){
		assertTrue(new ServiceCall().equals(new ServiceCall()));
	}
	
	@Test public void notEqualsWithNullName(){
		assertFalse(new ServiceCall().equals(new ServiceCall("d","s")));
	}
	
	@Test public void equalsWithDriverAndService(){
		assertTrue(new ServiceCall("d","s").equals(new ServiceCall("d","s")));
	}
	
	@Test public void notEqualsWithDifferentParameter(){
		ServiceCall call1 = new ServiceCall("d","s");
		call1.addParameter("a", 1);
		ServiceCall call2 = new ServiceCall("d","s");
		call2.addParameter("a", 2);
		ServiceCall call3 = new ServiceCall("d","s");
		call2.addParameter("b", 1);
		
		assertFalse(call1.equals(call2));
		assertFalse(call1.equals(call3));
	}

	@Test public void equalsWithEqualsParameters(){
		ServiceCall call1 = new ServiceCall("d","s");
		call1.addParameter("a", 1);
		ServiceCall call2 = new ServiceCall("d","s");
		call2.addParameter("a", 1);
		assertTrue(call1.equals(call2));
	}

	@Test public void notEqualsWithDifferentInstanceId(){
		ServiceCall call1 = new ServiceCall("d","s");
		call1.setInstanceId("id1");
		ServiceCall call2 = new ServiceCall("d","s");
		call2.setInstanceId("id2");
		
		assertFalse(call1.equals(call2));
	}
	
	@Test public void equalsWithSameInstanceId(){
		ServiceCall call1 = new ServiceCall("d","s");
		call1.setInstanceId("id1");
		ServiceCall call2 = new ServiceCall("d","s");
		call2.setInstanceId("id1");
		
		assertTrue(call1.equals(call2));
	}
	
	@Test public void notEqualsWithDifferentServiceType(){
		ServiceCall call1 = new ServiceCall("d","s");
		call1.setServiceType(ServiceType.DISCRETE);
		ServiceCall call2 = new ServiceCall("d","s");
		call2.setServiceType(ServiceType.STREAM);
		
		assertFalse(call1.equals(call2));
	}
	
	@Test public void equalsWithSameServiceType(){
		ServiceCall call1 = new ServiceCall("d","s");
		call1.setServiceType(ServiceType.STREAM);
		ServiceCall call2 = new ServiceCall("d","s");
		call2.setServiceType(ServiceType.STREAM);
		
		assertTrue(call1.equals(call2));
	}
	
	@Test public void notEqualsWithDifferentChannels(){
		ServiceCall call1 = new ServiceCall("d","s");
		call1.setChannels(3);
		ServiceCall call2 = new ServiceCall("d","s");
		call2.setChannels(2);
		
		assertFalse(call1.equals(call2));
	}

	@Test public void equalsWithSameChannels(){
		ServiceCall call1 = new ServiceCall("d","s");
		call1.setChannels(3);
		ServiceCall call2 = new ServiceCall("d","s");
		call2.setChannels(3);
		
		assertTrue(call1.equals(call2));
	}
	
	@Test public void notEqualsWithDifferentChannelIds(){
		ServiceCall call1 = new ServiceCall("d","s");
		call1.setChannelIDs(new String[]{"a"});
		ServiceCall call2 = new ServiceCall("d","s");
		call2.setChannelIDs(new String[]{"b"});
		
		assertFalse(call1.equals(call2));
	}
	
	@Test public void equalsWithSameChannelIds(){
		ServiceCall call1 = new ServiceCall("d","s");
		call1.setChannelIDs(new String[]{"a"});
		ServiceCall call2 = new ServiceCall("d","s");
		call2.setChannelIDs(new String[]{"a"});
		
		assertTrue(call1.equals(call2));
	}
	
	@Test public void notEqualsWithDifferentChannelType(){
		ServiceCall call1 = new ServiceCall("d","s");
		call1.setChannelType("t1");
		ServiceCall call2 = new ServiceCall("d","s");
		call2.setChannelType("t2");
		
		assertFalse(call1.equals(call2));
	}

	@Test public void equalsWithSameChannelType(){
		ServiceCall call1 = new ServiceCall("d","s");
		call1.setChannelType("t1");
		ServiceCall call2 = new ServiceCall("d","s");
		call2.setChannelType("t1");
		
		assertTrue(call1.equals(call2));
	}
	
	@Test public void notEqualsWithDifferentSecurityType(){
		ServiceCall call1 = new ServiceCall("d","s");
		call1.setSecurityType("s1");
		ServiceCall call2 = new ServiceCall("d","s");
		call2.setSecurityType("s2");
		
		assertFalse(call1.equals(call2));
	}

	@Test public void equalsWithSameSecurityType(){
		ServiceCall call1 = new ServiceCall("d","s");
		call1.setSecurityType("s1");
		ServiceCall call2 = new ServiceCall("d","s");
		call2.setSecurityType("s1");
		
		assertTrue(call1.equals(call2));
	}
	
	@Test public void hash(){
		ServiceCall d1 = new ServiceCall("d","s");
		ServiceCall d2 = new ServiceCall("d","s");
		ServiceCall d3 = new ServiceCall("d","ss");
		ServiceCall d4 = new ServiceCall("dd","s");
		assertThat(d2.hashCode()).isEqualTo(d1.hashCode());
		assertThat(d3.hashCode()).isNotEqualTo(d1.hashCode());
		assertThat(d4.hashCode()).isNotEqualTo(d1.hashCode());
	}
	
	@Test public void toJSON() throws JSONException{
		assertThat(dummyServiceCall().toJSON().toMap())
					    .isEqualTo(dummyJSON().toMap());
	}

	@Test public void toJSONChannelIds() throws JSONException{
		ServiceCall call = new ServiceCall("d","s");
		
		call.setChannelIDs(new String[]{"123","456"});
		
		assertThat((String[])(call.toJSON().toMap().get("channelIDs")))
			.containsOnly("123","456");
	}
	
	@SuppressWarnings({ "rawtypes", "serial", "unchecked" })
	@Test public void toJSONForEmpty() throws JSONException{
		JSONObject defaultJson = new JSONObject(
				new HashMap() {{ 
					put("type", Message.Type.SERVICE_CALL_REQUEST.name()); 
					
					put("serviceType",ServiceType.DISCRETE.name());
					put("channels",1);
				}}
			);
		assertThat(new ServiceCall().toJSON().toMap())
					.isEqualTo(defaultJson.toMap());
	}
	
	@Test public void fromJSON() throws JSONException{
		assertThat(ServiceCall.fromJSON(new JSONObject(dummyJSON().toString())))
					    .isEqualTo(dummyServiceCall());
	}
	
	@SuppressWarnings({ "rawtypes", "serial", "unchecked" })
	@Test public void fromJSONWithChannelIDs() throws JSONException{
		JSONObject json = new JSONObject(
				new HashMap() {{ 
					put("channelIDs",new String[]{"123","456"});
				}}
			);
		
		assertThat(ServiceCall.fromJSON(new JSONObject(json.toString())).getChannelIDs())
					    .containsOnly("123","456");
	}
	
	@Test public void fromJSONEmpty() throws JSONException{
		assertThat(ServiceCall.fromJSON(new JSONObject()))
					    .isEqualTo(new ServiceCall());
	}
	
	private ServiceCall dummyServiceCall() {
		ServiceCall call = new ServiceCall("d","s");
		
//		call.setType(Message.Type.SERVICE_CALL_REQUEST);
		call.setError("err");
		
		call.addParameter("a", "a1");
		call.addParameter("b", 1);
		call.setInstanceId("id1");
		call.setServiceType(ServiceType.DISCRETE);
		call.setChannels(3);
//		call.setChannelIDs(new String[]{"123","456"});
		call.setChannelType("ct");
		call.setSecurityType("st");
		return call;
	}

	@SuppressWarnings({ "rawtypes", "serial", "unchecked" })
	private JSONObject dummyJSON() {
		JSONObject json = new JSONObject(
				new HashMap() {{ 
					put("type", Message.Type.SERVICE_CALL_REQUEST.name()); 
					put("error", "err");
					
					put("driver", "d"); 
					put("service", "s"); 
					put("parameters", new HashMap() {{ 
						put("a","a1");
						put("b",1);
					}}); 
					put("instanceId","id1");
					put("serviceType",ServiceType.DISCRETE.name());
					put("channels",3);
//					put("channelIDs",new String[]{"123","456"});
					put("channelType","ct");
					put("securityType","st");
				}}
			);
		return json;
	}
	
}
