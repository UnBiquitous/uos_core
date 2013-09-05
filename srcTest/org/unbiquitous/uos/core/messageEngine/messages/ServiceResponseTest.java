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
		assertFalse(new ServiceResponse().equals(null));
	}
	
	@Test public void notEquals(){
		assertFalse(new ServiceResponse().equals("somthing"));
	}
	
	@Test public void equalsWithEmpty(){
		assertTrue(new ServiceResponse().equals(new ServiceResponse()));
	}
	
	@Test public void notEqualsWithDifferentParameter(){
		ServiceResponse r1 = new ServiceResponse();
		r1.addParameter("a", 1);
		ServiceResponse r2 = new ServiceResponse();
		r2.addParameter("a", 2);
		ServiceResponse r3 = new ServiceResponse();
		r2.addParameter("b", 1);
		
		assertFalse(r1.equals(r2));
		assertFalse(r1.equals(r3));
	}
	
	@Test public void equalsWithSameParameter(){
		ServiceResponse r1 = new ServiceResponse();
		r1.addParameter("a", 1);
		ServiceResponse r2 = new ServiceResponse();
		r2.addParameter("a", 1);
		
		assertTrue(r1.equals(r2));
	}
	
	@Test public void hash(){
		ServiceResponse r1 = new ServiceResponse().addParameter("a", 1);
		ServiceResponse r2 = new ServiceResponse().addParameter("a", 1);
		ServiceResponse r3 = new ServiceResponse().addParameter("b", 1);
		ServiceResponse r4 = new ServiceResponse().addParameter("a", 2);
		ServiceResponse r5 = new ServiceResponse();
		
		assertThat(r2.hashCode()).isEqualTo(r1.hashCode());
		assertThat(r3.hashCode()).isNotEqualTo(r1.hashCode());
		assertThat(r4.hashCode()).isNotEqualTo(r1.hashCode());
		assertThat(r5.hashCode()).isNotEqualTo(r1.hashCode());
	}
	
	@Test public void toJSON() throws JSONException{
		assertThat(dummyResponse().toJSON()).isEqualTo(dummyJSON());
	}
	
	@Test public void toJSONWithEmpty() throws JSONException{
		assertThat(new ServiceResponse().toJSON())
					.isEqualTo(new JSONObject(){{
						put("type", Message.Type.SERVICE_CALL_RESPONSE.name());
					}});
	}
	
	@Test public void fromJSON() throws JSONException{
		assertThat(ServiceResponse.fromJSON(dummyJSON()))
				.isEqualTo(dummyResponse());
	}
	
	@Test public void fromJSONWithEmpty() throws JSONException{
		assertThat(ServiceResponse.fromJSON(new JSONObject()))
				.isEqualTo(new ServiceResponse());
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

	private ServiceResponse dummyResponse() {
		ServiceResponse r = new ServiceResponse();
		r.setError("err");
		r.addParameter("a", "a");
		r.addParameter("b", 1);
		return r;
	}
}
