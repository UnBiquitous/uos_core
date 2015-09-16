package org.unbiquitous.uos.core.messageEngine.messages;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class EncapsulatedMessageTest {
	private static final ObjectMapper mapper = new ObjectMapper();

	@Test
	public void equalsNull() {
		assertFalse(new Capsule().equals(null));
	}

	@Test
	public void notEquals() {
		assertFalse(new Capsule().equals("somthing"));
	}

	@Test
	public void equalsWithEmpty() {
		assertTrue(new Capsule().equals(new Capsule()));
	}

	@Test
	public void notEqualsWithDifferentInnerMessage() {
		Capsule e1 = new Capsule();
		e1.setInnerMessage("msg1");
		Capsule e2 = new Capsule();
		e2.setInnerMessage("msg2");

		assertFalse(e1.equals(e2));
	}

	@Test
	public void equalsWithSameInnerMessage() {
		Capsule e1 = new Capsule();
		e1.setInnerMessage("msg1");
		Capsule e2 = new Capsule();
		e2.setInnerMessage("msg1");

		assertTrue(e1.equals(e2));
	}

	@Test
	public void notEqualsWithDifferentSecurityType() {
		Capsule e1 = new Capsule();
		e1.setSecurityType("t1");
		Capsule e2 = new Capsule();
		e2.setSecurityType("t2");

		assertFalse(e1.equals(e2));
	}

	@Test
	public void equalsWithSameSecurityType() {
		Capsule e1 = new Capsule();
		e1.setSecurityType("t1");
		Capsule e2 = new Capsule();
		e2.setSecurityType("t1");

		assertTrue(e1.equals(e2));
	}

	@Test
	public void toJSON() {
		assertThat(mapper.valueToTree(dummyEncapsulated())).isEqualTo(dummyJSON());
	}

	@Test
	public void toJSONWithEmpty() {
		ObjectNode aux = mapper.getNodeFactory().objectNode();
		aux.put("type", Message.Type.ENCAPSULATED_MESSAGE.name());
		assertThat(mapper.valueToTree(new Capsule())).isEqualTo(aux);
	}

	@Test
	public void fromJSON() throws IOException {
		assertThat(mapper.treeToValue(dummyJSON(), Capsule.class)).isEqualTo(dummyEncapsulated());
	}

	@Test
	public void fromJSONWithEmpty() throws IOException {
		assertThat(mapper.treeToValue(mapper.getNodeFactory().objectNode(), Capsule.class)).isEqualTo(new Capsule());
	}

	private ObjectNode dummyJSON() {
		ObjectNode json = mapper.getNodeFactory().objectNode();

		json.put("type", Message.Type.ENCAPSULATED_MESSAGE.name());
		json.put("error", "err");

		json.put("innerMessage", "msg1");
		json.put("securityType", "t1");

		return json;
	}

	private Capsule dummyEncapsulated() {
		Capsule e = new Capsule();
		e.setError("err");

		e.setInnerMessage("msg1");
		e.setSecurityType("t1");
		return e;
	}
}
