package org.unbiquitous.uos.core.messageEngine.messages;

import java.util.HashMap;
import java.util.Map;

public class ServiceCall extends Message {
	
	/** enum to specify the type of data transmission from the called service*/
	public enum ServiceType {DISCRETE, STREAM};
	
	private String driver;
	
	private String service;
	
	private Map<String,Object> parameters;
	
	private String instanceId;
	
	private ServiceType serviceType;
	
	private int channels;
	
	private String[] channelIDs;
	
	private String channelType;
	
	private String securityType;

	public ServiceCall() {
		setType(Message.Type.SERVICE_CALL_REQUEST);
		setServiceType(ServiceType.DISCRETE);
		setChannels(1);
	}
	
	public ServiceCall(String driver, String service){
		this();
		this.driver = driver;
		this.service = service;
	}
	
	public ServiceCall(String driver, String service, String instanceId){
		this(driver,service);
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

	public Map<String,Object> getParameters() {
		return parameters;
	}

	public void setParameters(Map<String,Object> parameters) {
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

	public ServiceCall addParameter(String key, Object value){
		if (parameters == null){
			parameters = new HashMap<String, Object>();
		}
		parameters.put(key, value);
		return this;
	}
	
	public Object getParameter(String key){
		if (parameters != null){
			return parameters.get(key);
		}
		return null;
	}
	public String getParameterString(String key){
		return (String) getParameter(key);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null){
			return false;
		}
		if (!( obj instanceof ServiceCall)){
			return false;
		}
		ServiceCall temp = (ServiceCall) obj; 
		if (	!( this.driver == temp.driver || (this.driver != null && this.driver.equals(temp.driver)))){
			return false;
		}
		if (	!( this.instanceId == temp.instanceId || (this.instanceId != null && this.instanceId.equals(temp.instanceId)))){
			return false;
		}
		if (	!( this.parameters == temp.parameters || (this.parameters != null && this.parameters.equals(temp.parameters)))){
			return false;
		}
		if (	!( this.service == temp.service || (this.service != null && this.service.equals(temp.service)))){
			return false;
		}
		
		return true;
	}
	
	@Override
	public int hashCode() {
		int hash = 0;
		if (this.driver != null){
			hash += this.driver.hashCode();
		}
		if (this.instanceId != null){
			hash += this.instanceId.hashCode();
		}
		if (this.parameters != null){
			hash += this.parameters.hashCode();
		}
		if (this.service != null){
			hash += this.service.hashCode();
		}
			
		if (hash != 0){
			return hash;
		}
		
		return super.hashCode();
	}

	/**
	 * @return the securityType
	 */
	public String getSecurityType() {
		return securityType;
	}

	/**
	 * @param securityType the securityType to set
	 */
	public void setSecurityType(String securityType) {
		this.securityType = securityType;
	}
}
