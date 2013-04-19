package org.unbiquitous.uos.core.network.connectionManager;

import java.io.IOException;

import org.unbiquitous.uos.core.network.exceptions.NetworkException;
import org.unbiquitous.uos.core.network.model.NetworkDevice;
import org.unbiquitous.uos.core.network.model.connection.ClientConnection;


public interface ChannelManager {
	
	public ClientConnection openPassiveConnection(String networkDeviceName) throws NetworkException, IOException;
	
	public ClientConnection openActiveConnection(String networkDeviceName) throws NetworkException, IOException;
	
	public NetworkDevice getAvailableNetworkDevice();
	
	public void tearDown() throws NetworkException, IOException;

}
