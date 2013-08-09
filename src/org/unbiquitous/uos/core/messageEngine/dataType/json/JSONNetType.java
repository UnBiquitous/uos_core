package org.unbiquitous.uos.core.messageEngine.dataType.json;


import org.unbiquitous.json.JSONException;
import org.unbiquitous.json.JSONObject;
import org.unbiquitous.uos.core.messageEngine.dataType.NetworkProtocol;

public class JSONNetType extends JSONObject{

	private static final String PROP_ID = "id";

	public JSONNetType(String source) throws JSONException {
		super(source);
	}

	public JSONNetType(NetworkProtocol bean) throws JSONException {
		this.put(PROP_ID,bean.getId());
	}
	
	public NetworkProtocol getAsObject() throws JSONException{
		NetworkProtocol netType = new NetworkProtocol();
		
		netType.setId( this.getInt(PROP_ID));
		
		return netType;
	}
}
