package org.unbiquitous.uos.core.messageEngine.messages;

import org.unbiquitous.json.JSONException;
import org.unbiquitous.json.JSONObject;



public class Message {
	
	public enum Type{SERVICE_CALL_REQUEST, SERVICE_CALL_RESPONSE,NOTIFY,ENCAPSULATED_MESSAGE};

	private Type type;
	
	private String error;
	
	public Message() {}
	
	public Message(Type type) {
		this.type = type;
	}
	
	public Type getType() {
		return type;
	}

	protected void setType(Type type) {
		this.type = type;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	public JSONObject toJSON() throws JSONException {
		JSONObject json = new JSONObject();
		
		json.put("type", getType().name());
		json.put("error", getError());
		
		return json;
	}
	
	public static void fromJSON(Message msg, JSONObject json) throws JSONException {
		msg.setError(json.optString("error",null));
	}
}
