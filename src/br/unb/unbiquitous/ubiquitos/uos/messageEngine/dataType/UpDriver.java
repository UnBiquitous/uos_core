package br.unb.unbiquitous.ubiquitos.uos.messageEngine.dataType;

import java.util.ArrayList;
import java.util.List;

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
	
}
