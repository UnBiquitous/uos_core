package org.unbiquitous.uos.core.messageEngine.messages;

import static org.unbiquitous.uos.core.ClassLoaderUtils.compare;

import org.unbiquitous.json.JSONException;
import org.unbiquitous.json.JSONObject;


public class Capsule extends Message {

	private String innerMessage;
	
	private String securityType;
	
	public Capsule() {
		setType(Type.ENCAPSULATED_MESSAGE);
	}

	public Capsule(String securityType, String innerMessage) {
		this();
		this.securityType = securityType;
		this.innerMessage = innerMessage;
	}


	public String getInnerMessage() {
		return innerMessage;
	}

	public void setInnerMessage(String innerMessage) {
		this.innerMessage = innerMessage;
	}

	public String getSecurityType() {
		return securityType;
	}

	public void setSecurityType(String securityType) {
		this.securityType = securityType;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null || !( obj instanceof Capsule)){
			return false;
		}
		Capsule temp = (Capsule) obj; 
		
		if(!compare(this.innerMessage,temp.innerMessage)) return false;
		if(!compare(this.securityType,temp.securityType)) return false;
		
		return true;
	}
	
	@Override
	public JSONObject toJSON() throws JSONException {
		JSONObject json = super.toJSON();
		
		json.put("innerMessage", this.innerMessage);
		json.put("securityType", this.securityType);
		return json;
	}

	public static Capsule fromJSON(JSONObject json) throws JSONException {
		Capsule e = new Capsule();
		Message.fromJSON(e, json);
		e.innerMessage = json.optString("innerMessage",null);
		e.securityType = json.optString("securityType",null);
		return e;
	}
	
	@Override
	public String toString() {
		try {
			return toJSON().toString();
		} catch (JSONException e) {
			return super.toString();
		}
	}
}
