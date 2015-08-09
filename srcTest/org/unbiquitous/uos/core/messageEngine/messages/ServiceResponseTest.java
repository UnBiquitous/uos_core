package org.unbiquitous.uos.core.messageEngine.messages;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class ServiceResponseTest {
	private static final ObjectMapper mapper = new ObjectMapper();

	@Test
	public void equalsNull() {
		assertFalse(new Response().equals(null));
	}

	@Test
	public void notEquals() {
		assertFalse(new Response().equals("somthing"));
	}

	@Test
	public void equalsWithEmpty() {
		assertTrue(new Response().equals(new Response()));
	}

	@Test
	public void notEqualsWithDifferentParameter() {
		Response r1 = new Response();
		r1.addParameter("a", 1);
		Response r2 = new Response();
		r2.addParameter("a", 2);
		Response r3 = new Response();
		r2.addParameter("b", 1);

		assertFalse(r1.equals(r2));
		assertFalse(r1.equals(r3));
	}

	@Test
	public void equalsWithSameParameter() {
		Response r1 = new Response();
		r1.addParameter("a", 1);
		Response r2 = new Response();
		r2.addParameter("a", 1);

		assertTrue(r1.equals(r2));
	}

	@Test
	public void hash() {
		Response r1 = new Response().addParameter("a", 1);
		Response r2 = new Response().addParameter("a", 1);
		Response r3 = new Response().addParameter("b", 1);
		Response r4 = new Response().addParameter("a", 2);
		Response r5 = new Response();

		assertThat(r2.hashCode()).isEqualTo(r1.hashCode());
		assertThat(r3.hashCode()).isNotEqualTo(r1.hashCode());
		assertThat(r4.hashCode()).isNotEqualTo(r1.hashCode());
		assertThat(r5.hashCode()).isNotEqualTo(r1.hashCode());
	}

	@Test
	public void toJSON() {
		assertThat(mapper.valueToTree(dummyResponse())).isEqualTo(dummyJSON());
	}

	@Test
	public void toJSONWithEmpty() {
		ObjectNode aux = mapper.createObjectNode();
		aux.put("type", Message.Type.SERVICE_CALL_RESPONSE.name());
		assertThat(mapper.valueToTree(new Response())).isEqualTo(aux);
	}

	@Test
	public void fromJSON() throws IOException {
		assertThat(mapper.treeToValue(dummyJSON(), Response.class)).isEqualTo(dummyResponse());
	}

	@Test
	public void fromJSONWithEmpty() throws IOException {
		assertThat(mapper.treeToValue(mapper.createObjectNode(), Response.class)).isEqualTo(new Response());
	}

	private ObjectNode dummyJSON() {
		ObjectNode json = mapper.createObjectNode();
		json.put("type", Message.Type.SERVICE_CALL_RESPONSE.name());
		json.put("error", "err");
		ObjectNode rd = json.putObject("responseData");
		rd.put("a", "a");
		rd.put("b", 1);
		return json;
	}

	private Response dummyResponse() {
		Response r = new Response();
		r.setError("err");
		r.addParameter("a", "a");
		r.addParameter("b", 1);
		return r;
	}
}
