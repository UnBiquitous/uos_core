package org.unbiquitous.uos.core.messageEngine.dataType;

import static org.unbiquitous.uos.core.ClassLoaderUtils.chainHashCode;
import static org.unbiquitous.uos.core.ClassLoaderUtils.compare;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This class represents a device from the middleware view.
 * 
 * @author Fabricio Nogueira Buzeto
 *
 */
public class UpDevice {
	private static final ObjectMapper mapper = new ObjectMapper();

	// Ensuring JSON field names, in case these properties ever change for any
	// reason...
	@JsonProperty(value = "name")
	@JsonInclude(Include.NON_NULL)
	private String name;
	
	@JsonProperty(value = "networks")
	@JsonInclude(Include.NON_NULL)
	private List<UpNetworkInterface> networks;
	
	@JsonProperty(value = "meta")
	@JsonInclude(Include.NON_NULL)
	private Map<String, String> meta;

	public UpDevice() {
	}

	public UpDevice(String name) {
		this.name = name;
	}

	public UpDevice addNetworkInterface(String networkAdress, String networkType) {
		if (networks == null) {
			networks = new ArrayList<UpNetworkInterface>();
		}
		networks.add(new UpNetworkInterface(networkType, networkAdress));
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
		if (obj == this)
			return true;
		if (!(obj instanceof UpDevice))
			return false;
		UpDevice other = (UpDevice) obj;

		return compare(this.name, other.name)
				&& compare(this.networks, other.networks)
				&& compare(this.meta, other.meta);
	}

	@Override
	public int hashCode() {
		int hash = chainHashCode(0, getName());
		hash = chainHashCode(hash, getNetworks());
		hash = chainHashCode(hash, getMeta());
		return hash;
	}

	@Override
	public String toString() {
		try {
			return mapper.writeValueAsString(this);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Return meta properties about the device. Ex:
	 * 
	 * 'platform': The undelying platform like "Dalvik" or "Sun Java VM"
	 */
	public Object getProperty(String key) {
		if (meta == null)
			return null;
		return meta.get(key);
	}

	public void addProperty(String key, String value) {
		if (meta == null)
			meta = new HashMap<String, String>();
		meta.put(key, value);
	}

	public Map<String, String> getMeta() {
		return meta;
	}

	public void setMeta(Map<String, String> meta) {
		this.meta = meta;
	}
}
