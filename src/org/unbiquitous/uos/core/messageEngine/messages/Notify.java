package org.unbiquitous.uos.core.messageEngine.messages;

import static org.unbiquitous.uos.core.ClassLoaderUtils.compare;

import java.util.HashMap;
import java.util.Map;

import org.unbiquitous.json.JSONException;
import org.unbiquitous.json.JSONObject;

public class Notify extends Message {

	private String eventKey;
	
	private String driver;
	
	private String instanceId;
	
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
	 * @param eventKey the eventKey to set
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
	 * @param parameters the parameters to set
	 */
	public void setParameters(Map<String, Object> parameters) {
		this.parameters = parameters;
	}
	
	public Notify addParameter(String key, String value){
		return this.addParameter(key, (Object)value);
	}
	
	public Notify addParameter(String key, Number value){
		return this.addParameter(key, (Object)value);
	}
	
	private Notify addParameter(String key, Object value){
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
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null){
			return false;
		}
		if (!( obj instanceof Notify)){
			return false;
		}
		Notify temp = (Notify) obj; 
		
		if(!compare(this.eventKey,temp.eventKey)) return false;
		if(!compare(this.driver,temp.driver)) return false;
		if(!compare(this.instanceId,temp.instanceId)) return false;
		if(!compare(this.parameters,temp.parameters)) return false;
		
		return true;
	}
	
	@Override
	public int hashCode() {
		int hash = 0;
		if (this.eventKey != null){
			hash += this.eventKey.hashCode();
		}
		if (this.driver != null){
			hash += this.driver.hashCode();
		}
		if (this.instanceId != null){
			hash += this.instanceId.hashCode();
		}
		
		return hash;
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

	public JSONObject toJSON() throws JSONException {
		JSONObject json = super.toJSON();
		
		json.put("eventKey",this.eventKey);
		json.put("driver",this.driver);
		json.put("instanceId",this.instanceId);
		if (this.parameters != null)
			json.put("parameters",this.parameters);
		return json;
	}

	public static Notify fromJSON(JSONObject json) throws JSONException {
		Notify e = new Notify();
		
		Message.fromJSON(e, json);
		
		e.setEventKey(json.optString("eventKey",null));
		e.setDriver(json.optString("driver",null));
		e.setInstanceId(json.optString("instanceId",null));
		if(json.has("parameters")){
			e.parameters = json.optJSONObject("parameters").toMap();
		}
		return e;
	}
	
	@Override
	public String toString() {
		try {
			return toJSON().toString();
		} catch (JSONException e) {
			return super.toString();
		}
	}
}
