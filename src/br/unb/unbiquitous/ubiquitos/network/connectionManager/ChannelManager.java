package br.unb.unbiquitous.ubiquitos.network.connectionManager;

import java.io.IOException;

import br.unb.unbiquitous.ubiquitos.network.exceptions.NetworkException;
import br.unb.unbiquitous.ubiquitos.network.model.NetworkDevice;
import br.unb.unbiquitous.ubiquitos.network.model.connection.ClientConnection;

public interface ChannelManager {
	
	public ClientConnection openPassiveConnection(String networkDeviceName) throws NetworkException, IOException;
	
	public ClientConnection openActiveConnection(String networkDeviceName) throws NetworkException, IOException;
	
	public NetworkDevice getAvailableNetworkDevice();
	
	public void tearDown() throws NetworkException, IOException;

}
