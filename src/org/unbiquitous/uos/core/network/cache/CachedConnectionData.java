package org.unbiquitous.uos.core.network.cache;

import java.util.Date;

import org.unbiquitous.uos.core.network.model.connection.ClientConnection;

/**
 * Class responsible for storing the data about a Connection Cache. 
 * 
 * @author Fabricio Nogueira Buzeto
 *
 */
public class CachedConnectionData {
	
	private ClientConnection connection;
	
	private Date lastAccess = new Date();
	
	private CacheController controller ;

	public CachedConnectionData(ClientConnection connection,
			CacheController controller) {
		super();
		this.connection = connection;
		this.controller = controller;
	}
	
	public void updateAccess(){
		synchronized (lastAccess) {
			lastAccess.setTime(new Date().getTime());
		}
	}

	/**
	 * @return the controller
	 */
	public CacheController getController() {
		return controller;
	}

	/**
	 * @return the connection
	 */
	public ClientConnection getConnection() {
		return connection;
	}

	/**
	 * @return the lastAccess
	 */
	public Date getLastAccess() {
		return lastAccess;
	}
	
}
