package org.unbiquitous.uos.core.messageEngine.messages.json;

import org.unbiquitous.json.JSONException;
import org.unbiquitous.uos.core.messageEngine.messages.EncapsulatedMessage;
import org.unbiquitous.uos.core.messageEngine.messages.Message;


public class JSONEncapsulatedMessage extends JSONMessage {
	
	protected static final String PROP_INNER_MESSAGE = "innerMessage";
	protected static final String PROP_SECURITY_TYPE = "securityType";
	
	public JSONEncapsulatedMessage(String source) throws JSONException {
		super(source);
	}
	
	
	public JSONEncapsulatedMessage(EncapsulatedMessage bean) throws JSONException {
		super((Message)bean);
		this.put(PROP_TYPE,bean.getType());
		this.put(PROP_ERROR,bean.getError());
		this.put(PROP_INNER_MESSAGE,bean.getInnerMessage());
		this.put(PROP_SECURITY_TYPE,bean.getSecurityType());
	}
	
	public EncapsulatedMessage getAsObject() throws JSONException{
		EncapsulatedMessage message = new EncapsulatedMessage();
		
		message.setError(this.optString(PROP_ERROR,null));
		message.setInnerMessage(this.optString(PROP_INNER_MESSAGE));
		message.setSecurityType(this.optString(PROP_SECURITY_TYPE));
		
		return message;
	}
}
