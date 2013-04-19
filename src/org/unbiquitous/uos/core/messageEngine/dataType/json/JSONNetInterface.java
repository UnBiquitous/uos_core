package org.unbiquitous.uos.core.messageEngine.dataType.json;


import org.unbiquitous.json.JSONException;
import org.unbiquitous.json.JSONObject;
import org.unbiquitous.uos.core.messageEngine.dataType.UpNetworkInterface;

public class JSONNetInterface extends JSONObject {

	private static final String PROP_NET_TYPE = "netType";
	private static final String PROP_NETWORK_ADDRESS = "networkAddress";

	public JSONNetInterface(String source) throws JSONException {
		super(source);
	}

	JSONNetInterface(UpNetworkInterface bean) throws JSONException{
		this.put(PROP_NETWORK_ADDRESS,bean.getNetworkAddress());
		// NetType netType;
		this.put(PROP_NET_TYPE,bean.getNetType());
	}
	
	public UpNetworkInterface getAsObject() throws JSONException{
		UpNetworkInterface netInterface = new UpNetworkInterface();
		
		netInterface.setNetworkAddress(this.getString(PROP_NETWORK_ADDRESS));
		netInterface.setNetType(this.getString(PROP_NET_TYPE));
		
		
		return netInterface;
	}
	
}
