package org.unbiquitous.uos.core.messageEngine.messages;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class NotifyTest {
	private static final ObjectMapper mapper = new ObjectMapper();

	@Test
	public void equalsNull() {
		assertFalse(new Notify().equals(null));
	}

	@Test
	public void notEquals() {
		assertFalse(new Notify().equals("something"));
	}

	@Test
	public void notEqualsToOtherThing() {
		assertFalse(new Notify("e1").equals(new Notify("e2")));
		assertFalse(new Notify("e", "d1").equals(new Notify("e", "d2")));
		assertFalse(new Notify("e", "d", "id1").equals(new Notify("e", "d", "id2")));
	}

	@Test
	public void equalsWithEmpty() {
		assertTrue(new Notify().equals(new Notify()));
	}

	@Test
	public void notEqualsWithNullData() {
		assertFalse(new Notify().equals(new Notify("e")));
		assertFalse(new Notify().equals(new Notify("e", "d")));
		assertFalse(new Notify().equals(new Notify("e", "d", "id")));
	}

	@Test
	public void equalsWithEventKey() {
		assertTrue(new Notify("e").equals(new Notify("e")));
		assertTrue(new Notify("e", "d").equals(new Notify("e", "d")));
		assertTrue(new Notify("e", "d", "id").equals(new Notify("e", "d", "id")));
	}

	@Test
	public void notEqualsWithDifferentParameters() {
		Notify e1 = new Notify("e", "d", "id").addParameter("k", "v");
		Notify e2 = new Notify("e", "d", "id").addParameter("k'", "v'");
		assertFalse(new Notify().equals(e1));
		assertFalse(e2.equals(e1));
	}

	@Test
	public void notEqualsWithSameParameters() {
		Notify e1 = new Notify("e", "d", "id").addParameter("k", "v");
		Notify e2 = new Notify("e", "d", "id").addParameter("k", "v");
		assertTrue(e2.equals(e1));
	}

	@Test
	public void hash() {
		Notify e1 = new Notify("e");
		Notify e2 = new Notify("e");
		Notify ed1 = new Notify("e", "d");
		Notify ed2 = new Notify("e", "d");
		Notify edid1 = new Notify("e", "d", "id");
		Notify edid2 = new Notify("e", "d", "id");
		Notify edid3 = new Notify("e", "d", "idx");
		Notify edid4 = new Notify("e", "dx", "id");
		Notify edid5 = new Notify("ex", "d", "id");
		assertThat(e2.hashCode()).isEqualTo(e1.hashCode());
		assertThat(ed1.hashCode()).isEqualTo(ed2.hashCode());
		assertThat(edid1.hashCode()).isEqualTo(edid2.hashCode());
		assertThat(edid1.hashCode()).isNotEqualTo(edid3.hashCode());
		assertThat(edid1.hashCode()).isNotEqualTo(edid4.hashCode());
		assertThat(edid1.hashCode()).isNotEqualTo(edid5.hashCode());
	}

	@Test
	public void toJSON() {
		assertThat(dummyJSON()).isEqualTo(mapper.valueToTree(dummyNotify()));
	}

	@Test
	public void toJSONWithEmpty() {
		ObjectNode json = mapper.createObjectNode();
		json.put("type", Message.Type.NOTIFY.name());
		assertThat(json).isEqualTo(mapper.valueToTree(new Notify()));
	}

	@Test
	public void fromJSON() throws IOException {
		assertThat(dummyNotify()).isEqualTo(mapper.treeToValue(dummyJSON(), Notify.class));
	}

	@Test
	public void fromJSONWithEmpty() throws IOException {
		assertThat(new Notify()).isEqualTo(mapper.treeToValue(mapper.createObjectNode(), Notify.class));
	}

	private ObjectNode dummyJSON() {
		ObjectNode json = mapper.createObjectNode();
		json.put("type", Message.Type.NOTIFY.name());
		json.put("error", "err");
		json.put("eventKey", "event");
		json.put("driver", "driver");
		json.put("instanceId", "id");
		ObjectNode params = json.putObject("parameters");
		params.put("a", "a1");
		params.put("b", 1);
		return json;
	}

	private Notify dummyNotify() {
		Notify e = new Notify("event", "driver", "id").addParameter("a", "a1").addParameter("b", 1);
		e.setError("err");
		return e;
	}
}
