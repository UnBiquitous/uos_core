package org.unbiquitous.uos.core.messageEngine.messages;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.unbiquitous.uos.core.messageEngine.messages.Call.ServiceType;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class ServiceCallTest {
	private static final ObjectMapper mapper = new ObjectMapper();

	@Test
	public void equalsNull() {
		assertFalse(new Call().equals(null));
	}

	@Test
	public void notEquals() {
		assertFalse(new Call().equals("somthing"));
	}

	@Test
	public void notEqualsToOtherThing() {
		assertFalse(new Call("d", "s").equals(new Call("dd", "s")));
		assertFalse(new Call("d", "s").equals(new Call("d", "ss")));
	}

	@Test
	public void equalsWithEmpty() {
		assertTrue(new Call().equals(new Call()));
	}

	@Test
	public void notEqualsWithNullName() {
		assertFalse(new Call().equals(new Call("d", "s")));
	}

	@Test
	public void equalsWithDriverAndService() {
		assertTrue(new Call("d", "s").equals(new Call("d", "s")));
	}

	@Test
	public void notEqualsWithDifferentParameter() {
		Call call1 = new Call("d", "s");
		call1.addParameter("a", 1);
		Call call2 = new Call("d", "s");
		call2.addParameter("a", 2);
		Call call3 = new Call("d", "s");
		call2.addParameter("b", 1);

		assertFalse(call1.equals(call2));
		assertFalse(call1.equals(call3));
	}

	@Test
	public void equalsWithEqualsParameters() {
		Call call1 = new Call("d", "s");
		call1.addParameter("a", 1);
		Call call2 = new Call("d", "s");
		call2.addParameter("a", 1);
		assertTrue(call1.equals(call2));
	}

	@Test
	public void notEqualsWithDifferentInstanceId() {
		Call call1 = new Call("d", "s");
		call1.setInstanceId("id1");
		Call call2 = new Call("d", "s");
		call2.setInstanceId("id2");

		assertFalse(call1.equals(call2));
	}

	@Test
	public void equalsWithSameInstanceId() {
		Call call1 = new Call("d", "s");
		call1.setInstanceId("id1");
		Call call2 = new Call("d", "s");
		call2.setInstanceId("id1");

		assertTrue(call1.equals(call2));
	}

	@Test
	public void notEqualsWithDifferentServiceType() {
		Call call1 = new Call("d", "s");
		call1.setServiceType(ServiceType.DISCRETE);
		Call call2 = new Call("d", "s");
		call2.setServiceType(ServiceType.STREAM);

		assertFalse(call1.equals(call2));
	}

	@Test
	public void equalsWithSameServiceType() {
		Call call1 = new Call("d", "s");
		call1.setServiceType(ServiceType.STREAM);
		Call call2 = new Call("d", "s");
		call2.setServiceType(ServiceType.STREAM);

		assertTrue(call1.equals(call2));
	}

	@Test
	public void notEqualsWithDifferentChannels() {
		Call call1 = new Call("d", "s");
		call1.setChannels(3);
		Call call2 = new Call("d", "s");
		call2.setChannels(2);

		assertFalse(call1.equals(call2));
	}

	@Test
	public void equalsWithSameChannels() {
		Call call1 = new Call("d", "s");
		call1.setChannels(3);
		Call call2 = new Call("d", "s");
		call2.setChannels(3);

		assertTrue(call1.equals(call2));
	}

	@Test
	public void notEqualsWithDifferentChannelIds() {
		Call call1 = new Call("d", "s");
		call1.setChannelIDs(new String[] { "a" });
		Call call2 = new Call("d", "s");
		call2.setChannelIDs(new String[] { "b" });

		assertFalse(call1.equals(call2));
	}

	@Test
	public void equalsWithSameChannelIds() {
		Call call1 = new Call("d", "s");
		call1.setChannelIDs(new String[] { "a" });
		Call call2 = new Call("d", "s");
		call2.setChannelIDs(new String[] { "a" });

		assertTrue(call1.equals(call2));
	}

	@Test
	public void notEqualsWithDifferentChannelType() {
		Call call1 = new Call("d", "s");
		call1.setChannelType("t1");
		Call call2 = new Call("d", "s");
		call2.setChannelType("t2");

		assertFalse(call1.equals(call2));
	}

	@Test
	public void equalsWithSameChannelType() {
		Call call1 = new Call("d", "s");
		call1.setChannelType("t1");
		Call call2 = new Call("d", "s");
		call2.setChannelType("t1");

		assertTrue(call1.equals(call2));
	}

	@Test
	public void notEqualsWithDifferentSecurityType() {
		Call call1 = new Call("d", "s");
		call1.setSecurityType("s1");
		Call call2 = new Call("d", "s");
		call2.setSecurityType("s2");

		assertFalse(call1.equals(call2));
	}

	@Test
	public void equalsWithSameSecurityType() {
		Call call1 = new Call("d", "s");
		call1.setSecurityType("s1");
		Call call2 = new Call("d", "s");
		call2.setSecurityType("s1");

		assertTrue(call1.equals(call2));
	}

	@Test
	public void hash() {
		Call d1 = new Call("d", "s");
		Call d2 = new Call("d", "s");
		Call d3 = new Call("d", "ss");
		Call d4 = new Call("dd", "s");
		assertThat(d2.hashCode()).isEqualTo(d1.hashCode());
		assertThat(d3.hashCode()).isNotEqualTo(d1.hashCode());
		assertThat(d4.hashCode()).isNotEqualTo(d1.hashCode());
	}

	@Test
	public void toJSON() {
		assertThat(mapper.valueToTree(dummyServiceCall())).isEqualTo(dummyJSON());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void toJSONChannelIds() throws IOException {
		Call call = new Call("d", "s");

		call.setChannelIDs(new String[] { "123", "456" });

		List<String> ids = (List<String>) mapper.treeToValue(mapper.valueToTree(call), Map.class).get("channelIDs");
		assertThat(ids).containsOnly("123", "456");
	}

	@Test
	public void toJSONForEmpty() {
		ObjectNode defaultJson = mapper.createObjectNode();
		defaultJson.put("type", Message.Type.SERVICE_CALL_REQUEST.name());
		defaultJson.put("serviceType", ServiceType.DISCRETE.name());
		defaultJson.put("channels", 1);
		assertThat(mapper.valueToTree(new Call())).isEqualTo(defaultJson);
	}

	@Test
	public void fromJSON() throws IOException {
		assertThat(mapper.readValue(dummyJSON().toString(), Call.class)).isEqualTo(dummyServiceCall());
	}

	@Test
	public void fromJSONWithChannelIDs() throws IOException {
		ObjectNode json = mapper.createObjectNode();
		json.set("channelIDs", mapper.valueToTree(new String[] { "123", "456" }));
		assertThat(mapper.readValue(json.toString(), Call.class).getChannelIDs()).containsOnly("123", "456");
	}

	@Test
	public void fromJSONEmpty() throws IOException {
		assertThat(mapper.treeToValue(mapper.createObjectNode(), Call.class)).isEqualTo(new Call());
	}

	private Call dummyServiceCall() {
		Call call = new Call("d", "s");

		// call.setType(Message.Type.SERVICE_CALL_REQUEST);
		call.setError("err");

		call.addParameter("a", "a1");
		call.addParameter("b", 1);
		call.setInstanceId("id1");
		call.setServiceType(ServiceType.DISCRETE);
		call.setChannels(3);
		// call.setChannelIDs(new String[]{"123","456"});
		call.setChannelType("ct");
		call.setSecurityType("st");
		return call;
	}

	private ObjectNode dummyJSON() {
		ObjectNode json = mapper.createObjectNode();
		json.put("type", Message.Type.SERVICE_CALL_REQUEST.name());
		json.put("error", "err");
		json.put("driver", "d");
		json.put("service", "s");
		ObjectNode params = json.putObject("parameters");
		params.put("a", "a1");
		params.put("b", 1);
		json.put("instanceId", "id1");
		json.put("serviceType", ServiceType.DISCRETE.name());
		json.put("channels", 3);
		// put("channelIDs",new String[]{"123","456"});
		json.put("channelType", "ct");
		json.put("securityType", "st");
		return json;
	}
}
