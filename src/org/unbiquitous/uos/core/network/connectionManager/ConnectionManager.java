package org.unbiquitous.uos.core.network.connectionManager;

import org.unbiquitous.uos.core.InitialProperties;
import org.unbiquitous.uos.core.network.model.NetworkDevice;

/**
 * This Defines the interface of a Connection Manager, describing all essential methods.
 * Every Connection Manager from the UbiquitOS must implement this interface
 * 
 * @author Passarinho
 *
 */
public interface ConnectionManager extends Runnable{
	
	/**
	 * A method for setting a Listener for the connection Manager
	 * @param connectionManagerListener
	 */
	public void setConnectionManagerListener(ConnectionManagerListener connectionManagerListener);
	
	/**
	 * A method for setting properties for the connection Manager
	 * @param resource
	 */
	public void setResourceBundle(InitialProperties resource);
	public InitialProperties getResourceBundle();
	
	/**
	 * Finalize the Connection Manager.
	 */
	public void tearDown();
	
	/**
	 * A method for retrieve the networkDevice that is listening. 
	 * @return networkDevice
	 */
	public NetworkDevice getNetworkDevice();
	
	/**
	 * A method for retrive the channel manager of this connection manager
	 * @return channel managar
	 */
	public ChannelManager getChannelManager();

}
