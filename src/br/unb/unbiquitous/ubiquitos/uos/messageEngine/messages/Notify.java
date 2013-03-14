package br.unb.unbiquitous.ubiquitos.uos.messageEngine.messages;

import java.util.HashMap;
import java.util.Map;

public class Notify extends Message {

	private String eventKey;
	
	private String driver;
	
	private String instanceId;
	
	private Map<String, String> parameters;
	
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
	 * @param eventKey the eventKey to set
	 */
	public void setEventKey(String eventKey) {
		this.eventKey = eventKey;
	}

	/**
	 * @return the parameters
	 */
	public Map<String, String> getParameters() {
		return parameters;
	}

	/**
	 * @param parameters the parameters to set
	 */
	public void setParameters(Map<String, String> parameters) {
		this.parameters = parameters;
	}
	
	public Notify addParameter(String key, String value){
		if (parameters == null){
			parameters = new HashMap<String, String>();
		}
		parameters.put(key, value);
		return this;
	}
	
	public String getParameter(String key){
		if (parameters != null){
			return parameters.get(key);
		}
		return null;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null){
			return false;
		}
		if (!( obj instanceof Notify)){
			return false;
		}
		Notify temp = (Notify) obj; 
		
		if (	!( this.eventKey == temp.eventKey || (this.eventKey != null && this.eventKey.equals(temp.eventKey)))){
			return false;
		}
		if (	!( this.parameters == temp.parameters || (this.parameters != null && this.parameters.equals(temp.parameters)))){
			return false;
		}
		
		return true;
	}
	
	@Override
	public int hashCode() {
		int hash = 0;
		if (this.eventKey != null){
			hash += this.eventKey.hashCode();
		}
		if (this.parameters != null){
			hash += this.parameters.hashCode();
		}
			
		if (hash != 0){
			return hash;
		}
		
		return super.hashCode();
	}

	/**
	 * @return the driver
	 */
	public String getDriver() {
		return driver;
	}

	/**
	 * @param driver the driver to set
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
	 * @param instanceId the instanceId to set
	 */
	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}
}
