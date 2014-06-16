package org.unbiquitous.uos.core.network.radar;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.unbiquitous.uos.core.InitialProperties;
import org.unbiquitous.uos.core.UOSLogging;
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

	// Separator token for resource parameters
	private static final String PARAM_SEPARATOR = ",";
	
	// Public constant for resource keys
	private static final String RADAR_CLASS_KEY = "ubiquitos.radar";
	
	private static String CONNECTION_MANAGER_INDICATOR_BEGIN = "(";
	
	private static String CONNECTION_MANAGER_INDICATOR_END = ")";
	
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
			// Retrieve all defined radars.
			String radarsPropertie = null; 
			if (this.properties != null && properties.containsKey(RADAR_CLASS_KEY)){
				radarsPropertie = properties.getString(RADAR_CLASS_KEY);
				
				String[] radarsArray = null; 
				if (radarsPropertie != null){
					radarsArray = radarsPropertie.split(PARAM_SEPARATOR);
				}
				// Iterate the array getting each radar class name
				for (String radar : radarsArray) {
					loadRadar(radar);
				}
			}
		} catch (Exception e) {
			logger.log(Level.SEVERE,"Error loading radars.",e);
		}
		
		// Check if there is any radar in the Control Center
		if (radarList == null || radarList.isEmpty()){
			logger.info("There is no Radars defined on Radar Control Center");
		}
	}


	/**
	 * Method responsible for loading a specific radar.
	 * 
	 * @param radar String containign the radar data.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void loadRadar(String radar)
			throws ClassNotFoundException, NoSuchMethodException,
			InstantiationException, IllegalAccessException,
			InvocationTargetException {
		
		// A Util Model for finding a constructor who have a RadarListener as parameter - Reflection API
		Class[] radarListenerArgsClass = new Class[] { RadarListener.class };
		
		String radarClass = null;
		String connectionManagerClass = null;
		if (radar.contains(CONNECTION_MANAGER_INDICATOR_BEGIN) &&
				radar.contains(CONNECTION_MANAGER_INDICATOR_END)){
			// Driver data with specified instanceID
			connectionManagerClass = radar.substring(
					radar.indexOf(CONNECTION_MANAGER_INDICATOR_BEGIN)+1,
					radar.indexOf(CONNECTION_MANAGER_INDICATOR_END)
						);
			radarClass = radar.substring(
					0,
					radar.indexOf(CONNECTION_MANAGER_INDICATOR_BEGIN)
					);
		}else{
			if (radar.contains(CONNECTION_MANAGER_INDICATOR_BEGIN) ||
					radar.contains(CONNECTION_MANAGER_INDICATOR_BEGIN)){
				// Driver data with malformed specified instanceID
				String erroMessage = "Radar Data '"+radar+"' in "+RADAR_CLASS_KEY+" is malformed.";
				logger.severe(erroMessage);
				//throw new NetworkException(erroMessage);
				//continue;
			}else{
				// Driver data without instanceId
				radarClass = radar;
			}
		}
		
		// Loads dynamically the class
		Class c = Class.forName(radarClass);
		// Gets the constructor having the RadarListener parameter
		Constructor constructor = c.getConstructor(radarListenerArgsClass);
		// Create a new instance of the radar, passing this(the RadarControlCenter) as the RadarListener
		Radar newRadar = (Radar) constructor.newInstance(this);
		// Add to the radars to a List
		radarList.add(newRadar);
		
		//Set the connection manager for the radar (if needed)
		newRadar.setConnectionManager(connectionManagerControlCenter.findConnectionManagerInstance(connectionManagerClass));
	}
	

}
