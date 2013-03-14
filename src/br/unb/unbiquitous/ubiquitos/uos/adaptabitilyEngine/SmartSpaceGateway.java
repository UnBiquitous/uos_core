package br.unb.unbiquitous.ubiquitos.uos.adaptabitilyEngine;

import java.util.List;
import java.util.Map;

import br.unb.unbiquitous.ubiquitos.uos.connectivity.ConnectivityManager;
import br.unb.unbiquitous.ubiquitos.uos.context.ApplicationDeployer;
import br.unb.unbiquitous.ubiquitos.uos.deviceManager.DeviceManager;
import br.unb.unbiquitous.ubiquitos.uos.driverManager.DriverData;
import br.unb.unbiquitous.ubiquitos.uos.driverManager.DriverManager;
import br.unb.unbiquitous.ubiquitos.uos.messageEngine.dataType.UpDevice;
import br.unb.unbiquitous.ubiquitos.uos.messageEngine.messages.Notify;
import br.unb.unbiquitous.ubiquitos.uos.messageEngine.messages.ServiceCall;
import br.unb.unbiquitous.ubiquitos.uos.messageEngine.messages.ServiceResponse;
import br.unb.unbiquitous.ubiquitos.uos.ontologyEngine.Ontology;
import br.unb.unbiquitous.ubiquitos.uos.ontologyEngine.api.StartReasoner;
import br.unb.unbiquitous.ubiquitos.uos.security.SecurityManager;

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

	@Override
	public ServiceResponse callService(UpDevice device, String serviceName,
			String driverName, String instanceId, String securityType,
			Map<String, String> parameters) throws ServiceCallException {

		return adaptabilityEngine.callService(device, serviceName, driverName,
				instanceId, securityType, parameters);
	}

	@Override
	public ServiceResponse callService(UpDevice device, ServiceCall serviceCall)
			throws ServiceCallException {
		return adaptabilityEngine.callService(device, serviceCall);
	}

	@Override
	public void registerForEvent(UosEventListener listener, UpDevice device,
			String driver, String eventKey) throws NotifyException {
		adaptabilityEngine.registerForEvent(listener, device, driver, null,
				eventKey);
	}

	@Override
	public void registerForEvent(UosEventListener listener, UpDevice device,
			String driver, String instanceId, String eventKey)
			throws NotifyException {
		adaptabilityEngine.registerForEvent(listener, device, driver,
				instanceId, eventKey);
	}

	@Override
	public List<DriverData> listDrivers(String driverName) {
		return driverManager.listDrivers(driverName, null);
	}

	/**
	 * @see br.unb.unbiquitous.ubiquitos.uos.adaptabitilyEngine.Gateway#getCurrentDevice()
	 */
	public UpDevice getCurrentDevice() {
		return currentDevice;
	}

	/**
	 * @see br.unb.unbiquitous.ubiquitos.uos.adaptabitilyEngine.Gateway#sendEventNotify(Notify,
	 *      UpDevice)
	 */
	public void sendEventNotify(Notify notify, UpDevice device)
			throws NotifyException {
		adaptabilityEngine.sendEventNotify(notify, device);
	}

	/**
	 * @see br.unb.unbiquitous.ubiquitos.uos.adaptabitilyEngine.Gateway#unregisterForEvent(UosEventListener)
	 */
	public void unregisterForEvent(UosEventListener listener)
			throws NotifyException {
		adaptabilityEngine.unregisterForEvent(listener);
	}

	/**
	 * @see br.unb.unbiquitous.ubiquitos.uos.adaptabitilyEngine.Gateway#unregisterForEvent(UosEventListener,
	 *      UpDevice, String, String, String)
	 */
	public void unregisterForEvent(UosEventListener listener, UpDevice device,
			String driver, String instanceId, String eventKey)
			throws NotifyException {
		adaptabilityEngine.unregisterForEvent(listener, device, driver,
				instanceId, eventKey);
	}

	/**
	 * @return The current SecurityManager available in the middleware instance.
	 */
	public SecurityManager getSecurityManager() {
		return securityManager;
	}

	/**
	 * @return The ConnectivityManager used by the middleware for managing the
	 *         network layer.
	 */
	public ConnectivityManager getConnectivityManager() {
		return connectivityManager;
	}

	/**
	 * @return The DeviceManager responsible for the device database on this
	 *         instance.
	 */
	public DeviceManager getDeviceManager() {
		return deviceManager;
	}

	/**
	 * @return The DriverManager responsible for the driver database on this
	 *         instance.
	 */
	public DriverManager getDriverManager() {
		return driverManager;
	}

	/**
	 * @return The ApplicationDeployer responsible for the applications running
	 *         on this instance.
	 */
	public ApplicationDeployer getApplicationDeployer() {
		return applicationDeployer;
	}

	/**
	 * @return The OntologyReasoner responsible for returning information from
	 *         the ontology.
	 */
	public StartReasoner getOntologyReasoner() {
            if(ontology!=null){
                
		return ontology.getOntologyReasoner();
            }
        
            return null;
	}
        
        public Ontology getOntology(){
            return ontology;
        }
}
