package org.unbiquitous.uos.core.messageEngine.dataType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.unbiquitous.json.JSONArray;
import org.unbiquitous.json.JSONException;
import org.unbiquitous.json.JSONObject;

/**
 * This class represents a device from the middleware view.
 * 
 * @author Fabricio Nogueira Buzeto
 *
 */
public class UpDevice {
	
	private String name;
	private List<UpNetworkInterface> networks;
	private Map<String, String> meta; //TODO: it's not on JSON

	public UpDevice() {}
	
	public UpDevice(String name) {
		this.name = name;
	}
	
	public UpDevice addNetworkInterface(String networkAdress, String networkType){
		if (networks == null){
			networks =  new ArrayList<UpNetworkInterface>();
		}
		networks.add(new UpNetworkInterface(networkType,networkAdress));
		return this;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<UpNetworkInterface> getNetworks() {
		return networks;
	}

	public void setNetworks(List<UpNetworkInterface> networks) {
		this.networks = networks;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null || ! (obj instanceof UpDevice) ){
			return false;
		}
		
		UpDevice d = (UpDevice) obj;
		
		boolean equals = this.name == d.name ||
				( this.name != null && this.name.equals(d.name));
		
		equals &= this.networks == d.networks ||
				( this.networks != null  && d.networks != null
					&& this.networks.containsAll(d.networks)
					&& d.networks.containsAll(this.networks));
		
		equals &= this.meta == d.meta ||
				( this.meta != null && this.meta.equals(d.meta));
		
		return equals;
	}
	
	@Override
	public int hashCode() {
		if(name != null){
			return name.hashCode();
		}
		return super.hashCode();
	}
	
	@Override
	public String toString() {
		try {
			return toJSON().toString();
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Return meta properties about the device.
	 * Ex:
	 * 
	 * 'platform': The undelying platform like "Dalvik" or "Sun Java VM"
	 */
	public Object getProperty(String key) {
		if (meta == null) return null;
		return meta.get(key);
	}

	public void addProperty(String key, String value) {
		if (meta == null) meta = new HashMap<String, String>();
		meta.put(key, value);
	}

	public Map<String, String> getMeta() {
		return meta;
	}
	
	public void setMeta(Map<String, String> meta) {
		this.meta = meta;
	}

	public JSONObject toJSON() throws JSONException {
		JSONObject json = new JSONObject();
		json.put("name", this.name);
		
		addNetworks(json, "networks");
		addMeta(json);
		return json;
	}

	private void addMeta(JSONObject json) throws JSONException {
		if(this.meta != null){
			json.put("meta", meta);
		}
	}

	private void addNetworks(JSONObject json, String propName) 
			throws JSONException {
		if (this.networks != null){
			JSONArray networks = new JSONArray();
			json.put(propName, networks);
			for(UpNetworkInterface ni : this.networks){
				networks.put(ni.toJSON());
			}
		}
	}

	public static UpDevice fromJSON(JSONObject json) throws JSONException {
		UpDevice device = new UpDevice();
		device.name = json.optString("name", null);

		device.networks = fromNetworks(json, "networks");
		
		device.meta = fromMeta(json);
		return device;
	}

	private static Map<String, String> fromMeta(JSONObject json)
			throws JSONException {
		JSONObject j_meta = json.optJSONObject("meta");
		Map<String,String> meta = null;
		if(j_meta != null){
			meta = (Map)j_meta.toMap();
		}
		return meta;
	}

	private static List<UpNetworkInterface> fromNetworks(JSONObject json, String propName)
			throws JSONException {
		JSONArray j_networks = json.optJSONArray(propName);
		if(j_networks != null){
			List<UpNetworkInterface> networks = new ArrayList<UpNetworkInterface>(); 
			for(int i = 0; i < j_networks.length(); i++){
				networks.add(
					UpNetworkInterface.fromJSON(j_networks.getJSONObject(i))
				);
			}
			return networks;
		}
		return null;
	}

}
