package org.unbiquitous.uos.core.messageEngine.messages;

import static org.fest.assertions.api.Assertions.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.unbiquitous.json.JSONException;
import org.unbiquitous.json.JSONObject;

public class EncapsulatedMessageTest {
	@Test
	public void equalsNull() {
		assertFalse(new EncapsulatedMessage().equals(null));
	}
	
	@Test public void notEquals(){
		assertFalse(new EncapsulatedMessage().equals("somthing"));
	}
	
	@Test public void equalsWithEmpty(){
		assertTrue(new EncapsulatedMessage().equals(new EncapsulatedMessage()));
	}
	
	@Test public void notEqualsWithDifferentInnerMessage(){
		EncapsulatedMessage e1 = new EncapsulatedMessage();
		e1.setInnerMessage("msg1");
		EncapsulatedMessage e2 = new EncapsulatedMessage();
		e2.setInnerMessage("msg2");
		
		assertFalse(e1.equals(e2));
	}

	@Test public void equalsWithSameInnerMessage(){
		EncapsulatedMessage e1 = new EncapsulatedMessage();
		e1.setInnerMessage("msg1");
		EncapsulatedMessage e2 = new EncapsulatedMessage();
		e2.setInnerMessage("msg1");
		
		assertTrue(e1.equals(e2));
	}
	
	@Test public void notEqualsWithDifferentSecurityType(){
		EncapsulatedMessage e1 = new EncapsulatedMessage();
		e1.setSecurityType("t1");
		EncapsulatedMessage e2 = new EncapsulatedMessage();
		e2.setSecurityType("t2");
		
		assertFalse(e1.equals(e2));
	}
	
	@Test public void equalsWithSameSecurityType(){
		EncapsulatedMessage e1 = new EncapsulatedMessage();
		e1.setSecurityType("t1");
		EncapsulatedMessage e2 = new EncapsulatedMessage();
		e2.setSecurityType("t1");
		
		assertTrue(e1.equals(e2));
	}


	@Test public void toJSON() throws JSONException{
		assertThat(dummyEncapsulated().toJSON()).isEqualTo(dummyJSON());
	}
	
	@Test public void toJSONWithEmpty() throws JSONException{
		assertThat(new EncapsulatedMessage().toJSON())
					.isEqualTo(new JSONObject(){{
						put("type", Message.Type.ENCAPSULATED_MESSAGE.name());
					}});
	}
	
	@Test public void fromJSON() throws JSONException{
		assertThat(EncapsulatedMessage.fromJSON(dummyJSON()))
		.isEqualTo(dummyEncapsulated());
	}
	
	@Test public void fromJSONWithEmpty() throws JSONException{
		assertThat(EncapsulatedMessage.fromJSON(new JSONObject()))
					.isEqualTo(new EncapsulatedMessage());
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

	private EncapsulatedMessage dummyEncapsulated() {
		EncapsulatedMessage e = new EncapsulatedMessage();
		e.setError("err");
		
		e.setInnerMessage("msg1");
		e.setSecurityType("t1");
		return e;
	}
}
