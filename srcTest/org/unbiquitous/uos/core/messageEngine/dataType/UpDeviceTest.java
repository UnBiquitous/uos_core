package org.unbiquitous.uos.core.messageEngine.dataType;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.unbiquitous.json.JSONArray;
import org.unbiquitous.json.JSONException;
import org.unbiquitous.json.JSONObject;

public class UpDeviceTest {

	@Test
	public void equalsNull() {
		assertFalse(new UpDevice(null).equals(null));
	}
	
	@Test public void notEquals(){
		assertFalse(new UpDevice("d").equals("somthing"));
	}
	
	@Test public void notEqualsToOtherThing(){
		assertFalse(new UpDevice("d").equals(new UpDevice("otherD")));
	}
	
	@Test public void notEqualsWithNullName(){
		assertFalse(new UpDevice(null).equals(new UpDevice("name")));
	}
	
	@Test public void notEqualsWithDifferentNetworkInterfaces(){
		UpDevice device1 = new UpDevice("d");
		device1.addNetworkInterface("addr1", "type1");
		UpDevice device2 = new UpDevice("d");
		device2.addNetworkInterface("addr2", "type1");
		UpDevice device3 = new UpDevice("d");
		device3.addNetworkInterface("addr1", "type2");
		assertFalse(device1.equals(device2));
		assertFalse(device1.equals(device3));
	}
	
	@Test public void notEqualsWithDifferentMetaInformation(){
		UpDevice device1 = new UpDevice("d");
		UpDevice device2 = new UpDevice("d");
		device1.addProperty("p", "v");
		assertFalse(device1.equals(device2));
	}

	@Test public void equalsWithBothWithNullName(){
		assertTrue(new UpDevice(null).equals(new UpDevice(null)));
	}

	@Test public void equalsWithNameEquals(){
		assertTrue(new UpDevice("d").equals(new UpDevice("d")));
	}
	
	@Test public void equalsWithEqualsNetworkInterfaces(){
		UpDevice device1 = new UpDevice("d");
		device1.addNetworkInterface("addr1", "type1");
		UpDevice device2 = new UpDevice("d");
		device2.addNetworkInterface("addr1", "type1");
		assertTrue(device1.equals(device2));
	}
	
	@Test public void equalsWithEqualsMetas(){
		UpDevice device1 = new UpDevice("d");
		device1.addProperty("p", "v");
		UpDevice device2 = new UpDevice("d");
		device1.addProperty("p", "v");
		assertFalse(device1.equals(device2));
	}
	
	@Test public void hash(){
		UpDevice d1 = new UpDevice("device");
		UpDevice d2 = new UpDevice("device");
		UpDevice d3 = new UpDevice("notdevice");
		assertThat(d2.hashCode()).isEqualTo(d1.hashCode());
		assertThat(d3.hashCode()).isNotEqualTo(d1.hashCode());
	}
	
	@Test public void toJson() throws JSONException{
		UpDevice device = dummyDevice();
		JSONObject json = dummyJSONDevice();
		
		assertThat(device.toJSON().toMap())
			.isEqualTo(json.toMap());
	}
	
	@Test public void toJsonFromEmpty() throws JSONException{
		assertThat(new UpDevice().toJSON().toMap())
			.isEqualTo(new JSONObject().toMap());
	}
	
	@Test public void fromJSON() throws JSONException{
		assertThat(UpDevice.fromJSON(dummyJSONDevice()))
			.isEqualTo(dummyDevice());
	}
	
	@Test public void fromJSONForEmpty() throws JSONException{
		assertThat(UpDevice.fromJSON(new JSONObject()))
			.isEqualTo(new UpDevice());
	}

	private JSONObject dummyJSONDevice() throws JSONException {
		JSONObject json = new JSONObject();
		json.put("name", "d");
		
		JSONArray networks = new JSONArray();
		JSONObject ni = new JSONObject();
		ni.put("networkAddress", "addr");
		ni.put("netType", "type");
		networks.put(ni);
		json.put("networks", networks);
		
		JSONObject meta = new JSONObject();
		meta.put("p", "v");
		json.put("meta",meta);
		return json;
	}

	private UpDevice dummyDevice() {
		UpDevice device = new UpDevice("d");
		
		device.addNetworkInterface("addr", "type");
		device.addProperty("p", "v");
		return device;
	}
	
}
