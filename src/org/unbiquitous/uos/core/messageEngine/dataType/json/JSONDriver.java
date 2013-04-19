package org.unbiquitous.uos.core.messageEngine.dataType.json;

import java.util.ArrayList;
import java.util.List;

import org.unbiquitous.json.JSONArray;
import org.unbiquitous.json.JSONException;
import org.unbiquitous.json.JSONObject;
import org.unbiquitous.uos.core.messageEngine.dataType.UpDriver;
import org.unbiquitous.uos.core.messageEngine.dataType.UpService;

public class JSONDriver extends JSONObject {
	private static final String PROP_SERVICES = "services";
	private static final String PROP_EVENTS = "events";
	private static final String PROP_EQUIVALENT_DRIVERS = "equivalent_drivers";
	private static final String PROP_NAME = "name";

	public JSONDriver(String source) throws JSONException {
		super(source);
	}



	public JSONDriver(UpDriver bean) throws JSONException {
		this.put(PROP_NAME,bean.getName());
		
		//Set<Service> services;
		if (bean.getServices() != null && !bean.getServices().isEmpty()){
			for (UpService d : bean.getServices()){
				this.append(PROP_SERVICES,new JSONService(d));
			}
		}else{
			this.put(PROP_SERVICES, (Object)null);
		}
		//Set<String> events;
		if (bean.getEvents() != null && !bean.getEvents().isEmpty()){
			for (UpService s : bean.getEvents()){
				this.append(PROP_EVENTS,new JSONService(s));
			}
		}else{
			this.put(PROP_EVENTS, (Object)null);
		}
		//Set<String> equivalent_drivers
		if (bean.getEquivalentDrivers() != null && !bean.getEquivalentDrivers().isEmpty()){
			for (String equivalentDriver : bean.getEquivalentDrivers()){
				this.append(PROP_EQUIVALENT_DRIVERS, equivalentDriver);
			}
		}else{
			this.put(PROP_EQUIVALENT_DRIVERS, (Object)null);
		}
	}
	
	public UpDriver getAsObject() throws JSONException{
		UpDriver driver = new UpDriver();
		
		driver.setName(this.getString(PROP_NAME));
		
		//Set<Service> services;
		if (this.get(PROP_SERVICES) != null && this.get(PROP_SERVICES) instanceof JSONArray){
			List<UpService> services = new ArrayList<UpService>();
			JSONArray array = (JSONArray)this.get(PROP_SERVICES);
			for (int i = 0 ; i < array.length() ; i ++){
				Object o = array.get(i);
				JSONService jsonS = new JSONService(o.toString());
				services.add(jsonS.getAsObject());
			}
			driver.setServices(services);
		}
		//Set<String> events;
		if (this.opt(PROP_EVENTS) != null && this.opt(PROP_EVENTS) instanceof JSONArray){
			List<UpService> events = new ArrayList<UpService>();
			JSONArray array = (JSONArray)this.opt(PROP_EVENTS);
			for (int i = 0 ; i < array.length() ; i ++){
				Object o = array.get(i);
				JSONService jsonS = new JSONService(o.toString());
				events.add(jsonS.getAsObject());
			}
			driver.setEvents(events);
		}
		//Set<String> equivalent_drivers;
		if (this.opt(PROP_EQUIVALENT_DRIVERS) != null && this.opt(PROP_EQUIVALENT_DRIVERS) instanceof JSONArray){
			List<String> equivalentDrivers = new ArrayList<String>();
			JSONArray array = (JSONArray)this.opt(PROP_EQUIVALENT_DRIVERS);
			for (int i = 0 ; i < array.length() ; i ++){
				Object o = array.get(i);
				equivalentDrivers.add(o.toString());
			}
			driver.setEquivalentDrivers(equivalentDrivers);
		}
		
		return driver;
	}
}

