package org.unbiquitous.uos.core.network.model.connection;

import java.io.IOException;

import org.unbiquitous.uos.core.network.model.NetworkDevice;

public abstract class ServerConnection{
	
	/**
	 * Device who will be listening.
	 */
	protected NetworkDevice networkDevice;
	
	/**
	 * accept a connection and return the client connection.
	 * @return client connection
	 * @throws IOException
	 */
	public abstract ClientConnection accept() throws IOException;
	
	/**
	 * Tries to close the open connections.
	 * @throws IOException
	 */
	public abstract void closeConnection() throws IOException;
	
	/**
	 * Constructor
	 * 
	 * @param networkDevice the device that will be listening.
	 */
	public ServerConnection(NetworkDevice networkDevice){
		this.networkDevice = networkDevice;
	}
	
	/**
	 * A method for retrieve the networkDevice of this connection.
	 * @return networkDevice
	 */
	public NetworkDevice getNetworkDevice(){
		return networkDevice;
	}
	
}
