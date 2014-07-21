package org.unbiquitous.uos.core.messageEngine.messages;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.unbiquitous.json.JSONException;
import org.unbiquitous.json.JSONObject;

public class EncapsulatedMessageTest {
	@Test
	public void equalsNull() {
		assertFalse(new Capsule().equals(null));
	}
	
	@Test public void notEquals(){
		assertFalse(new Capsule().equals("somthing"));
	}
	
	@Test public void equalsWithEmpty(){
		assertTrue(new Capsule().equals(new Capsule()));
	}
	
	@Test public void notEqualsWithDifferentInnerMessage(){
		Capsule e1 = new Capsule();
		e1.setInnerMessage("msg1");
		Capsule e2 = new Capsule();
		e2.setInnerMessage("msg2");
		
		assertFalse(e1.equals(e2));
	}

	@Test public void equalsWithSameInnerMessage(){
		Capsule e1 = new Capsule();
		e1.setInnerMessage("msg1");
		Capsule e2 = new Capsule();
		e2.setInnerMessage("msg1");
		
		assertTrue(e1.equals(e2));
	}
	
	@Test public void notEqualsWithDifferentSecurityType(){
		Capsule e1 = new Capsule();
		e1.setSecurityType("t1");
		Capsule e2 = new Capsule();
		e2.setSecurityType("t2");
		
		assertFalse(e1.equals(e2));
	}
	
	@Test public void equalsWithSameSecurityType(){
		Capsule e1 = new Capsule();
		e1.setSecurityType("t1");
		Capsule e2 = new Capsule();
		e2.setSecurityType("t1");
		
		assertTrue(e1.equals(e2));
	}


	@Test public void toJSON() throws JSONException{
		assertThat(dummyEncapsulated().toJSON()).isEqualTo(dummyJSON());
	}
	
	@Test public void toJSONWithEmpty() throws JSONException{
		assertThat(new Capsule().toJSON())
					.isEqualTo(new JSONObject(){{
						put("type", Message.Type.ENCAPSULATED_MESSAGE.name());
					}});
	}
	
	@Test public void fromJSON() throws JSONException{
		assertThat(Capsule.fromJSON(dummyJSON()))
		.isEqualTo(dummyEncapsulated());
	}
	
	@Test public void fromJSONWithEmpty() throws JSONException{
		assertThat(Capsule.fromJSON(new JSONObject()))
					.isEqualTo(new Capsule());
	}

	private JSONObject dummyJSON() throws JSONException {
		JSONObject json = new JSONObject(){{
			put("type", Message.Type.ENCAPSULATED_MESSAGE.name());
			put("error", "err");
			
			put("innerMessage", "msg1");
			put("securityType", "t1");
		}};
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
