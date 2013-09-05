package org.unbiquitous.uos.core.messageEngine.messages;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.unbiquitous.json.JSONException;
import org.unbiquitous.json.JSONObject;

public class ServiceResponseTest {

	@Test
	public void equalsNull() {
		assertFalse(new Response().equals(null));
	}
	
	@Test public void notEquals(){
		assertFalse(new Response().equals("somthing"));
	}
	
	@Test public void equalsWithEmpty(){
		assertTrue(new Response().equals(new Response()));
	}
	
	@Test public void notEqualsWithDifferentParameter(){
		Response r1 = new Response();
		r1.addParameter("a", 1);
		Response r2 = new Response();
		r2.addParameter("a", 2);
		Response r3 = new Response();
		r2.addParameter("b", 1);
		
		assertFalse(r1.equals(r2));
		assertFalse(r1.equals(r3));
	}
	
	@Test public void equalsWithSameParameter(){
		Response r1 = new Response();
		r1.addParameter("a", 1);
		Response r2 = new Response();
		r2.addParameter("a", 1);
		
		assertTrue(r1.equals(r2));
	}
	
	@Test public void hash(){
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
	
	@Test public void toJSON() throws JSONException{
		assertThat(dummyResponse().toJSON()).isEqualTo(dummyJSON());
	}
	
	@Test public void toJSONWithEmpty() throws JSONException{
		assertThat(new Response().toJSON())
					.isEqualTo(new JSONObject(){{
						put("type", Message.Type.SERVICE_CALL_RESPONSE.name());
					}});
	}
	
	@Test public void fromJSON() throws JSONException{
		assertThat(Response.fromJSON(dummyJSON()))
				.isEqualTo(dummyResponse());
	}
	
	@Test public void fromJSONWithEmpty() throws JSONException{
		assertThat(Response.fromJSON(new JSONObject()))
				.isEqualTo(new Response());
	}

	private JSONObject dummyJSON() throws JSONException {
		JSONObject json = new JSONObject(){{
			put("type", Message.Type.SERVICE_CALL_RESPONSE.name());
			put("error", "err");
			put("responseData",new JSONObject(){{
				put("a","a");
				put("b",1);
			}});
		}};
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
