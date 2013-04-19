package org.unbiquitous.uos.core.adaptabitilyEngine;

import java.util.List;
import java.util.Map;

import org.unbiquitous.uos.core.SecurityManager;
import org.unbiquitous.uos.core.applicationManager.ApplicationDeployer;
import org.unbiquitous.uos.core.connectivity.ConnectivityManager;
import org.unbiquitous.uos.core.deviceManager.DeviceManager;
import org.unbiquitous.uos.core.driverManager.DriverData;
import org.unbiquitous.uos.core.driverManager.DriverManager;
import org.unbiquitous.uos.core.messageEngine.dataType.UpDevice;
import org.unbiquitous.uos.core.messageEngine.messages.Notify;
import org.unbiquitous.uos.core.messageEngine.messages.ServiceCall;
import org.unbiquitous.uos.core.messageEngine.messages.ServiceResponse;
import org.unbiquitous.uos.core.ontologyEngine.Ontology;
import org.unbiquitous.uos.core.ontologyEngine.api.StartReasoner;

public class SmartSpaceGateway implements Gateway {

	private AdaptabilityEngine adaptabilityEngine;
	private UpDevice currentDevice;

	// These properties are used by advanced parts of the middleware
	private SecurityManager securityManager;
	private ConnectivityManager connectivityManager;
	private DeviceManager deviceManager;
	private DriverManager driverManager;
	private ApplicationDeployer applicationDeployer;
	private Ontology ontology;

	public void init(AdaptabilityEngine adaptabilityEngine,
			UpDevice currentDevice, SecurityManager securityManager,
			ConnectivityManager connectivityManager,
			DeviceManager deviceManager, DriverManager driverManager,
			ApplicationDeployer applicationDeployer, Ontology ontology) {
		this.adaptabilityEngine = adaptabilityEngine;
		this.currentDevice = currentDevice;
		this.securityManager = securityManager;
		this.connectivityManager = connectivityManager;
		this.deviceManager = deviceManager;
		this.driverManager = driverManager;
		this.applicationDeployer = applicationDeployer;
		this.ontology = ontology;
	}

	public ServiceResponse callService(UpDevice device, String serviceName,
			String driverName, String instanceId, String securityType,
			Map<String, String> parameters) throws ServiceCallException {

		return adaptabilityEngine.callService(device, serviceName, driverName,
				instanceId, securityType, parameters);
	}

	public ServiceResponse callService(UpDevice device, ServiceCall serviceCall)
			throws ServiceCallException {
		return adaptabilityEngine.callService(device, serviceCall);
	}

	public void registerForEvent(UosEventListener listener, UpDevice device,
			String driver, String eventKey) throws NotifyException {
		adaptabilityEngine.registerForEvent(listener, device, driver, null,
				eventKey);
	}

	public void registerForEvent(UosEventListener listener, UpDevice device,
			String driver, String instanceId, String eventKey)
			throws NotifyException {
		adaptabilityEngine.registerForEvent(listener, device, driver,
				instanceId, eventKey);
	}

	public List<DriverData> listDrivers(String driverName) {
		return driverManager.listDrivers(driverName, null);
	}

	public List<UpDevice> listDevices() {
		return deviceManager.listDevices();
	}
	
	public UpDevice getCurrentDevice() {
		return currentDevice;
	}

	public void sendEventNotify(Notify notify, UpDevice device)
			throws NotifyException {
		adaptabilityEngine.sendEventNotify(notify, device);
	}

	public void unregisterForEvent(UosEventListener listener)
			throws NotifyException {
		adaptabilityEngine.unregisterForEvent(listener);
	}

	public void unregisterForEvent(UosEventListener listener, UpDevice device,
			String driver, String instanceId, String eventKey)
			throws NotifyException {
		adaptabilityEngine.unregisterForEvent(listener, device, driver,
				instanceId, eventKey);
	}

	public SecurityManager getSecurityManager() {
		return securityManager;
	}

	public ConnectivityManager getConnectivityManager() {
		return connectivityManager;
	}

	public DeviceManager getDeviceManager() {
		return deviceManager;
	}

	public DriverManager getDriverManager() {
		return driverManager;
	}

	public ApplicationDeployer getApplicationDeployer() {
		return applicationDeployer;
	}

	public StartReasoner getOntologyReasoner() {
		if (ontology != null) {
			return ontology.getOntologyReasoner();
		}
		return null;
	}

	public Ontology getOntology() {
		return ontology;
	}

}
