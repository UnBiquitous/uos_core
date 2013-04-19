package org.unbiquitous.uos.core.network.loopback.connection;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.unbiquitous.uos.core.network.exceptions.NetworkException;
import org.unbiquitous.uos.core.network.loopback.LoopbackChannel;
import org.unbiquitous.uos.core.network.loopback.LoopbackDevice;
import org.unbiquitous.uos.core.network.model.connection.ClientConnection;


/**
 * 
 * This class represents the LoopbackClientConnection. It acts like a socket, i.e., a 
 * LoopbackClientConnection object can connect to a listening server. In the same way, a 
 * LoopbackClientConnection object is returned from a waiting server when a connection
 * is established.
 * 
 * @author Lucas Paranhos Quintella
 *
 */
public class LoopbackClientConnection extends ClientConnection{
	
	/** Data streams of this connection */
	protected DataInputStream dataInputStream;
    protected DataOutputStream dataOutputStream;
    
    /** The channel of this connection */
    protected LoopbackChannel channel;

    /**
     * Creates a LoopbackClientConnection and connects it to the server with the given ID, if 
     * there's such server listening to connections.
     * 
     * @param clientDevice The device to be connected to the server
     * @param id The server's id
     */
	public LoopbackClientConnection(LoopbackDevice clientDevice, long id) throws NetworkException{
		super(clientDevice);
		
		//LoopbackChannel's static method will do the trick. Exception may be thrown.
		LoopbackChannel.establishConnection(this,id);

	}
	
	/**
	 * Creates a LoopbackClientConnection already connected to a server. This is returned from
	 * a listening server when the connection is established.
	 * 
	 * @param clientDevice The device (interface) of the server side client connection.
	 */
	public LoopbackClientConnection(LoopbackDevice clientDevice) {
		super(clientDevice);	
	}	
	
	public boolean isConnected() {
		return channel != null;
	}
	
	
	/**
	 * Closes the connection and its corresponding channel and streams.
	 */
	public void closeConnection() throws IOException {		
		this.channel.close();
		this.channel = null;
		this.dataInputStream.close();
		this.dataOutputStream.close();
	}

	
	/**
	 * Getter of the data input stream.
	 * @return The input stream of the connection.
	 */
	public DataInputStream getDataInputStream() throws IOException {
		
		if(this.dataInputStream == null){
			this.dataInputStream = new DataInputStream(this.channel.getInputStream(getDeviceId()));
		}
		
		return this.dataInputStream;
	}

	/**
	 * Getter of the data output stream.
	 * @return The output stream of the connection.
	 */
	public DataOutputStream getDataOutputStream() throws IOException {
		
		if(this.dataOutputStream == null){
			this.dataOutputStream = new DataOutputStream(this.channel.getOutputStream(getDeviceId()));
		}
		
		return this.dataOutputStream;
	}
	
	/**
	 * Returns which interface, or device, the connection is established on.
	 * @return The device's ID.
	 */
	public long getDeviceId(){
		return ((LoopbackDevice)this.clientDevice).getDeviceId();
	}
	
	/**
	 * Setter of the channel, if no channel has been already set on this connection. A RuntimeException
	 * is thrown when the connection already got a channel.
	 * @param channel The channel of the connection
	 */
	public void setChannel(LoopbackChannel channel){
		if(this.channel != null){
			throw new RuntimeException("LoopbackClientConnection already got a channel");
		}
		this.channel = channel;
	}

}
