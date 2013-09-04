package org.unbiquitous.uos.core.messageEngine.dataType;

import static org.unbiquitous.uos.core.ClassLoaderUtils.compare;

import org.unbiquitous.json.JSONException;
import org.unbiquitous.json.JSONObject;


public class UpNetworkInterface {

	private String netType;
	
	private String networkAddress;
	
	public UpNetworkInterface() {}
	
	public UpNetworkInterface(String netType, String networkAddress) {
		this.netType = netType;
		this.networkAddress = networkAddress;
	}



	public String getNetType() {
		return netType;
	}

	public void setNetType(String netType) {
		this.netType = netType;
	}

	public String getNetworkAddress() {
		return networkAddress;
	}

	public void setNetworkAddress(String networkAddress) {
		this.networkAddress = networkAddress;
	} 
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null || ! (obj instanceof UpNetworkInterface) ){
			return false;
		}
		
		UpNetworkInterface d = (UpNetworkInterface) obj;
		
		if(!compare(this.networkAddress,d.networkAddress)) return false;
		if(!compare(this.netType,d.netType)) return false;
		
		return true;
	}
	
	public JSONObject toJSON() throws JSONException {
		JSONObject ni_json = new JSONObject();
		ni_json.put("networkAddress", this.getNetworkAddress());
		ni_json.put("netType", this.getNetType());
		return ni_json;
	}
	
	public static UpNetworkInterface fromJSON(JSONObject json)
			throws JSONException {
		UpNetworkInterface ni =  new UpNetworkInterface();
		ni.setNetworkAddress(json.getString("networkAddress"));
		ni.setNetType(json.getString("netType"));
		return ni;
	}
}
