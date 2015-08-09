package org.unbiquitous.uos.core.messageEngine.messages;

import static org.unbiquitous.uos.core.ClassLoaderUtils.chainHashCode;
import static org.unbiquitous.uos.core.ClassLoaderUtils.compare;

import java.util.HashMap;
import java.util.Map;

import org.unbiquitous.uos.core.applicationManager.CallContext;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Response extends Message {
	// Ensuring JSON field names, in case these properties ever change for any
	// reason...
	@JsonProperty(value = "responseData")
	@JsonInclude(Include.NON_NULL)
	private Map<String, Object> responseData;

	@JsonIgnore
	private CallContext messageContext;

	public Response() {
		setType(Message.Type.SERVICE_CALL_RESPONSE);
	}

	public Map<String, Object> getResponseData() {
		return responseData;
	}

	public Object getResponseData(String key) {
		if (responseData != null)
			return responseData.get(key);
		else
			return null;
	}

	public String getResponseString(String key) {
		return (String) getResponseData(key);
	}

	public void setResponseData(Map<String, Object> responseData) {
		this.responseData = responseData;
	}

	public Response addParameter(String key, Object value) {
		if (responseData == null) {
			responseData = new HashMap<String, Object>();
		}
		responseData.put(key, value);
		return this;
	}

	public CallContext getMessageContext() {
		return messageContext;
	}

	public void setMessageContext(CallContext messageContext) {
		this.messageContext = messageContext;
	}

	@Override
	public boolean equals(Object obj) {
		if (super.equals(obj)) {
			if (!(obj instanceof Response))
				return false;
			Response other = (Response) obj;

			return compare(this.responseData, other.responseData);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return chainHashCode(super.hashCode(), this.responseData);
	}
}
