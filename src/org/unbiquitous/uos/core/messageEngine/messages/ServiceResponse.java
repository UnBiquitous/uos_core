package org.unbiquitous.uos.core.messageEngine.messages;

import java.util.HashMap;
import java.util.Map;

import org.unbiquitous.uos.core.applicationManager.UOSMessageContext;


public class ServiceResponse extends Message{
	
	private Map<String,Object> responseData;
	
	private UOSMessageContext messageContext;
	
	public ServiceResponse() {
		setType(Message.Type.SERVICE_CALL_RESPONSE);
	}

	public Map<String,Object> getResponseData() {
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

	public void setResponseData(Map<String,Object> responseData) {
		this.responseData = responseData;
	}
	
	public ServiceResponse addParameter(String key, Object value){
		if (responseData == null){
			responseData = new HashMap<String, Object>();
		}
		responseData.put(key, value);
		return this;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null){
			return false;
		}
		if (!( obj instanceof ServiceResponse)){
			return false;
		}
		ServiceResponse temp = (ServiceResponse) obj; 
		
		if (	!( this.responseData == temp.responseData || (this.responseData != null && this.responseData.equals(temp.responseData)))){
			return false;
		}
		
		return true;
	}
	
	@Override
	public int hashCode() {
		int hash = 0;
		if (this.responseData != null){
			hash += this.responseData.hashCode();
		}
			
		if (hash != 0){
			return hash;
		}
		
		return super.hashCode();
	}

	public UOSMessageContext getMessageContext() {
		return messageContext;
	}

	public void setMessageContext(UOSMessageContext messageContext) {
		this.messageContext = messageContext;
	}
}
