package br.unb.unbiquitous.ubiquitos.uos.messageEngine.messages.json;

import br.unb.unbiquitous.json.JSONException;
import br.unb.unbiquitous.json.JSONObject;
import br.unb.unbiquitous.ubiquitos.uos.messageEngine.messages.Message;

public class JSONMessage extends JSONObject  {
	
	protected static final String PROP_TYPE = "type";
	protected static final String PROP_ERROR = "error";
	
	public JSONMessage(String source) throws JSONException {
		super(source);
	}
	
	
	public JSONMessage(Message bean) throws JSONException {
		this.put(PROP_TYPE,bean.getType());
		this.put(PROP_ERROR,bean.getError());
	}
	
	public Message getAsObject() throws JSONException{
		Message message = new Message(Message.Type.valueOf((this.getString(PROP_TYPE))));
		
		message.setError(this.optString(PROP_ERROR,null));
		
		return message;
	}
}

