package br.unb.unbiquitous.ubiquitos.network.loopback.channelManager;

import java.io.IOException;

import br.unb.unbiquitous.ubiquitos.network.connectionManager.ChannelManager;
import br.unb.unbiquitous.ubiquitos.network.exceptions.NetworkException;
import br.unb.unbiquitous.ubiquitos.network.loopback.LoopbackDevice;
import br.unb.unbiquitous.ubiquitos.network.loopback.connection.LoopbackClientConnection;
import br.unb.unbiquitous.ubiquitos.network.loopback.connection.LoopbackServerConnection;

/**
 * 
 * This class is the loopback version of the Channel Manager. It is responsible
 * for opening active and passive connections for new data channels.
 * 
 * @author Lucas Paranhos Quintella
 *
 */
public class LoopbackChannelManager implements ChannelManager{
	

	/**
	 * Opens an active connection, i.e., connects to a listening server.
	 * 
	 * @param networkDeviceName The name of the device of the listening server. It is in the 
	 * format This Device:ID.
	 * @return The established connection
	 */
	public LoopbackClientConnection openActiveConnection(String networkDeviceName)
			throws NetworkException, IOException {
		
		//Checks if the device name is correct
		String[] address = networkDeviceName.split(":");
		if(address.length != 2){
			throw new NetworkException("Invalid parameters for creation of the channel.");
		}
		
		//Connects to the given server. The device that will represent the client can be anyone
		LoopbackClientConnection clientConnection = new LoopbackClientConnection(new LoopbackDevice(), Long.parseLong(address[1]));
		
		return clientConnection;
	}

	
	/**
	 * Opens a passive connection, i.e., starts a new server, waits for new incoming connection
	 * and returns it.
	 * 
	 * @param networkDeviceName The name of the device to be listening to in the format This Device:ID
	 * @return The established connection
	 */
	public LoopbackClientConnection openPassiveConnection(String networkDeviceName)
			throws NetworkException, IOException {

		String[] address = networkDeviceName.split(":");
		
		if(address.length != 2){
			throw new NetworkException("Invalid parameters for creation of the channel.");
		}
		
		//Starts a new server with the given ID
		LoopbackServerConnection serverConnection = new LoopbackServerConnection(new LoopbackDevice(Long.parseLong(address[1])));

		return serverConnection.accept();
	}

	/**
	 * Returns a available network device to be connected.
	 * 
	 * @return A new available device
	 */
	public LoopbackDevice getAvailableNetworkDevice() {
		//Can be any device. Returns a new one with an unique ID.
		return new LoopbackDevice();
	}

	/**
	 * Tears down the Loopback Channel Manager
	 */
	public void tearDown() throws NetworkException, IOException {}
}
