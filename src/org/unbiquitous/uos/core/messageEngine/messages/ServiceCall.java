package org.unbiquitous.uos.core.messageEngine.messages;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.unbiquitous.json.JSONArray;
import org.unbiquitous.json.JSONException;
import org.unbiquitous.json.JSONObject;

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
		
		if(!compare(this.driver,temp.driver)) return false;
		if(!compare(this.service,temp.service)) return false;
		if(!compare(this.parameters,temp.parameters)) return false;
		if(!compare(this.instanceId,temp.instanceId)) return false;
		if(!compare(this.serviceType,temp.serviceType)) return false;
		if(!compare(this.channels,temp.channels)) return false;
		if(!compare(this.channelIDs,temp.channelIDs)) return false;
		if(!compare(this.channelType,temp.channelType)) return false;
		if(!compare(this.securityType,temp.securityType)) return false;
		
		return true;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	static boolean compare(Object a, Object b){
		if(a != null && b != null && 
				a.getClass().isArray() && b.getClass().isArray()){
			List _a = Arrays.asList((Object[])a);
			List _b = Arrays.asList((Object[])b);
			return a == b || (  _a.containsAll(_b) && _b.containsAll(_a) );
		}
		return a == b || (a != null && a.equals(b));
	}
	
	@Override
	public int hashCode() {
		if (this.driver != null && this.service != null){
			return this.driver.hashCode() + this.service.hashCode();
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

	public JSONObject toJSON() throws JSONException {
		JSONObject json = new JSONObject();
		
		json.put("type", getType().name());
		json.put("error", getError());
		
		json.put("driver", driver);
		json.put("service", service);
		if(parameters != null)
			json.put("parameters",parameters);
		json.put("instanceId",instanceId);
		json.put("serviceType",serviceType.name());
		json.put("channels",channels);
		json.put("channelIDs",channelIDs);
		json.put("channelType",channelType);
		json.put("securityType",securityType);
		
		return json;
	}

	@SuppressWarnings("unchecked")
	public static ServiceCall fromJSON(JSONObject json) throws JSONException {
		ServiceCall call = new ServiceCall();
		
		call.setError(json.optString("error",null));
		
		call.driver = json.optString("driver",null);
		call.service = json.optString("service",null);
		if(json.has("parameters")){
			call.parameters = json.optJSONObject("parameters").toMap();
		}
		call.instanceId = json.optString("instanceId",null);
		if(json.has("serviceType")){
			call.serviceType = ServiceType.valueOf(json.optString("serviceType"));
		}
		if(json.has("channels")){
			call.channels = json.optInt("channels");
		}
		if(json.has("channelIDs")){
			JSONArray jsonArray = json.getJSONArray("channelIDs");
			call.channelIDs = (String[]) jsonArray.toArray().toArray(new String[]{});
		}
		call.channelType = json.optString("channelType",null);
		call.securityType = json.optString("securityType",null);
		
		return call;
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
