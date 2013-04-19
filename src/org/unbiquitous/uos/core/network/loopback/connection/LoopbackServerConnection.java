package org.unbiquitous.uos.core.network.loopback.connection;

import java.io.IOException;

import org.unbiquitous.uos.core.network.loopback.LoopbackChannel;
import org.unbiquitous.uos.core.network.loopback.LoopbackDevice;
import org.unbiquitous.uos.core.network.model.NetworkDevice;
import org.unbiquitous.uos.core.network.model.connection.ServerConnection;

/**
 * 
 * This class represents the LoopbackServerConnection. It acts like a server socket, i.e., its
 * objects will wait and block in a device's ID (just like a port) for new connections with the
 * accept() method. LoopbackClientConnection object is returned from this waiting server when a
 * connection is established.
 * 
 * @author Lucas Paranhos Quintella
 *
 */
public class LoopbackServerConnection extends ServerConnection{

	/** The client with whom the connection has been established */
	private LoopbackClientConnection connectedClient;
	
	/** If some other thread tries to close the server, this is set as true */
	private boolean isClosed;
	
	/**
	 * Constructor. The server is still closed, i.e., not listening to connections.
	 * @param networkDevice The device that will be listening to new connections.
	 */
	public LoopbackServerConnection(NetworkDevice networkDevice) {
		super(networkDevice);
		this.isClosed = true;
	}
	
	/**
	 * When a connection is established, some other thread sets our client.
	 * @param client The server side client that has been connected to.
	 */
	public synchronized void setConnectedClient(LoopbackClientConnection client){
		this.connectedClient = client;
	}

	/**
	 * Blocks until a client tries to connect with us. A new LoopbackClientConnection is
	 * returned when the connection is established, just like a server socket.
	 * 
	 * @return The server side client that has been connected to.
	 * @throws IOException Thrown when some other thread closed the server while we
	 * were waiting for connections.
	 */
	public synchronized LoopbackClientConnection accept() throws IOException {
			
		//No client is connected to us
		this.connectedClient = null;
		
		//Adds the server to the waiting server vector
		LoopbackChannel.addWaitingServer(this);
		
		//We are opened and waiting
		this.isClosed = false;
		
		//Waits until we got a client connected to us
		while(this.connectedClient == null){
			try {		
				//block until someone wakes us up
				wait();
			}catch (InterruptedException e) {
				//Wait throws an exception
			}finally{
				//Check if we stopped waiting cause other thread closed us
				if(this.isClosed){
					throw new IOException("LoopbackServer is closed");
				}
			}
		}

		return this.connectedClient;
	}


	/**
	 * Closes the listening server.
	 */
	public void closeConnection() throws IOException {
		LoopbackChannel.removeWaitingServer(this);
	}
	
	/**
	 * Sets the server connection as closed. This is used for closing the listening server
	 * from another thread.
	 */
	public synchronized void setAsClosed(){
		this.isClosed = true;
	}
	
	/**
	 * Getter of the server's device's ID
	 * @return The server's ID
	 */
	public long getDeviceId(){
		return ((LoopbackDevice)this.networkDevice).getDeviceId();

	}
	

}
