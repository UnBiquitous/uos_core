package org.unbiquitous.uos.core.messageEngine.dataType;

import static org.unbiquitous.uos.core.ClassLoaderUtils.chainHashCode;
import static org.unbiquitous.uos.core.ClassLoaderUtils.compare;

import java.io.IOException;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;

public class UpNetworkInterface {
	private static final ObjectMapper mapper = new ObjectMapper();

	// Ensuring JSON field names, in case these properties ever change for any
	// reason...
	@JsonProperty(value = "netType")
	@JsonInclude(Include.NON_NULL)
	private String netType;
	
	@JsonProperty(value = "networkAddress")
	@JsonInclude(Include.NON_NULL)
	private String networkAddress;

	public UpNetworkInterface() {
	}

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
	public int hashCode() {
		int hash = chainHashCode(0, networkAddress);
		hash = chainHashCode(hash, netType);
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (!(obj instanceof UpNetworkInterface))
			return false;
		UpNetworkInterface other = (UpNetworkInterface) obj;

		return compare(this.networkAddress, other.networkAddress) && compare(this.netType, other.netType);
	}

	@Override
	public String toString() {
		try {
			return mapper.writeValueAsString(this);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
