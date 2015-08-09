package org.unbiquitous.uos.core.messageEngine.dataType;

import static org.unbiquitous.uos.core.ClassLoaderUtils.chainHashCode;
import static org.unbiquitous.uos.core.ClassLoaderUtils.compare;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;

public class UpDriver {
	private static final ObjectMapper mapper = new ObjectMapper();
	
	// Ensuring JSON field names, in case these properties ever change for any
	// reason...
	@JsonProperty(value = "name")
	@JsonInclude(Include.NON_NULL)
	private String name;
	
	@JsonProperty(value = "services")
	@JsonInclude(Include.NON_NULL)
	private List<UpService> services;
	
	@JsonProperty(value = "events")
	@JsonInclude(Include.NON_NULL)
	private List<UpService> events;
	
	@JsonProperty(value = "equivalent_drivers")
	@JsonInclude(Include.NON_NULL)
	private List<String> equivalentDrivers;

	public UpDriver() {
	}

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

	public UpService addService(UpService service) {
		if (services == null) {
			services = new ArrayList<UpService>();
		}
		services.add(service);
		return service;
	}

	public UpService addService(String serviceName) {
		return addService(new UpService(serviceName));
	}

	public List<String> getEquivalentDrivers() {
		return equivalentDrivers;
	}

	public void setEquivalentDrivers(List<String> equivalentDrivers) {
		this.equivalentDrivers = equivalentDrivers;
	}

	public List<String> addEquivalentDrivers(String driver) {
		if (equivalentDrivers == null) {
			equivalentDrivers = new ArrayList<String>();
		}
		equivalentDrivers.add(driver);
		return equivalentDrivers;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (!(obj instanceof UpDriver))
			return false;

		UpDriver other = (UpDriver) obj;

		return compare(getName(), other.getName()) && compare(getServices(), other.getServices())
				&& compare(getEvents(), other.getEvents());
	}

	@Override
	public int hashCode() {
		int hash = chainHashCode(0, getName());
		hash = chainHashCode(hash, getServices());
		hash = chainHashCode(hash, getEvents());
		return hash;
	}
	
	@Override
	public String toString() {
		try {
			return mapper.writeValueAsString(this);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public UpDriver addEvent(UpService event) {
		if (events == null) {
			events = new ArrayList<UpService>();
		}
		events.add(event);
		return this;
	}

	public UpService addEvent(String event) {
		if (events == null) {
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
	 * @param events
	 *            the events to set
	 */
	public void setEvents(List<UpService> events) {
		this.events = events;
	}
}
