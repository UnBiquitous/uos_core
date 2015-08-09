package org.unbiquitous.uos.core.messageEngine.messages;

import static org.unbiquitous.uos.core.ClassLoaderUtils.chainHashCode;
import static org.unbiquitous.uos.core.ClassLoaderUtils.compare;

import java.io.IOException;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Capsule extends Message {
	private static final ObjectMapper mapper = new ObjectMapper();

	// Ensuring JSON field names, in case these properties ever change for any
	// reason...
	@JsonProperty(value = "innerMessage")
	@JsonInclude(Include.NON_NULL)
	private String innerMessage;

	@JsonProperty(value = "securityType")
	@JsonInclude(Include.NON_NULL)
	private String securityType;

	public Capsule() {
		setType(Type.ENCAPSULATED_MESSAGE);
	}

	public Capsule(String securityType, String innerMessage) {
		this();
		this.securityType = securityType;
		this.innerMessage = innerMessage;
	}

	public String getInnerMessage() {
		return innerMessage;
	}

	public void setInnerMessage(String innerMessage) {
		this.innerMessage = innerMessage;
	}

	public String getSecurityType() {
		return securityType;
	}

	public void setSecurityType(String securityType) {
		this.securityType = securityType;
	}

	@Override
	public int hashCode() {
		int hash = chainHashCode(0, innerMessage);
		hash = chainHashCode(hash, securityType);
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (!(obj instanceof Capsule))
			return false;
		Capsule other = (Capsule) obj;

		return compare(this.innerMessage, other.innerMessage) && compare(this.securityType, other.securityType);
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
