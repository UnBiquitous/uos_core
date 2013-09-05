package org.unbiquitous.uos.core.messageEngine.messages;

import static org.unbiquitous.uos.core.ClassLoaderUtils.compare;

import org.unbiquitous.json.JSONException;
import org.unbiquitous.json.JSONObject;


public class EncapsulatedMessage extends Message {

	private String innerMessage;
	
	private String securityType;
	
	public EncapsulatedMessage() {
		setType(Type.ENCAPSULATED_MESSAGE);
	}

	public EncapsulatedMessage(String securityType, String innerMessage) {
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
		if (obj == null || !( obj instanceof EncapsulatedMessage)){
			return false;
		}
		EncapsulatedMessage temp = (EncapsulatedMessage) obj; 
		
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

	public static EncapsulatedMessage fromJSON(JSONObject json) throws JSONException {
		EncapsulatedMessage e = new EncapsulatedMessage();
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
