package br.unb.unbiquitous.ubiquitos.network.model.connection;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import br.unb.unbiquitous.ubiquitos.network.model.NetworkDevice;

/**
 * This abstract class describes a abstraction of a device found in the smart-space by a radar(Bluetooth, Ethernet, etc.)
 *  It defines the generic method to access the found device.
 * @author Passarinho
 */
public abstract class ClientConnection {
	
	/**
	 * Device who stabilished the client side of the connection
	 */
	protected NetworkDevice clientDevice;
	
	/**
	 * @return <code>true</code> if the Connection is still alive. <code>false</code> otherwise.
	 */
	public abstract boolean isConnected();
	
	/**
     * Return a InputStrem of the client connection
     * @return
     * @throws IOException 
     */
    public abstract DataInputStream getDataInputStream() throws IOException;
    
    /**
     * Returns a OutputStream of the Client Connection
     * @return
     * @throws IOException 
     */
    public abstract DataOutputStream getDataOutputStream() throws IOException;
	
	/**
	 * Tries to close the open connections.
	 * @throws IOException
	 */
	public abstract void closeConnection() throws IOException;

	/**
	 * Returns the client side device of the connection.
	 * @return
	 */
	public NetworkDevice getClientDevice() {
		return clientDevice;
	}

	/**
	 * Constructor
	 * 
	 * @param clientDevice the client side device of the connection.
	 */
	public ClientConnection(NetworkDevice clientDevice){
		this.clientDevice = clientDevice;
	}
	
}
