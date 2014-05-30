package org.unbiquitous.uos.core.connectivity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.unbiquitous.uos.core.UOSLogging;
import org.unbiquitous.uos.core.adaptabitilyEngine.SmartSpaceGateway;
import org.unbiquitous.uos.core.connectivity.proxying.ProxyDriver;
import org.unbiquitous.uos.core.connectivity.proxying.ProxyDriverImpl;
import org.unbiquitous.uos.core.driverManager.DriverData;
import org.unbiquitous.uos.core.driverManager.DriverManager;
import org.unbiquitous.uos.core.driverManager.DriverManagerException;
import org.unbiquitous.uos.core.driverManager.DriverNotFoundException;
import org.unbiquitous.uos.core.messageEngine.dataType.UpDevice;
import org.unbiquitous.uos.core.messageEngine.dataType.UpDriver;
import org.unbiquitous.uos.core.messageEngine.dataType.UpNetworkInterface;
import org.unbiquitous.uos.core.messageEngine.messages.Call;
import org.unbiquitous.uos.core.network.exceptions.NetworkException;


/**
 * This class is responsible for dealing with connectivity issues in the smart-space.
 * It resolves problems such as different network links, acessibility and visibility
 * between the provider and the consumer of a service.
 * 
 * @author Lucas Paranhos Quintella
 *
 */
public class ConnectivityManager {
	
	/** The context of the uOS */
	private SmartSpaceGateway gateway;
	
	/** Our device */
	private UpDevice device;
	
	/** If our device is set to do proxying */
	private boolean doProxying;
	
	/** For logging */
    private static final Logger logger = UOSLogging.getLogger();
	
	/** For registering drivers */
	private DriverManager driverManager;
	
	public ConnectivityManager(){}
	
	public void init(SmartSpaceGateway gateway, boolean doProxying){
		logger.info("Starting up UOS Connectivity Manager");
		this.gateway = gateway;
		this.device = this.gateway.getCurrentDevice();
		//TODO: isn't this needed?
//		this.driverManager = this.applicationContext.getDriverManager();
		this.doProxying = doProxying;
		logger.fine("DoProxying? "+this.doProxying);
		logger.info("Connectivity Manager is started");
	}
	
	/**
	 * Gets an appropriate - tries to choose the best one - network interface available to the given service.
	 * @param deviceProvider The remote device providing the service
	 * @param serviceCall The service call object
	 * @return The best network interface to this service
	 * @throws NetworkException
	 */
	public UpNetworkInterface getAppropriateInterface(UpDevice deviceProvider, Call serviceCall) throws NetworkException{
		
		//List of compatible network interfaces
		List<UpNetworkInterface> compatibleNetworks = new ArrayList<UpNetworkInterface>();
		
		//Solves the different network link problem:
		for(UpNetworkInterface thisNetInterface : this.device.getNetworks()){
			for(UpNetworkInterface providerNetInterface : deviceProvider.getNetworks()){
				if(thisNetInterface.getNetType().equals(providerNetInterface.getNetType())){
					compatibleNetworks.add(providerNetInterface);
					break;
				}			
			}			
		}
		
		//Solves the accessibility problem	
		
		//Solves the visibility problem
		//NetworkDevice.testConnectivity();
		
		//Checks if none compatible interface is available
		if(compatibleNetworks.size() == 0){
			logger.severe("ConnectivityManager - Lacks connectivity between the devices for this service");
			throw new ConnectivityException("ConnectivityManager - Lacks connectivity between the devices for this service");
		}
		
		//Gets the best choice of network for this service
		UpNetworkInterface networkInterface = servicesBestInterface(compatibleNetworks, serviceCall);
		
		return networkInterface;
	}
	
	
	/**
	 * Gets an appropriate - tries to choose the best one - network interface available to the given service.
	 * @param deviceProvider The remote device providing the service
	 * @param serviceCall The service call object
	 * @param preferredNetwork Set a preferred network interface to make the connection with
	 * @return A compatible network interface to this service
	 * @throws NetworkException
	 */
	public UpNetworkInterface getAppropriateInterface(UpDevice deviceProvider, Call serviceCall, String preferredNetwork) throws NetworkException{
		
		//If there is no preferred network interface, calls the standard method
		if( preferredNetwork == null ){
			logger.info("ConnectivityManager - The preferred network is null");
			return getAppropriateInterface(deviceProvider, serviceCall);
		}
		
		//Checks if the given network is a valid one on our current device
		boolean ok = false;
		for(UpNetworkInterface thisNetInterface : this.device.getNetworks()){
			
			if( thisNetInterface.getNetType().equals(preferredNetwork) ){
				ok = true;
				break;
			}
		}
		if( !ok ){
			logger.info("ConnectivityManager - There's no such network interface on the current device");
			return getAppropriateInterface(deviceProvider, serviceCall);
		}
		
		//Tries to get our preferred network interface on the provider device
		UpNetworkInterface network = null;
		for(UpNetworkInterface providerNetInterface : deviceProvider.getNetworks()){
			
			if( providerNetInterface.getNetType().equals(preferredNetwork) ){
				network = providerNetInterface;
				break;
			}			
		}
		//If there's no such network on the provider, gets any compatible network
		if( network == null ){
			logger.info("ConnectivityManager - There's no such network interface on the provider device");
			return getAppropriateInterface(deviceProvider, serviceCall);
		}
		
		return network;
	}
	
	
	
	/**
	 * Gets an appropriate - tries to choose the best one - network interface available to the given service.
	 * @param deviceProvider The remote device providing the service
	 * @return A compatible network interface to this service
	 * @throws NetworkException
	 */
	public UpNetworkInterface getAppropriateInterface(UpDevice deviceProvider) throws NetworkException{
		
		return getAppropriateInterface(deviceProvider, null);
	}
	
	
	
	/**
	 * This method receives a list of the already connectivity-problem-solved network interfaces, the service call and returns the best network
	 * interface to the given service specificities.
	 * 
	 * @param networks List of the compatible network interface for the service.
	 * @param serviceCall The service call object.
	 * @return The best choice of network Interface for the given service.
	 */
	private UpNetworkInterface servicesBestInterface(List<UpNetworkInterface> networks, Call serviceCall){
		
		//Insert QoS issues here. Check if service call is null.
		//if(serviceCall != null){
		// Gets properties from the serviceCall
		//}
		
		return networks.get(0);
	}
	
	
	
	
	/**
	 * Does the filtering of services which don't have connectivity with the given network device.
	 * @param callerName The caller device name
	 * @param driversList The list of remote drivers
	 */
	public void filterRemoteDriversList(String callerName, List<DriverData> driversList) {
		
		logger.fine("ConnectivityManager - Filtering the remote drivers");
		UpDevice callerUpDevice = this.gateway.getDeviceManager().retrieveDevice(callerName);

		if(callerUpDevice == null){
			logger.severe("ConnectivityManager - There's no such device");
			throw new RuntimeException("ConnectivityManager - There's no such device");
		}
		
		Set<DriverData> incompatibleDrivers = new HashSet<DriverData>();
		Set<DriverData> callerDeviceDrivers = new HashSet<DriverData>();
		
		Iterator<DriverData> iterator = driversList.iterator();
		//Takes off the drivers of the caller device
		while( iterator.hasNext() ){
			DriverData remoteDriverData = iterator.next();
			if( remoteDriverData.getDevice().equals(callerUpDevice) ){
				callerDeviceDrivers.add(remoteDriverData);
				iterator.remove();
			}
		}	
				
		//Searches for the drivers that don't have connectivity. Solves the differente network link problem.
		for(DriverData remoteDriverData : driversList){
			
			boolean compatible = false;
			
			for( UpNetworkInterface remoteNetInterface : remoteDriverData.getDevice().getNetworks() ){

				for( UpNetworkInterface callerNetInterface : callerUpDevice.getNetworks() ){
					
					if( remoteNetInterface.getNetType().equals(callerNetInterface.getNetType()) ){
						//One interface is enough to make them compatible
						compatible = true;
						break;
					}
				}
				if( compatible ){
					break;
				}	
			}
			
			if( !compatible ){
				incompatibleDrivers.add(remoteDriverData);
			}
			
		}
		
		//TODO : Proxying : Here we can extend to check visibility and accessibility
		
		//Removes all of the incompatible drivers
		driversList.removeAll(incompatibleDrivers);
		
		//Checks if we are proxying which ones are already compatible
		if( this.doProxying ){
			filterProxyingDrivers(callerDeviceDrivers, driversList);
		}

	}
	
	
	
	/**
	 * Does the filtering of services which don't have connectivity with the given network device.
	 * @param callerDevice The device who is making the service call
	 * @param driversList The list of remote drivers
	 */
	public void filterRemoteDriversList(UpDevice callerDevice, List<DriverData> driversList) {
		
		if( callerDevice != null ){
			filterRemoteDriversList(callerDevice.getName(), driversList);
		}else{
			logger.severe("ConnectivityManager - The caller device is null");
		}
	}
	
	
	
	/**
	 * Filter the proxy drivers if there's any.
	 * @param driversList The list of local drivers
	 */
	public void filterDriversList(List<DriverData> driversList) {
		
		logger.fine("ConnectivityManager - Filtering the local drivers");
		
		Iterator<DriverData> iterator = driversList.iterator();
		
		while( iterator.hasNext() ){
			DriverData driverData = iterator.next();
			if( driverManager.driver(driverData.getInstanceID()) instanceof ProxyDriver ){
				iterator.remove();
			}
		}

	}	
	
	
	
	/**
	 * 
	 * @param driversList
	 * @return
	 */
	private void filterProxyingDrivers(Set<DriverData> callerDeviceDrivers, List<DriverData> driversList){
		
		//Removes those proxies relative to the caller device 
		removesProxyiedCallerDrivers(callerDeviceDrivers, driversList);
		
		//Set of drivers to be removed
		Set<DriverData> toBeRemoved = new HashSet<DriverData>();
		
		for( DriverData rdd : driversList ){
			
			//Gets a driver that is not a proxy driver
			if( !rdd.getDevice().equals(this.gateway.getCurrentDevice()) ){
				
				//Searches for another driver that has the same id
				for( DriverData innerRdd : driversList ){
					
					if( innerRdd.getInstanceID().equals(rdd.getInstanceID()) && innerRdd != rdd ){
						//And removes it
						toBeRemoved.add(innerRdd);
						break;
					}					
				}	
			}
					
		}
		
		driversList.removeAll(toBeRemoved);	
		
	}
	
		
	/**
	 * 
	 * @param callerDeviceDrivers
	 * @param driversList
	 */
	private void removesProxyiedCallerDrivers(Set<DriverData> callerDeviceDrivers, List<DriverData> driversList){
		
		Iterator<DriverData> iterator = driversList.iterator();
		
		//Removes the proxy drivers of the caller device
		while( iterator.hasNext() ){
			DriverData remoteDriverData = iterator.next();			
			
			for(DriverData callerDriver : callerDeviceDrivers){
				if( callerDriver.getInstanceID().equals(remoteDriverData.getInstanceID()) ){
					iterator.remove();
					break;
				}
			}
				
		}
		
	}
	
	
	/**
	 * 
	 * @param driver The UpDriver to be registered
	 * @param device The device who is providing the driver
	 * @param id 
	 * @throws DriverNotFoundException 
	 */
	public void registerProxyDriver(UpDriver driver, UpDevice device, String id) throws DriverNotFoundException{
		logger.fine("Connectivity Manager -- New driver is being registered for proxying");

		ProxyDriver proxyDriver = new ProxyDriverImpl(driver, device);
		try{
			this.driverManager.deployDriver(driver, proxyDriver, id);
			driverManager.initDrivers(gateway, null);
		}catch(DriverManagerException e){
			logger.severe(e.getMessage());
		}
	}
	
	/**
	 * 
	 * @param id
	 */
	public void unregisterProxyDriver(String id){
		this.driverManager.undeployDriver(id);
	}
	
	
	/**
	 * Checks if the connectivity manager is set to do proxying.
	 * @return Either or not we are proxying remote drivers
	 */
	public boolean doProxying(){
		return this.doProxying;
	}
}
