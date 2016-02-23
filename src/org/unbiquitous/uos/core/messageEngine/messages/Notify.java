package org.unbiquitous.uos.core.messageEngine.messages;

import static org.unbiquitous.uos.core.ClassLoaderUtils.chainHashCode;
import static org.unbiquitous.uos.core.ClassLoaderUtils.compare;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Notify extends Message {
	// Ensuring JSON field names, in case these properties ever change for any
	// reason...
	@JsonProperty(value = "eventKey")
	@JsonInclude(Include.NON_NULL)
	private String eventKey;
	
	@JsonProperty(value = "driver")
	@JsonInclude(Include.NON_NULL)
	private String driver;
	
	@JsonProperty(value = "instanceId")
	@JsonInclude(Include.NON_NULL)
	private String instanceId;
	
	@JsonProperty(value = "parameters")
	@JsonInclude(Include.NON_NULL)
	private Map<String, Object> parameters;

	public Notify() {
		setType(Message.Type.NOTIFY);
	}

	public Notify(String eventKey) {
		this();
		this.eventKey = eventKey;
	}

	public Notify(String eventKey, String driver) {
		this(eventKey);
		this.driver = driver;
	}

	public Notify(String eventKey, String driver, String instanceId) {
		this(eventKey, driver);
		this.instanceId = instanceId;
	}

	/**
	 * @return the eventKey
	 */
	public String getEventKey() {
		return eventKey;
	}

	/**
	 * @param eventKey
	 *            the eventKey to set
	 */
	public void setEventKey(String eventKey) {
		this.eventKey = eventKey;
	}

	/**
	 * @return the parameters
	 */
	public Map<String, Object> getParameters() {
		return parameters;
	}

	/**
	 * @param parameters
	 *            the parameters to set
	 */
	public void setParameters(Map<String, Object> parameters) {
		this.parameters = parameters;
	}

	public Notify addParameter(String key, String value) {
		return this.addParameter(key, (Object) value);
	}

	public Notify addParameter(String key, Number value) {
		return this.addParameter(key, (Object) value);
	}

	public Notify addParameter(String key, Object value) {
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

	/**
	 * @return the driver
	 */
	public String getDriver() {
		return driver;
	}

	/**
	 * @param driver
	 *            the driver to set
	 */
	public void setDriver(String driver) {
		this.driver = driver;
	}

	/**
	 * @return the instanceId
	 */
	public String getInstanceId() {
		return instanceId;
	}

	/**
	 * @param instanceId
	 *            the instanceId to set
	 */
	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}

	@Override
	public boolean equals(Object obj) {
		if (super.equals(obj)) {
			if (!(obj instanceof Notify))
				return false;
			Notify other = (Notify) obj;

			if (!compare(this.eventKey, other.eventKey))
				return false;
			if (!compare(this.driver, other.driver))
				return false;
			if (!compare(this.instanceId, other.instanceId))
				return false;
			if (!compare(this.parameters, other.parameters))
				return false;

			return true;
		}
		return false;
	}

	@Override
	public int hashCode() {
		int hash = chainHashCode(super.hashCode(), this.eventKey);
		hash = chainHashCode(hash, this.driver);
		hash = chainHashCode(hash, this.instanceId);
		hash = chainHashCode(hash, this.parameters);
		return hash;
	}
}
