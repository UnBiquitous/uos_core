package org.unbiquitous.uos.core.messageEngine.dataType;

import static org.unbiquitous.uos.core.ClassLoaderUtils.chainHashCode;
import static org.unbiquitous.uos.core.ClassLoaderUtils.compare;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

public class UpService {
	private static final ObjectMapper mapper = new ObjectMapper();

	public enum ParameterType {
		MANDATORY, OPTIONAL
	}

	// Ensuring JSON field names, in case these properties ever change for any
	// reason...
	@JsonProperty(value = "name")
	@JsonInclude(Include.NON_NULL)
	private String name;
	
	@JsonProperty(value = "parameters")
	@JsonInclude(Include.NON_NULL)
	private Map<String, ParameterType> parameters;

	public UpService() {
	}

	public UpService(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the parameters
	 */
	public Map<String, ParameterType> getParameters() {
		return parameters;
	}

	/**
	 * @param parameters
	 *            the parameters to set
	 */
	public void setParameters(Map<String, ParameterType> parameters) {
		this.parameters = parameters;
	}

	public UpService addParameter(String paramName, ParameterType paramType) {
		if (parameters == null) {
			parameters = new HashMap<String, ParameterType>();
		}

		parameters.put(paramName, paramType);
		return this;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (!(obj instanceof UpService))
			return false;

		UpService other = (UpService) obj;
		return compare(this.name, other.name) && compare(this.parameters, other.parameters);
	}

	@Override
	public int hashCode() {
		int hash = chainHashCode(0, this.name);
		hash = chainHashCode(hash, parameters);
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
