package org.unbiquitous.uos.core.messageEngine.dataType;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.unbiquitous.json.JSONArray;
import org.unbiquitous.json.JSONException;
import org.unbiquitous.json.JSONObject;
import org.unbiquitous.uos.core.messageEngine.dataType.UpService.ParameterType;

public class UpDriverTest {

	@Test
	public void equalsNull() {
		assertFalse(new UpDriver(null).equals(null));
	}
	
	@Test public void notEquals(){
		assertFalse(new UpDriver("oneThing").equals(new UpDriver("otherThing")));
	}
	
	@Test public void notEqualsToOtherThing(){
		assertFalse(new UpDriver("oneThing").equals("otherThing"));
	}
	
	@Test public void notEqualsWithNullName(){
		assertFalse(new UpDriver(null).equals(new UpDriver("name")));
	}
	
	@Test public void notEqualsWithDifferentServices(){
		UpDriver driver1 = new UpDriver("driver");
		driver1.addService("s1");
		UpDriver driver2 = new UpDriver("driver");
		driver2.addService("s2");
		assertFalse(driver1.equals(driver2));
	}
	
	@Test public void notEqualsWithDifferentWithOneDriverWithNoServices(){
		UpDriver driver1 = new UpDriver("driver");
		UpDriver driver2 = new UpDriver("driver");
		driver2.addService("s2");
		assertFalse(driver1.equals(driver2));
	}
	
	@Test public void notEqualsWithDifferentEvents(){
		UpDriver driver1 = new UpDriver("driver");
		driver1.addEvent("e1");
		UpDriver driver2 = new UpDriver("driver");
		driver2.addEvent("e2");
		assertFalse(driver1.equals(driver2));
	}
	
	@Test public void notEqualsWithDifferentWithOneDriverWithNoEvents(){
		UpDriver driver1 = new UpDriver("driver");
		UpDriver driver2 = new UpDriver("driver");
		driver2.addEvent("e2");
		assertFalse(driver1.equals(driver2));
	}
	
	@Test public void equalsWithBothWithNullName(){
		assertTrue(new UpDriver(null).equals(new UpDriver(null)));
	}

	@Test public void equalsWithNameEquals(){
		assertTrue(new UpDriver("driver").equals(new UpDriver("driver")));
	}
	
	@Test public void equalsWithEqualsServices(){
		UpDriver driver1 = new UpDriver("driver");
		driver1.addService("s1");
		UpDriver driver2 = new UpDriver("driver");
		driver2.addService("s1");
		assertTrue(driver1.equals(driver2));
	}
	
	@Test public void equalsWithEqualsEvents(){
		UpDriver driver1 = new UpDriver("driver");
		driver1.addEvent("e1");
		UpDriver driver2 = new UpDriver("driver");
		driver2.addEvent("e1");
		assertTrue(driver1.equals(driver2));
	}
	
	@Test public void hash(){
		UpDriver driver1 = new UpDriver("driver");
		UpDriver driver2 = new UpDriver("driver");
		UpDriver driver3 = new UpDriver("notdriver");
		assertThat(driver2.hashCode()).isEqualTo(driver1.hashCode());
		assertThat(driver3.hashCode()).isNotEqualTo(driver1.hashCode());
	}
	
	@Test public void toJson() throws JSONException{
		assertThat(dummyDriver().toJSON().toMap())
		.isEqualTo(dummyJSONDriver().toMap());
	}
	
	@Test public void toJsonForEmtyData() throws JSONException{
		UpDriver driver = new UpDriver();
		JSONObject json = new JSONObject();
		json.put("name", (String)null);
		
		assertThat(driver.toJSON().toMap())
					.isEqualTo(json.toMap());
	}
	
	@Test public void fromJson() throws JSONException{
		JSONObject json = dummyJSONDriver();
		assertThat(UpDriver.fromJSON(json)).isEqualTo(dummyDriver());
	}
	
	@Test public void fromJsonForEmptyData() throws JSONException{
		assertThat(UpDriver.fromJSON(new JSONObject())).isEqualTo(new UpDriver());
	}
	
	private UpDriver dummyDriver() {
		UpDriver driver = new UpDriver("d");
		
		UpService s1 = driver.addService("s");
		s1.addParameter("p", ParameterType.MANDATORY);
		
		driver.addService("sn");
		
		UpService e1 = driver.addEvent("e");
		e1.addParameter("p", ParameterType.OPTIONAL);
		
		driver.addEquivalentDrivers("d1");
		driver.addEquivalentDrivers("d2");
		return driver;
	}

	private JSONObject dummyJSONDriver() throws JSONException {
		JSONObject json = new JSONObject();
		json.put("name", "d");
		
		dummyJSONServices(json);
		dummyJSONEvents(json);
		dummyJSONEquivalentDrivers(json);
		return json;
	}

	private void dummyJSONEquivalentDrivers(JSONObject json)
			throws JSONException {
		JSONArray equivalent_drivers = new JSONArray();
		json.put("equivalent_drivers", equivalent_drivers);
		equivalent_drivers.put("d1");
		equivalent_drivers.put("d2");
	}

	private void dummyJSONEvents(JSONObject json) throws JSONException {
		JSONArray events = new JSONArray();
		json.put("events", events);
		
		JSONObject e_json = new JSONObject();
		events.put(e_json);
		e_json.put("name", "e");
		
		JSONObject e_parameters = new JSONObject();
		e_json.put("parameters", e_parameters);
		
		e_parameters.put("p",ParameterType.OPTIONAL.name());
	}

	private void dummyJSONServices(JSONObject json) throws JSONException {
		JSONArray services = new JSONArray();
		json.put("services", services);
		
		JSONObject s_json = new JSONObject();
		services.put(s_json);
		s_json.put("name", "s");
		
		JSONObject parameters = new JSONObject();
		s_json.put("parameters", parameters);
		
		parameters.put("p",ParameterType.MANDATORY.name());
		
		JSONObject sn_json = new JSONObject();
		services.put(sn_json);
		sn_json.put("name", "sn");
		sn_json.put("parameters", new JSONObject());
	}

}
