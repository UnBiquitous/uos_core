package br.unb.unbiquitous.ubiquitos.uos.messageEngine.messages.json;

import br.unb.unbiquitous.json.JSONException;
import br.unb.unbiquitous.ubiquitos.uos.messageEngine.messages.EncapsulatedMessage;
import br.unb.unbiquitous.ubiquitos.uos.messageEngine.messages.Message;


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
