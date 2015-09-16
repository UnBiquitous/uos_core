package org.unbiquitous.uos.core.messageEngine.dataType;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class UpDeviceTest {
	private final JsonNodeFactory factory = JsonNodeFactory.instance;
	private final ObjectMapper mapper = new ObjectMapper();

	@Test
	public void equalsNull() {
		assertFalse(new UpDevice(null).equals(null));
	}

	@Test
	public void notEquals() {
		assertFalse(new UpDevice("d").equals("somthing"));
	}

	@Test
	public void notEqualsToOtherThing() {
		assertFalse(new UpDevice("d").equals(new UpDevice("otherD")));
	}

	@Test
	public void notEqualsWithNullName() {
		assertFalse(new UpDevice(null).equals(new UpDevice("name")));
	}

	@Test
	public void notEqualsWithDifferentNetworkInterfaces() {
		UpDevice device1 = new UpDevice("d");
		device1.addNetworkInterface("addr1", "type1");
		UpDevice device2 = new UpDevice("d");
		device2.addNetworkInterface("addr2", "type1");
		UpDevice device3 = new UpDevice("d");
		device3.addNetworkInterface("addr1", "type2");
		assertFalse(device1.equals(device2));
		assertFalse(device1.equals(device3));
	}

	@Test
	public void notEqualsWithDifferentMetaInformation() {
		UpDevice device1 = new UpDevice("d");
		UpDevice device2 = new UpDevice("d");
		device1.addProperty("p", "v");
		assertFalse(device1.equals(device2));
	}

	@Test
	public void equalsWithBothWithNullName() {
		assertTrue(new UpDevice(null).equals(new UpDevice(null)));
	}

	@Test
	public void equalsWithNameEquals() {
		assertTrue(new UpDevice("d").equals(new UpDevice("d")));
	}

	@Test
	public void equalsWithEqualsNetworkInterfaces() {
		UpDevice device1 = new UpDevice("d");
		device1.addNetworkInterface("addr1", "type1");
		UpDevice device2 = new UpDevice("d");
		device2.addNetworkInterface("addr1", "type1");
		assertTrue(device1.equals(device2));
	}

	@Test
	public void equalsWithEqualsMetas() {
		UpDevice device1 = new UpDevice("d");
		device1.addProperty("p", "v");
		UpDevice device2 = new UpDevice("d");
		device1.addProperty("p", "v");
		assertFalse(device1.equals(device2));
	}

	@Test
	public void hash() {
		UpDevice d1 = new UpDevice("device");
		UpDevice d2 = new UpDevice("device");
		UpDevice d3 = new UpDevice("notdevice");
		assertThat(d2.hashCode()).isEqualTo(d1.hashCode());
		assertThat(d3.hashCode()).isNotEqualTo(d1.hashCode());
	}

	@Test
	public void toJson() {
		UpDevice device = dummyDevice();
		JsonNode json = dummyJSONDevice();

		assertThat(mapper.valueToTree(device)).isEqualTo(json);
	}

	@Test
	public void toJsonFromEmpty() {
		assertThat(mapper.valueToTree(new UpDevice())).isEqualTo(factory.objectNode());
	}

	@Test
	public void fromJSON() throws IOException {
		assertThat(mapper.treeToValue(dummyJSONDevice(), UpDevice.class)).isEqualTo(dummyDevice());
	}

	@Test
	public void fromJSONString() throws IOException {
		JsonNode dummyJSON = dummyJSONDevice();
		assertThat(mapper.readValue(dummyJSON.toString(), UpDevice.class)).isEqualTo(dummyDevice());
	}

	@Test
	public void fromJSONForEmpty() throws IOException {
		assertThat(mapper.treeToValue(factory.objectNode(), UpDevice.class)).isEqualTo(new UpDevice());
	}

	private JsonNode dummyJSONDevice() {
		ObjectNode json = factory.objectNode();
		json.put("name", "d");

		ArrayNode networks = factory.arrayNode();
		ObjectNode ni = factory.objectNode();
		ni.put("networkAddress", "addr");
		ni.put("netType", "type");
		networks.add(ni);
		json.set("networks", networks);

		ObjectNode meta = json.putObject("meta");
		meta.put("p", "v");

		return json;
	}

	private UpDevice dummyDevice() {
		UpDevice device = new UpDevice("d");

		device.addNetworkInterface("addr", "type");
		device.addProperty("p", "v");
		return device;
	}

}
