package org.unbiquitous.uos.core.messageEngine.messages;

import static org.unbiquitous.uos.core.ClassLoaderUtils.chainHashCode;
import static org.unbiquitous.uos.core.ClassLoaderUtils.compare;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Call extends Message {

	/** enum to specify the type of data transmission from the called service */
	public enum ServiceType {
		DISCRETE, STREAM
	};

	private static final ObjectMapper mapper = new ObjectMapper();

	// Ensuring JSON field names, in case these properties ever change for any
	// reason...
	@JsonProperty(value = "driver")
	@JsonInclude(Include.NON_NULL)
	private String driver;
	
	@JsonProperty(value = "service")
	@JsonInclude(Include.NON_NULL)
	private String service;
	
	@JsonProperty(value = "parameters")
	@JsonInclude(Include.NON_NULL)
	private Map<String, Object> parameters;
	
	@JsonProperty(value = "instanceId")
	@JsonInclude(Include.NON_NULL)
	private String instanceId;
	
	@JsonProperty(value = "serviceType")
	@JsonInclude(Include.NON_NULL)
	private ServiceType serviceType;
	
	@JsonProperty(value = "channels")
	@JsonInclude(Include.NON_NULL)
	private int channels;
	
	@JsonProperty(value = "channelIDs")
	@JsonInclude(Include.NON_NULL)
	private String[] channelIDs;
	
	@JsonProperty(value = "channelType")
	@JsonInclude(Include.NON_NULL)
	private String channelType;
	
	@JsonProperty(value = "securityType")
	@JsonInclude(Include.NON_NULL)
	private String securityType;

	public Call() {
		setType(Message.Type.SERVICE_CALL_REQUEST);
		setServiceType(ServiceType.DISCRETE);
		setChannels(1);
	}

	public Call(String driver, String service) {
		this();
		this.driver = driver;
		this.service = service;
	}

	public Call(String driver, String service, String instanceId) {
		this(driver, service);
		this.instanceId = instanceId;
	}

	public String getDriver() {
		return driver;
	}

	public void setDriver(String driver) {
		this.driver = driver;
	}

	public String getService() {
		return service;
	}

	public void setService(String service) {
		this.service = service;
	}

	public Map<String, Object> getParameters() {
		return parameters;
	}

	public void setParameters(Map<String, Object> parameters) {
		this.parameters = parameters;
	}

	public String getInstanceId() {
		return instanceId;
	}

	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}

	public ServiceType getServiceType() {
		return serviceType;
	}

	public void setServiceType(ServiceType serviceType) {
		this.serviceType = serviceType;
	}

	public int getChannels() {
		return channels;
	}

	public void setChannels(int channels) {
		this.channels = channels;
	}

	public String[] getChannelIDs() {
		return channelIDs;
	}

	public void setChannelIDs(String[] channelIDs) {
		this.channelIDs = channelIDs;
	}

	public String getChannelType() {
		return channelType;
	}

	public void setChannelType(String channelType) {
		this.channelType = channelType;
	}

	public Call addParameter(String key, Object value) {
		if (parameters == null) {
			parameters = new HashMap<String, Object>();
		}
		parameters.put(key, value);
		return this;
	}

	public Object getParameter(String key) {
		if (parameters != null) {
			return parameters.get(key);
		}
		return null;
	}

	public String getParameterString(String key) {
		return (String) getParameter(key);
	}

	/**
	 * @return the securityType
	 */
	public String getSecurityType() {
		return securityType;
	}

	/**
	 * @param securityType
	 *            the securityType to set
	 */
	public void setSecurityType(String securityType) {
		this.securityType = securityType;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (!(obj instanceof Call))
			return false;
		Call other = (Call) obj;

		if (!compare(this.driver, other.driver))
			return false;
		if (!compare(this.service, other.service))
			return false;
		if (!compare(this.parameters, other.parameters))
			return false;
		if (!compare(this.instanceId, other.instanceId))
			return false;
		if (!compare(this.serviceType, other.serviceType))
			return false;
		if (!compare(this.channels, other.channels))
			return false;
		if (!compare(this.channelIDs, other.channelIDs))
			return false;
		if (!compare(this.channelType, other.channelType))
			return false;
		if (!compare(this.securityType, other.securityType))
			return false;

		return true;
	}

	@Override
	public int hashCode() {
		int hash = chainHashCode(0, this.driver);
		hash = chainHashCode(hash, this.service);
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
}
