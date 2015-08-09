package org.unbiquitous.uos.core.messageEngine.messages;

import static org.unbiquitous.uos.core.ClassLoaderUtils.chainHashCode;
import static org.unbiquitous.uos.core.ClassLoaderUtils.compare;

import java.io.IOException;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Message {

	public enum Type {
		SERVICE_CALL_REQUEST, SERVICE_CALL_RESPONSE, NOTIFY, ENCAPSULATED_MESSAGE
	};

	private static final ObjectMapper mapper = new ObjectMapper();

	// Ensuring JSON field names, in case these properties ever change for any
	// reason...
	@JsonProperty(value = "type")
	@JsonInclude(Include.NON_NULL)
	private Type type;
	
	@JsonProperty(value = "error")
	@JsonInclude(Include.NON_NULL)
	private String error;

	public Message() {
	}

	public Message(Type type) {
		this.type = type;
	}

	public Type getType() {
		return type;
	}

	protected void setType(Type type) {
		this.type = type;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	@Override
	public int hashCode() {
		int hash = chainHashCode(0, type);
		hash = chainHashCode(hash, error);
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (!(obj instanceof Message))
			return false;
		Message other = (Message) obj;

		return compare(type, other.type) && compare(error, other.error);
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
