package org.unbiquitous.uos.core.messageEngine.dataType;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;
import org.unbiquitous.uos.core.messageEngine.dataType.UpService.ParameterType;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class UpDriverTest {
	private final JsonNodeFactory factory = JsonNodeFactory.instance;
	private final ObjectMapper mapper = new ObjectMapper();

	@Test
	public void equalsNull() {
		assertFalse(new UpDriver(null).equals(null));
	}

	@Test
	public void notEquals() {
		assertFalse(new UpDriver("oneThing").equals(new UpDriver("otherThing")));
	}

	@Test
	public void notEqualsToOtherThing() {
		assertFalse(new UpDriver("oneThing").equals("otherThing"));
	}

	@Test
	public void notEqualsWithNullName() {
		assertFalse(new UpDriver(null).equals(new UpDriver("name")));
	}

	@Test
	public void notEqualsWithDifferentServices() {
		UpDriver driver1 = new UpDriver("driver");
		driver1.addService("s1");
		UpDriver driver2 = new UpDriver("driver");
		driver2.addService("s2");
		assertFalse(driver1.equals(driver2));
	}

	@Test
	public void notEqualsWithDifferentWithOneDriverWithNoServices() {
		UpDriver driver1 = new UpDriver("driver");
		UpDriver driver2 = new UpDriver("driver");
		driver2.addService("s2");
		assertFalse(driver1.equals(driver2));
	}

	@Test
	public void notEqualsWithDifferentEvents() {
		UpDriver driver1 = new UpDriver("driver");
		driver1.addEvent("e1");
		UpDriver driver2 = new UpDriver("driver");
		driver2.addEvent("e2");
		assertFalse(driver1.equals(driver2));
	}

	@Test
	public void notEqualsWithDifferentWithOneDriverWithNoEvents() {
		UpDriver driver1 = new UpDriver("driver");
		UpDriver driver2 = new UpDriver("driver");
		driver2.addEvent("e2");
		assertFalse(driver1.equals(driver2));
	}

	@Test
	public void equalsWithBothWithNullName() {
		assertTrue(new UpDriver(null).equals(new UpDriver(null)));
	}

	@Test
	public void equalsWithNameEquals() {
		assertTrue(new UpDriver("driver").equals(new UpDriver("driver")));
	}

	@Test
	public void equalsWithEqualsServices() {
		UpDriver driver1 = new UpDriver("driver");
		driver1.addService("s1");
		UpDriver driver2 = new UpDriver("driver");
		driver2.addService("s1");
		assertTrue(driver1.equals(driver2));
	}

	@Test
	public void equalsWithEqualsEvents() {
		UpDriver driver1 = new UpDriver("driver");
		driver1.addEvent("e1");
		UpDriver driver2 = new UpDriver("driver");
		driver2.addEvent("e1");
		assertTrue(driver1.equals(driver2));
	}

	@Test
	public void hash() {
		UpDriver driver1 = new UpDriver("driver");
		UpDriver driver2 = new UpDriver("driver");
		UpDriver driver3 = new UpDriver("notdriver");
		assertThat(driver2.hashCode()).isEqualTo(driver1.hashCode());
		assertThat(driver3.hashCode()).isNotEqualTo(driver1.hashCode());
	}

	@Test
	public void toJson() throws IOException {
		JsonNode dd = mapper.valueToTree(dummyDriver());
		JsonNode dsd = dummyJSONDriver();
		assertThat(dd).isEqualTo(dsd);
	}

	@Test
	public void toJsonForEmtyData() throws IOException {
		UpDriver driver = new UpDriver();
		ObjectNode json = factory.objectNode();
		// json.put("name", (String) null);

		assertThat(mapper.valueToTree(driver)).isEqualTo(json);
	}

	@Test
	public void fromJson() throws IOException {
		assertThat(mapper.treeToValue(dummyJSONDriver(), UpDriver.class)).isEqualTo(dummyDriver());
	}

	@Test
	public void fromJsonForEmptyData() throws IOException {
		assertThat(mapper.treeToValue(factory.objectNode(), UpDriver.class)).isEqualTo(new UpDriver());
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

	private JsonNode dummyJSONDriver() {
		ObjectNode json = factory.objectNode();
		json.put("name", "d");
		dummyJSONServices(json);
		dummyJSONEvents(json);
		dummyJSONEquivalentDrivers(json);
		return json;
	}

	private void dummyJSONEquivalentDrivers(ObjectNode json) {
		ArrayNode equivalent_drivers = factory.arrayNode();
		json.set("equivalent_drivers", equivalent_drivers);
		equivalent_drivers.add("d1");
		equivalent_drivers.add("d2");
	}

	private void dummyJSONEvents(ObjectNode json) {
		ArrayNode events = factory.arrayNode();
		json.set("events", events);

		ObjectNode e_json = factory.objectNode();
		events.add(e_json);
		e_json.put("name", "e");

		ObjectNode e_parameters = e_json.putObject("parameters");
		e_parameters.put("p", ParameterType.OPTIONAL.name());
	}

	private void dummyJSONServices(ObjectNode json) {
		ArrayNode services = factory.arrayNode();
		json.set("services", services);

		ObjectNode s_json = factory.objectNode();
		services.add(s_json);
		s_json.put("name", "s");

		ObjectNode parameters = s_json.putObject("parameters");
		parameters.put("p", ParameterType.MANDATORY.name());

		ObjectNode sn_json = factory.objectNode();
		services.add(sn_json);
		sn_json.put("name", "sn");
		//sn_json.set("parameters", factory.objectNode());
	}
}
