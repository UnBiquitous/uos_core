package org.unbiquitous.uos.core.messageEngine.dataType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.unbiquitous.json.JSONException;
import org.unbiquitous.uos.core.messageEngine.dataType.json.JSONDevice;

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
		
		return this.name == d.name || this.name.equals(d.name);
	}
	
	@Override
	public String toString() {
		try {
			JSONDevice json = new JSONDevice(this);
			return json.toString();
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
}
