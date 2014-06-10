package org.unbiquitous.uos.core.network.radar;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.unbiquitous.uos.core.InitialProperties;
import org.unbiquitous.uos.core.UOSLogging;
import org.unbiquitous.uos.core.network.connectionManager.ConnectionManager;
import org.unbiquitous.uos.core.network.connectionManager.ConnectionManagerControlCenter;
import org.unbiquitous.uos.core.network.exceptions.NetworkException;
import org.unbiquitous.uos.core.network.model.NetworkDevice;


/**
 *   This Class is the Control Center of the UbiquitOS Radars. Its the central Microkernel 
 * component. It centralizes all the radars events, treating them and passing to the layer 
 * above: the DeviceManager
 * 
 * @author Passarinho
 *
 */
public class RadarControlCenter implements RadarListener {
	
	private static final Logger logger = UOSLogging.getLogger();
	
	/* *****************************
	 *   	ATRUBUTES
	 * *****************************/
	
	// A list of all Radars of the Control Center
	private List<Radar> radarList;
	private List<Thread> radarThreadList;
	// The Radar Listener Above
	private RadarListener radarListener;
	// The resource bundle from where we can get a set of configurations
	private InitialProperties properties;

	// ConnectionManagerControlCenter for resolving the connectionManager dependency
	ConnectionManagerControlCenter connectionManagerControlCenter;
	
	/* *****************************
	 *   	CONSTRUCTOR
	 * *****************************/
	
	/**
	 * Constructor using DeviceManager
	 * @param deviceManager
	 * @throws UbiquitOSException
	 */
	public RadarControlCenter(InitialProperties properties,
			ConnectionManagerControlCenter connectionManagerControlCenter) throws NetworkException {
		this.properties = properties;
		this.connectionManagerControlCenter = connectionManagerControlCenter;
		// Instantiates all the Radars("Externals Servers" of this component)
		loadRadars();
	}

	
	public void setListener(RadarListener radarListener){
		this.radarListener = radarListener;
	}
	
	
	/* *****************************
	 *   	PUBLIC METHODS
	 * *****************************/
	
	// Starts all radars of the Control Center
	public void startRadar() {
		if (radarList != null){
			for (Radar radar : radarList) {
				radar.startRadar();
				Thread thread = new Thread(radar);
				thread.start();
				if (radarThreadList == null){
					radarThreadList = new ArrayList<Thread>();
				}
				radarThreadList.add(thread);
			}
		}
		
	}
	
	// Stop All radars of the control Center
	public void stopRadar() {
		if (radarList != null){
			for (Radar radar : radarList) {
				radar.stopRadar();
			}
		}
		
	}
	/* *****************************
	 *   	PUBLIC METHODS - RADAR LISTENER INTERFACE
	 * *****************************/

	public void deviceEntered(NetworkDevice device){
		radarListener.deviceEntered(device);
	}

	public void deviceLeft(NetworkDevice device){
		radarListener.deviceLeft(device);
		
	}

	/* *****************************
	 *   	PRIVATE METHODS
	 * *****************************/

	/**
	 * Loads dynamically the radars defined in the UbiquitOS properties file
	 */
	private void loadRadars() throws NetworkException {
		// A list of created Radars
		radarList = new ArrayList<Radar>();
		
		try {
			Map<Class<Radar>, Class<ConnectionManager>> radarMap = properties.getRadars();
			if(radarMap != null){
				for(Class<Radar> radar : radarMap.keySet()){
					addRadar(radarMap, radar);
				}
			}
		} catch (Exception e) {
			String message = "Error loading radars. The error found was: "+e.getMessage();
			logger.log(Level.SEVERE,message,e);
		}
		
		if (radarList == null || radarList.isEmpty()){
			logger.info("There is no Radars defined on Radar Control Center");
		}
	}


	@SuppressWarnings("rawtypes")
	private void addRadar(Map<Class<Radar>, Class<ConnectionManager>> radarMap,
			Class<Radar> radar) throws NoSuchMethodException,
			InstantiationException, IllegalAccessException,
			InvocationTargetException {
		Class[] radarListenerArgsClass = new Class[] { RadarListener.class };
		Constructor constructor = radar.getConstructor(radarListenerArgsClass);
		Radar newRadar = (Radar) constructor.newInstance(this);
		radarList.add(newRadar);
		setRadarManager(radarMap, radar, newRadar);
	}


	private void setRadarManager(
			Map<Class<Radar>, Class<ConnectionManager>> radarMap,
			Class<Radar> radar, Radar newRadar) {
		Class<ConnectionManager> connectionManagerClass = radarMap.get(radar);
		String managerName = connectionManagerClass.getCanonicalName();
		ConnectionManager manager = connectionManagerControlCenter.findConnectionManagerInstance(managerName);
		newRadar.setConnectionManager(manager);
	}
}
