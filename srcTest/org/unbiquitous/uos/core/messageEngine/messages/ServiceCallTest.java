package org.unbiquitous.uos.core.messageEngine.messages;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;

import org.junit.Test;
import org.unbiquitous.json.JSONException;
import org.unbiquitous.json.JSONObject;
import org.unbiquitous.uos.core.messageEngine.messages.Call.ServiceType;

public class ServiceCallTest {
	
	@Test
	public void equalsNull() {
		assertFalse(new Call().equals(null));
	}
	
	@Test public void notEquals(){
		assertFalse(new Call().equals("somthing"));
	}
	
	@Test public void notEqualsToOtherThing(){
		assertFalse(new Call("d","s").equals(new Call("dd","s")));
		assertFalse(new Call("d","s").equals(new Call("d","ss")));
	}
	
	@Test public void equalsWithEmpty(){
		assertTrue(new Call().equals(new Call()));
	}
	
	@Test public void notEqualsWithNullName(){
		assertFalse(new Call().equals(new Call("d","s")));
	}
	
	@Test public void equalsWithDriverAndService(){
		assertTrue(new Call("d","s").equals(new Call("d","s")));
	}
	
	@Test public void notEqualsWithDifferentParameter(){
		Call call1 = new Call("d","s");
		call1.addParameter("a", 1);
		Call call2 = new Call("d","s");
		call2.addParameter("a", 2);
		Call call3 = new Call("d","s");
		call2.addParameter("b", 1);
		
		assertFalse(call1.equals(call2));
		assertFalse(call1.equals(call3));
	}

	@Test public void equalsWithEqualsParameters(){
		Call call1 = new Call("d","s");
		call1.addParameter("a", 1);
		Call call2 = new Call("d","s");
		call2.addParameter("a", 1);
		assertTrue(call1.equals(call2));
	}

	@Test public void notEqualsWithDifferentInstanceId(){
		Call call1 = new Call("d","s");
		call1.setInstanceId("id1");
		Call call2 = new Call("d","s");
		call2.setInstanceId("id2");
		
		assertFalse(call1.equals(call2));
	}
	
	@Test public void equalsWithSameInstanceId(){
		Call call1 = new Call("d","s");
		call1.setInstanceId("id1");
		Call call2 = new Call("d","s");
		call2.setInstanceId("id1");
		
		assertTrue(call1.equals(call2));
	}
	
	@Test public void notEqualsWithDifferentServiceType(){
		Call call1 = new Call("d","s");
		call1.setServiceType(ServiceType.DISCRETE);
		Call call2 = new Call("d","s");
		call2.setServiceType(ServiceType.STREAM);
		
		assertFalse(call1.equals(call2));
	}
	
	@Test public void equalsWithSameServiceType(){
		Call call1 = new Call("d","s");
		call1.setServiceType(ServiceType.STREAM);
		Call call2 = new Call("d","s");
		call2.setServiceType(ServiceType.STREAM);
		
		assertTrue(call1.equals(call2));
	}
	
	@Test public void notEqualsWithDifferentChannels(){
		Call call1 = new Call("d","s");
		call1.setChannels(3);
		Call call2 = new Call("d","s");
		call2.setChannels(2);
		
		assertFalse(call1.equals(call2));
	}

	@Test public void equalsWithSameChannels(){
		Call call1 = new Call("d","s");
		call1.setChannels(3);
		Call call2 = new Call("d","s");
		call2.setChannels(3);
		
		assertTrue(call1.equals(call2));
	}
	
	@Test public void notEqualsWithDifferentChannelIds(){
		Call call1 = new Call("d","s");
		call1.setChannelIDs(new String[]{"a"});
		Call call2 = new Call("d","s");
		call2.setChannelIDs(new String[]{"b"});
		
		assertFalse(call1.equals(call2));
	}
	
	@Test public void equalsWithSameChannelIds(){
		Call call1 = new Call("d","s");
		call1.setChannelIDs(new String[]{"a"});
		Call call2 = new Call("d","s");
		call2.setChannelIDs(new String[]{"a"});
		
		assertTrue(call1.equals(call2));
	}
	
	@Test public void notEqualsWithDifferentChannelType(){
		Call call1 = new Call("d","s");
		call1.setChannelType("t1");
		Call call2 = new Call("d","s");
		call2.setChannelType("t2");
		
		assertFalse(call1.equals(call2));
	}

	@Test public void equalsWithSameChannelType(){
		Call call1 = new Call("d","s");
		call1.setChannelType("t1");
		Call call2 = new Call("d","s");
		call2.setChannelType("t1");
		
		assertTrue(call1.equals(call2));
	}
	
	@Test public void notEqualsWithDifferentSecurityType(){
		Call call1 = new Call("d","s");
		call1.setSecurityType("s1");
		Call call2 = new Call("d","s");
		call2.setSecurityType("s2");
		
		assertFalse(call1.equals(call2));
	}

	@Test public void equalsWithSameSecurityType(){
		Call call1 = new Call("d","s");
		call1.setSecurityType("s1");
		Call call2 = new Call("d","s");
		call2.setSecurityType("s1");
		
		assertTrue(call1.equals(call2));
	}
	
	@Test public void hash(){
		Call d1 = new Call("d","s");
		Call d2 = new Call("d","s");
		Call d3 = new Call("d","ss");
		Call d4 = new Call("dd","s");
		assertThat(d2.hashCode()).isEqualTo(d1.hashCode());
		assertThat(d3.hashCode()).isNotEqualTo(d1.hashCode());
		assertThat(d4.hashCode()).isNotEqualTo(d1.hashCode());
	}
	
	@Test public void toJSON() throws JSONException{
		assertThat(dummyServiceCall().toJSON().toMap())
					    .isEqualTo(dummyJSON().toMap());
	}

	@Test public void toJSONChannelIds() throws JSONException{
		Call call = new Call("d","s");
		
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
		assertThat(new Call().toJSON().toMap())
					.isEqualTo(defaultJson.toMap());
	}
	
	@Test public void fromJSON() throws JSONException{
		assertThat(Call.fromJSON(new JSONObject(dummyJSON().toString())))
					    .isEqualTo(dummyServiceCall());
	}
	
	@Test public void fromJSONWithChannelIDs() throws JSONException{
		JSONObject json = new JSONObject(){{ 
					put("channelIDs",new String[]{"123","456"});
				}};
		
		assertThat(Call.fromJSON(new JSONObject(json.toString())).getChannelIDs())
					    .containsOnly("123","456");
	}
	
	@Test public void fromJSONEmpty() throws JSONException{
		assertThat(Call.fromJSON(new JSONObject()))
					    .isEqualTo(new Call());
	}
	
	private Call dummyServiceCall() {
		Call call = new Call("d","s");
		
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
