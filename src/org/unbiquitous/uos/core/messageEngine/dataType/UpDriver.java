package org.unbiquitous.uos.core.messageEngine.dataType;

import java.util.ArrayList;
import java.util.List;

import org.unbiquitous.json.JSONArray;
import org.unbiquitous.json.JSONException;
import org.unbiquitous.json.JSONObject;

public class UpDriver {

	private String name;
	
	private List<UpService> services;
	
	private List<UpService> events;
	
	private List<String> equivalentDrivers;
	
	public UpDriver() {}
	
	public UpDriver(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<UpService> getServices() {
		return services;
	}

	public void setServices(List<UpService> services) {
		this.services = services;
	}
	
	public UpService addService(UpService service){
		if (services == null){
			services = new ArrayList<UpService>();
		}
		services.add(service);
		return service;
	}
	
	public UpService addService(String serviceName){
		return addService(new UpService(serviceName));
	}
	
	public List<String> getEquivalentDrivers() {
		return equivalentDrivers;
	}
	
	public void setEquivalentDrivers(List<String> equivalentDrivers) {
		this.equivalentDrivers = equivalentDrivers;
	}
	
	public List<String> addEquivalentDrivers(String driver) {
		if(equivalentDrivers == null) {
			equivalentDrivers = new ArrayList<String>();
		}
		equivalentDrivers.add(driver);
		return equivalentDrivers;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null || ! (obj instanceof UpDriver) ) return false;
		
		UpDriver d = (UpDriver) obj;
		if (this == d) return true;
		else {
			if ((this.name != null && !this.name.equals(d.name)) || (this.name == null && d.name != null))
				return false;
			else if (this.services != null && !this.services.equals(d.services) || ((this.services == null) && (d.services != null)))
				return false;
			else if (this.events != null && !this.events.equals(d.events) || ((this.events == null) && (d.events != null)))
				return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		if(name != null){
			return name.hashCode();
		}
		return super.hashCode();
	}
	
	public UpDriver addEvent(UpService event){
		if (events == null){
			events = new ArrayList<UpService>();
		}
		events.add(event);
		return this;
	}
	
	public UpService addEvent(String event){
		if (events == null){
			events = new ArrayList<UpService>();
		}
		
		UpService sEvent = new UpService(event);
		events.add(sEvent);
		return sEvent;
	}
	
	/**
	 * @return the events
	 */
	public List<UpService> getEvents() {
		return events;
	}

	/**
	 * @param events the events to set
	 */
	public void setEvents(List<UpService> events) {
		this.events = events;
	}

	public JSONObject toJSON() throws JSONException {
		JSONObject json = new JSONObject();
		json.put("name", this.getName());
		
		addServices(json, "services", this.services);
		addServices(json, "events", this.events);
		addStrings(json, "equivalent_drivers", equivalentDrivers);
		
		return json;
	}

	private void addStrings(	JSONObject json, 
										String propName, List<String> stringList) 
			throws JSONException {
		if (stringList != null){
			JSONArray equivalent_drivers = new JSONArray();
			json.put(propName,equivalent_drivers);
			for(String eq: equivalentDrivers){
				equivalent_drivers.put(eq);
			}
		}
	}

	private void addServices(JSONObject json, 
							String propName, List<UpService> serviceList) 
			throws JSONException {
		if (serviceList != null){
			JSONArray services = new JSONArray();
			json.put(propName,services);
			for(UpService s : serviceList){
				services.put(s.toJSON());
			}
		}
	}

	public static UpDriver fromJSON(JSONObject json) throws JSONException {
		UpDriver d = new UpDriver(json.optString("name",null));
		
		d.services = addServices(json, "services");
		d.events = addServices(json, "events");
		d.equivalentDrivers = addStrings(json, "equivalent_drivers");
		
		return d;
	}

	private static List<String> addStrings(JSONObject json, String propName)
			throws JSONException {
		JSONArray jsonArray = json.optJSONArray(propName);
		if(jsonArray != null){
			List<String> strings = new ArrayList<String>();
			for( int i = 0 ; i < jsonArray.length(); i++){
				strings.add( jsonArray.getString(i));
			}
			return strings;
		}
		return null;
	}

	private static List<UpService> addServices(JSONObject json, String propName)
			throws JSONException {
		JSONArray jsonArray = json.optJSONArray(propName);
		if (jsonArray != null){
			List<UpService> services = new ArrayList<UpService>();
			for( int i = 0 ; i < jsonArray.length(); i++){
				services.add(UpService.fromJSON(jsonArray.getJSONObject(i)));
			}
			return services;
		}
		return null;
	}

}
