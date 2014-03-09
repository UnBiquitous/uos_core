package org.unbiquitous.uos.core.network.loopback.connectionManager;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.unbiquitous.uos.core.InitialProperties;
import org.unbiquitous.uos.core.UOSLogging;
import org.unbiquitous.uos.core.network.connectionManager.ChannelManager;
import org.unbiquitous.uos.core.network.connectionManager.ConnectionManager;
import org.unbiquitous.uos.core.network.connectionManager.ConnectionManagerListener;
import org.unbiquitous.uos.core.network.exceptions.NetworkException;
import org.unbiquitous.uos.core.network.loopback.LoopbackChannel;
import org.unbiquitous.uos.core.network.loopback.LoopbackDevice;
import org.unbiquitous.uos.core.network.loopback.channelManager.LoopbackChannelManager;
import org.unbiquitous.uos.core.network.loopback.connection.LoopbackServerConnection;
import org.unbiquitous.uos.core.network.model.NetworkDevice;


/**
 * 
 * This class is the connection manager of loopback connections. It is responsible for listening
 * to new incoming connections and handling them correctly.
 * 
 * @author Lucas Paranhos Quintella
 *
 */
public class LoopbackConnectionManager implements ConnectionManager {
	
    /** Object for logging registration.*/
	public static final Logger logger = UOSLogging.getLogger();
	
	/** A Connection Manager Listener (ConnectionManagerControlCenter) */
    private ConnectionManagerListener connectionManagerListener;
    
    /** The ChannelManager for new data channels */
    private LoopbackChannelManager channelManager;
    
	private InitialProperties properties;
    
	/** The device that will be listening to new connections */
    private LoopbackDevice listeningDevice;
    private LoopbackServerConnection listeningConnection;
    
    /** Indicates that some thread tried to tear down the Connection Manager */
    private boolean closingLoopbackConnectionManager;
    
    /** The default ID of the Connection Manager server that will be listening to connections */
    public static final int DEFAULT_ID = 0;
	
    /**
	 * Constructor
	 * @throws UbiquitOSException
	 */
    public LoopbackConnectionManager() throws NetworkException {
    	LoopbackDevice.initDevicesID();
    	LoopbackChannel.initChannel();
    }
    
    
	/**
	 * Getter of the Channel Manager of the Connection Manager
	 * @return The Channel Manager
	 */
	public synchronized ChannelManager getChannelManager() {

		if(this.channelManager == null){
			this.channelManager = new LoopbackChannelManager();
		}
		
		return this.channelManager;
	}
	

	/**
	 * Returns the server device that is listening to new incoming connections.
	 * @return The server device
	 */
	public synchronized NetworkDevice getNetworkDevice() {
		
		if(this.listeningDevice == null){
			this.listeningDevice = new LoopbackDevice(DEFAULT_ID);
		}
		
		return this.listeningDevice;
	}

	
	/**
	 * Setter of the middleware's Network Control Center
	 * @param connectionManagerListener Reference for the Control Center
	 */
	public void setConnectionManagerListener(
			ConnectionManagerListener connectionManagerListener) {
		this.connectionManagerListener = connectionManagerListener;
	}

	
	/**
	 * Setter of the resource bundle
	 * @param resource Reference to the resource bundle
	 */
	public void setProperties(InitialProperties resource) {
		this.properties = resource;
	}

	public InitialProperties getProperties(){
		return this.properties;
	}
	
	/**
	 * Tears down the Connection Manager and all of its dependencies.
	 */
	public void tearDown(){
		try {
			logger.fine("Closing Loopback Connection Manager...");
			//Sets true for stopping the Connection Manager thread
			this.closingLoopbackConnectionManager = true;
			//Shuts the server
			this.listeningConnection.closeConnection();
			//Tears down the Channel Manager, if it exists
			if(this.channelManager != null){
				this.channelManager.tearDown();
			}
			//Tears down the LoopbackChannel
			LoopbackChannel.tearDown();
		} catch (Exception e) {
			this.closingLoopbackConnectionManager = false;
			String msg = "Error stoping Loopback Connection Manager. ";
            logger.log(Level.SEVERE,msg, e);
            throw new RuntimeException(msg + e);
		}
		
	}

	
	/**
	 * Waits for new connections indefinitely and handle them properly.
	 */
	public void run() {
		logger.fine("Starting uOS Smart-Space Loopback Connection Manager.");
        logger.info("Starting Loopback Connection Manager...");
        
        //Instantiates the server
        this.listeningConnection = new LoopbackServerConnection(getNetworkDevice());
        logger.info("Loopback Connection Manager is started.");
        
        while(true){
        	try {
        		//Starts listening for and handling new connections. Accept() will throw a 
        		//exception if another thread closed the server
        		this.connectionManagerListener.handleClientConnection(this.listeningConnection.accept());
        		logger.info("Loopback Connection Manager -- Connection handled!");
			}catch (IOException ex) {
				//Check if some other error occurred
    			if(!closingLoopbackConnectionManager){
    				String msg = "Error handling connection at Loopback Connection Manager. ";
                    logger.log(Level.SEVERE,msg, ex);
                    throw new RuntimeException(msg + ex);
    			}else{
    				//Another thread closed the Connection Manager. OK, let it closes.
    				logger.fine("Loopback Connection Manager is closed.");
    				return;
    			}
    		}
			
        }
		
	}

}
