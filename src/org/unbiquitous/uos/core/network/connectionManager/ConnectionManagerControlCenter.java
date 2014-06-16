/*
 * BluetoothConnectionManager.java
 *
 * Created on January 7, 2007, 11:02 AM
 */

package org.unbiquitous.uos.core.network.connectionManager;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.unbiquitous.uos.core.InitialProperties;
import org.unbiquitous.uos.core.UOSComponent;
import org.unbiquitous.uos.core.UOSComponentFactory;
import org.unbiquitous.uos.core.UOSLogging;
import org.unbiquitous.uos.core.network.exceptions.NetworkException;
import org.unbiquitous.uos.core.network.model.NetworkDevice;
import org.unbiquitous.uos.core.network.model.connection.ClientConnection;
import org.unbiquitous.uos.core.network.radar.RadarControlCenter;

/**
 * Manage the ubiquitos-smartspace service interface.
 *
 * @author Passarinho
 */
public class ConnectionManagerControlCenter implements ConnectionManagerListener, UOSComponent{
   
	private static Logger logger = UOSLogging.getLogger(); 
	
	// Separator token for resource parameters
	private static final String PARAM_SEPARATOR = ",";
	
	// Public constant for resource keys
	private static final String CONNECTION_MANAGER_CLASS_KEY = "ubiquitos.connectionManager";
	
	/* *****************************
	 *   	ATRUBUTES
	 * *****************************/
	
	// A list of all Radars of the Control Center
	private List<ConnectionManager> connectionManagersList;
	private Map<ConnectionManager, Thread> connectionManagersThreadMap;
	private Map<String,ConnectionManager> connectionManagersMap;
	
    
    /** The Adaptability Engine module reference. */
    private MessageListener messageListener = null;
	
    /** The resource bundle from where we can get a set of configurations. */
	private InitialProperties properties;

	private RadarControlCenter radarControlCenter;
	private int maxRetries = 30;
	private int waitTime = 100;
	
    /* *****************************
	 *   	PUBLIC METHODS
	 * *****************************/

    /**
     * Method invoked to handle a connection established from a UbiquitOS Client.
     * @param con
     * @throws UbiquitOSException
     */
    public void handleClientConnection(ClientConnection clientConnection) {
    	// Creates a threaded ConnectionHandlingNotifier to handle the connection
    	// The connection handling must be done in a separated thread so it can handle multiple connections.
    	ThreadedConnectionHandler threadedConnectionHandling = new ThreadedConnectionHandler(clientConnection,messageListener);
    	threadedConnectionHandling.start();
	}
    
    /**
     * A method for retrieve all network devices that are waiting for connection in connection managers.
     * @return list of networkDevice.
     */
    public List<NetworkDevice> getNetworkDevices(){
    	List<NetworkDevice> networkDeviceList = new ArrayList<NetworkDevice>();
    	for(ConnectionManager cm : connectionManagersList){
    		networkDeviceList.add(cm.getNetworkDevice());
    	}
    	return networkDeviceList;
    }
    
    /**
     * A method for retrieve the networkDevice of the given type from the connectionManager.
     * @param networkDeviceType
     * @return networkDevice
     * @throws NetworkException
     */
    public NetworkDevice getNetworkDevice(String networkDeviceType) throws NetworkException{
    	ConnectionManager connectionManager = null;
    	for(ConnectionManager cm : connectionManagersList){
    		if(cm.getNetworkDevice().getNetworkDeviceType().equals(networkDeviceType)){
    			connectionManager = cm;
    			break;
    		}
    	}
    	
    	if(connectionManager == null){
    		throw new NetworkException("There is no Connection Manager for the given connection type: "+networkDeviceType);
    	}
    	
    	return connectionManager.getNetworkDevice();
    }
    
    /**
     * A method for retrieve an available networkDevice of the given type.
     * @param networkDeviceType
     * @return networkDevice
     * @throws NetworkException
     */
    public NetworkDevice getAvailableNetworkDevice(String networkDeviceType) throws NetworkException{
    	ChannelManager channelManager = null;
    	for(ConnectionManager cm : connectionManagersList){
    		if(cm.getNetworkDevice().getNetworkDeviceType().equals(networkDeviceType)){
    			channelManager = cm.getChannelManager();
    			break;
    		}
    	}
    	
    	if(channelManager == null){
    		throw new NetworkException("There is no Channel Manager for the given connection type: "+networkDeviceType);
    	}
    	
    	return channelManager.getAvailableNetworkDevice();
    }
    
    /**
     * Retrieve the channelID from the given networkDevice.
     * @param networkDevice
     * @return channelID
     * @throws Exception
     */
    public String getChannelID(String networkDeviceName){
    	return networkDeviceName.split(":")[1];
    }
    
    /**
     * Retrieve the host from the given networkDevice.
     * @param networkDevice
     * @return host
     * @throws Exception
     */
    public String getHost(String networkDeviceName){
    	return networkDeviceName.split(":")[0];
    }
    
    /**
     * Open a passive connection based on the networkDeviceName and networkDeviceType given, when the remote
     * host connect on this passive connection a clientConnection is created e returned. 
     * @param networkDeviceName
     * @param networkDeviceType
     * @return clientConnection
     * @throws NetworkException
     */
    public ClientConnection openPassiveConnection(String networkDeviceName, String networkDeviceType) throws NetworkException{
    	ChannelManager channelManager = null;
    	for(ConnectionManager cm : connectionManagersList){
    		if(cm.getNetworkDevice().getNetworkDeviceType().equals(networkDeviceType)){
    			channelManager = cm.getChannelManager();
    			break;
    		}
    	}
    	
    	if(channelManager == null){
    		throw new NetworkException("There is no Channel Manager for the given connection type: "+networkDeviceType);
    	}
    	
    	try{
    		return channelManager.openPassiveConnection(networkDeviceName);
    	}catch (Exception e) {
    		throw new NetworkException("Could not create channel.",e);
		}
    }
    
    /**
     * Open a active connection, clientConnection, based on the networkDeviceName and networkDeviceType given. 
     * @param networkDeviceName
     * @param networkDeviceType
     * @return clientConnection
     * @throws NetworkException
     */
    public ClientConnection openActiveConnection(String networkDeviceName, String networkDeviceType) throws NetworkException{
    	ChannelManager channelManager = null;
    	for(ConnectionManager cm : connectionManagersList){
    		if(cm.getNetworkDevice().getNetworkDeviceType().equals(networkDeviceType)){
    			channelManager = cm.getChannelManager();
    			break;
    		}
    	}
    	
    	if(channelManager == null){
    		throw new NetworkException("There is no Channel Manager for the given connection type: "+networkDeviceType);
    	}
    	
    	try{
    		return channelManager.openActiveConnection(networkDeviceName);
    	}catch (Exception e) {
    		throw new NetworkException("Could not create channel.",e);
		}
    }
    
    /* *****************************
	 *   	PRIVATE METHODS
	 * *****************************/
    
    /**
	 * Loads dynamically the Connection Managers defined in the UbiquitOS properties file
	 */
	private void loadAndStartConnectionManagers() throws NetworkException {
    	// A list of created Connection Managers
    	connectionManagersList = new ArrayList<ConnectionManager>();
    	connectionManagersMap = new HashMap<String, ConnectionManager>();
    	connectionManagersThreadMap = new HashMap<ConnectionManager, Thread>();
		
    	// 1. LOAD ALL CONNECTIONS MANAGERS FROM THE RESOURCE FILE
		
    	try {
			// Retrieve all defined Connection Managers.
    		if (!properties.containsKey(CONNECTION_MANAGER_CLASS_KEY)){
    			logger.warning("No '"+CONNECTION_MANAGER_CLASS_KEY+"' property defined. This implies on no network communication for this instance.");
    			return;
    		}
			String connectionPropertie = null; 
			if (this.properties != null){
				connectionPropertie = properties.getString(CONNECTION_MANAGER_CLASS_KEY);
			}
			String[] connectionsArray = null; //UbiquitosResourceBundleReader.getParamSplitedArray(UbiquitosResourceBundleReader.RADAR_CLASS_KEY);
			if (connectionPropertie != null){
				connectionsArray = connectionPropertie.split(PARAM_SEPARATOR);
			}
			// Iterate the array getting each Connection Manager class name
			for (String radar : connectionsArray) {
				// Loads dynamically the class
				@SuppressWarnings("rawtypes")
				Class c = Class.forName(radar);
				// Create a new instance of the Connection Manager
				ConnectionManager newConMan = (ConnectionManager) c.newInstance(); 
				// Sets the this Control Center as the Listener of the new Connection Manager 
				newConMan.setConnectionManagerListener(this);
				// Sets the resource bundle
				newConMan.init(properties);
				// Add to the Connection Managers to a List
				connectionManagersList.add(newConMan);
				connectionManagersMap.put(radar, newConMan);
				
			}
		} catch (Exception e) {
			NetworkException ex = new NetworkException("Error reading UbiquitOS Resource Bundle Propertie File. " +
														   "Check if the files exists or there is no errors in his definitions." +
														   " The found error is: "+e.getMessage());
			throw ex;
		}
		
		
		// 2. CHECK IF THERE IS ANY CONNECTION MANAGER IN THE CONTROL CENTER

		if (connectionManagersList == null || connectionManagersList.isEmpty()){
			NetworkException ex = new NetworkException("There is no Connection Managers defined on Connection Managers Control Center");
			throw ex;
		}
		
		// 3. STARTS ALL THE CONNECTION MANAGERS
		
		for (ConnectionManager connectionManager : connectionManagersList) {
			// Create a thread for each one and starts it.
			Thread t = new Thread(connectionManager);
			t.start();
			connectionManagersThreadMap.put(connectionManager, t);
		}
    }    
    
    /**
     * Returns the current instance of the informed connection manager.
     * 
     * @param cManagerClass Name of the class of the connection manager to be found.
     * @return ConnectionManager if found. Null otherwise.
     */
    public ConnectionManager findConnectionManagerInstance(String cManagerClass){
    	return connectionManagersMap.get(cManagerClass);
    }
    
    public RadarControlCenter radarControlCenter(){
    	return radarControlCenter;
    }
    
    /************************ USO COmpoment ***************************/
    
    public ConnectionManagerControlCenter() {}
    
    public void setListener(MessageListener listener){
    	this.messageListener = listener;
    }
    
    @Override
    public void create(InitialProperties properties) {
    	this.properties = properties;
		if (properties.containsKey("ubiquitos.message.response.timeout")) {
			waitTime = properties.getInt("ubiquitos.message.response.timeout");
		}
		if (properties.containsKey("ubiquitos.message.response.retry")) {
			maxRetries = properties.getInt("ubiquitos.message.response.retry");
		}
    }
    
    @Override
    public void init(UOSComponentFactory factory) {
        loadAndStartConnectionManagers();
        radarControlCenter = new RadarControlCenter(properties, this);
    }
    
    @Override
    public void start() {
		radarControlCenter.startRadar();
    }
    
    @Override
    public void stop() {
    	tearDown();
    }

    public void tearDown() {
		radarControlCenter.stopRadar();
    	for(ConnectionManager cm : connectionManagersList){
    		logger.finest("Stoppping "+cm.getClass().getSimpleName());
    		cm.tearDown();
    		try {
				connectionManagersThreadMap.get(cm).join();
			} catch (Exception e) {
				logger.log(Level.SEVERE,"Problems tearing down.",e);
			}
    	}
	}
    
    /**
     * Sends the message using the control channel to the specified device.
     * @param waitForResponse Specifies if method should wait for a synchronous response.
     * @return Response from that device
     */
    public String sendControlMessage(String message, boolean waitForResponse,
			String networkAddress, String networkType) throws IOException,
			InterruptedException {
		ClientConnection connection = openActiveConnection(networkAddress, networkType);

		if (connection == null) {
			logger.warning(String
					.format("Not possible to stablish a connection with %s of type %s.",
							networkAddress, networkType));
			return null;
		}
		if (connection.getDataInputStream() == null || connection.getDataOutputStream() == null) {
			return null;
		}

		String response = sendReceive(message, connection, waitForResponse);

		connection.closeConnection();

		if (!waitForResponse || response.isEmpty()) {
			return null;
		}
		return response;
	}

	/**
	 * Method responsible for handling the sending of a request and the
	 * receiving of its response
	 * 
	 * @param jsonCall
	 *            JSON Object of the service call to be sent
	 * @param outputStream
	 *            OutputStream Object to write into
	 * @param inputStream
	 *            InputStream Object to read from
	 * @return String of the response read
	 * @throws IOException
	 * @throws InterruptedException
	 */
	// FIXME: This is NetworkLayer work
	private String sendReceive(String call, ClientConnection connection, boolean waitForResponse)
			throws IOException, InterruptedException {
		System.out.println("<< in "+connection.getDataInputStream());
		System.out.println("<< out "+connection.getDataOutputStream());
		
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
				connection.getDataOutputStream()));
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				connection.getDataInputStream()));

		writer.write(call);
		writer.write('\n');
		writer.flush();

		if (waitForResponse) {
			StringBuilder builder = new StringBuilder();
			for (int i = 0; i < maxRetries; i++) {
				if (reader.ready()) {
					for (Character c = (char) reader.read(); c != '\n'; c = (char) reader
							.read()) {
						builder.append(c);
					}
					break;
				}
				Thread.sleep(waitTime);
			}

			logger.fine("Received message '" + builder + "'.");
			return builder.toString();
		}
		return null;
	}
    
}
