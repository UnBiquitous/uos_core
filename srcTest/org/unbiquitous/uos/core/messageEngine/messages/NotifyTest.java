package org.unbiquitous.uos.core.messageEngine.messages;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;

import org.junit.Test;
import org.unbiquitous.json.JSONException;
import org.unbiquitous.json.JSONObject;

public class NotifyTest {

	@Test
	public void equalsNull() {
		assertFalse(new Notify().equals(null));
	}
	
	@Test public void notEquals(){
		assertFalse(new Notify().equals("something"));
	}
	
	@Test public void notEqualsToOtherThing(){
		assertFalse(new Notify("e1").equals(new Notify("e2")));
		assertFalse(new Notify("e","d1").equals(new Notify("e","d2")));
		assertFalse(new Notify("e","d", "id1").equals(new Notify("e","d","id2")));
	}
	
	@Test public void equalsWithEmpty(){
		assertTrue(new Notify().equals(new Notify()));
	}
	
	@Test public void notEqualsWithNullData(){
		assertFalse(new Notify().equals(new Notify("e")));
		assertFalse(new Notify().equals(new Notify("e","d")));
		assertFalse(new Notify().equals(new Notify("e","d","id")));
	}
	
	@Test public void equalsWithEventKey(){
		assertTrue(new Notify("e").equals(new Notify("e")));
		assertTrue(new Notify("e","d").equals(new Notify("e","d")));
		assertTrue(new Notify("e","d","id").equals(new Notify("e","d","id")));
	}
	
	@Test public void notEqualsWithDifferentParameters(){
		Notify e1 = new Notify("e","d","id").addParameter("k", "v");
		Notify e2 = new Notify("e","d","id").addParameter("k'", "v'");
		assertFalse(new Notify().equals(e1));
		assertFalse(e2.equals(e1));
	}
	
	@Test public void notEqualsWithSameParameters(){
		Notify e1 = new Notify("e","d","id").addParameter("k", "v");
		Notify e2 = new Notify("e","d","id").addParameter("k", "v");
		assertTrue(e2.equals(e1));
	}
	
	@Test public void hash(){
		Notify e1 = new Notify("e");
		Notify e2 = new Notify("e");
		Notify ed1 = new Notify("e","d");
		Notify ed2 = new Notify("e","d");
		Notify edid1 = new Notify("e","d","id");
		Notify edid2 = new Notify("e","d","id");
		Notify edid3 = new Notify("e","d","idx");
		Notify edid4 = new Notify("e","dx","id");
		Notify edid5 = new Notify("ex","d","id");
		assertThat(e2.hashCode()).isEqualTo(e1.hashCode());
		assertThat(ed1.hashCode()).isEqualTo(ed2.hashCode());
		assertThat(edid1.hashCode()).isEqualTo(edid2.hashCode());
		assertThat(edid1.hashCode()).isNotEqualTo(edid3.hashCode());
		assertThat(edid1.hashCode()).isNotEqualTo(edid4.hashCode());
		assertThat(edid1.hashCode()).isNotEqualTo(edid5.hashCode());
	}
	
	@Test public void toJSON() throws JSONException{
		assertThat(dummyJSON()).isEqualTo(dummyNotify().toJSON());
	}
	
	@Test public void toJSONWithEmpty() throws JSONException{
		JSONObject json = new JSONObject(){{ 
			put("type", Message.Type.NOTIFY.name());
		}};
		assertThat(json).isEqualTo(new Notify().toJSON());
	}
	
	@Test public void fromJSON() throws JSONException{
		assertThat(dummyNotify()).isEqualTo(Notify.fromJSON(dummyJSON()));
	}
	
	@Test public void fromJSONWithEmpty() throws JSONException{
		assertThat(new Notify()).isEqualTo(Notify.fromJSON(new JSONObject()));
	}

	@SuppressWarnings({ "rawtypes", "unchecked", "serial" })
	private JSONObject dummyJSON() throws JSONException {
		JSONObject json = new JSONObject();
		json.put("type", Message.Type.NOTIFY.name());
		json.put("error", "err"); 
		json.put("eventKey","event");
		json.put("driver","driver");
		json.put("instanceId","id");
		json.put("parameters", new HashMap() {{ 
			put("a","a1");
			put("b",1);
		}});
		return json;
	}

	private Notify dummyNotify() {
		Notify e = new Notify("event","driver","id")
						.addParameter("a", "a1")
						.addParameter("b", 1);
		e.setError("err");
		return e;
	}
	
}
