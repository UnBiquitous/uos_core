package br.unb.unbiquitous.ubiquitos.network.connectionManager;

import br.unb.unbiquitous.ubiquitos.network.model.connection.ClientConnection;

/**
 * This Defines the interface of a ConnectionManager, describing all essential methods.
 * Every ConnectionManager from the UbiquitOS must implement this interface
 * 
 * @author Passarinho
 *
 */
//FIXME : ConnectionManagerListener : This class comment does not fits its actions 
public interface ConnectionManagerListener {

	/**
	 * Handles a Connection established from a client.
	 * @param bluetoothClientConnection
     * @throws UbiquitOSException
	 */
	public void handleClientConnection(ClientConnection clientConnection);
	
	/**
	 * Finalize the Connection Manager.
	 */
	public void tearDown();
	
}
