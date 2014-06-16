package org.unbiquitous.uos.core.network.cache;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.unbiquitous.uos.core.network.model.connection.ClientConnection;

/**
 * 
 * This class is responsible for controlling the cache of ClientConnections.
 * Each controller control single connections between the host device and its server devices.
 * One controller must be created for each type of communication established.  
 * 
 * @author Fabricio Nogueira Buzeto 
 *
 */
public class CacheController {

	private static Logger logger = Logger.getLogger(CacheController.class.getName());
	
	private static int DEFAULT_CONNECTION_TIMEOUT = 5*60*1000;
	private static int DEFAULT_CHECK_WAIT_TIME = 60*1000;
	
	private int connectionTimeout = DEFAULT_CONNECTION_TIMEOUT;
	private int checkWaitTime = DEFAULT_CHECK_WAIT_TIME;
	
	/**
	 *  Connection cache used to know which connections where recently used and
	 *  which can be discarded.
	 */
	protected HashMap<ClientConnection, CachedConnectionData> connectionCache = new HashMap<ClientConnection,CachedConnectionData>();
	/**
	 * Client Cache to know which connections each client have.
	 */
	protected HashMap<String, List<ClientConnection>> clientCache = new HashMap<String, List<ClientConnection>>();
	
	/**
	 * Current instance responsible to check if the cached connections have expried.
	 */
	protected CacheChecker cacheChecker;
	
	public CacheController() {}
	
	public CacheController(int connectionTimeout) {
		this();
		this.connectionTimeout = connectionTimeout;
	}
	
	public CacheController(int connectionTimeout, int checkWaitTime) {
		this(connectionTimeout);
		this.checkWaitTime = checkWaitTime;
	}
	
	/**
	 * Add a connection to the cache.
	 * 
	 * @param c Connection to be added to the cache.
	 * 
	 * @return Cached Connection Data about the cached connection to help it's control.
	 */
	public CachedConnectionData addConnection(ClientConnection c){
		CachedConnectionData connectionData = null;
		String deviceName = c.getClientDevice().getNetworkDeviceName();
		synchronized (clientCache) {
			List<ClientConnection> clientConnectionList = clientCache.get(deviceName);
			if (clientConnectionList == null){
				clientConnectionList = new ArrayList<ClientConnection>();
				clientCache.put(deviceName,clientConnectionList);
			}
			clientConnectionList.add(c);
			
			connectionData = new CachedConnectionData(c,this);
			
			connectionCache.put(c, connectionData);
			
		}
		
		// if no Cache Checker exists, start one to check whenever a connection is no longer needed
		if (cacheChecker == null){
			cacheChecker = new CacheChecker();
			Thread t = new Thread(cacheChecker);
			t.start();
		}
		
		logger.log(Level.FINE,"Added cached connection for device '"+c.getClientDevice().getNetworkDeviceName()+"'");
		
		return connectionData;
	}
	
	/**
	 * Method responsible for removing a connection from the cache.
	 * The connection will be closed (teared down) too.
	 * 
	 * @param c Connection  to be removed.
	 * @throws IOException
	 */
	public void removeConnection(ClientConnection c) throws IOException{
		
		String deviceName = c.getClientDevice().getNetworkDeviceName();
		synchronized (clientCache) {
			
			List<ClientConnection> clientConnectionList = clientCache.get(deviceName);
			
			// remove from client connection cache
			if (clientConnectionList != null){
				clientConnectionList.remove(c);
			}
			
			// if there is no connection for that device, remove it from the client cache
			if (clientConnectionList == null || clientConnectionList.isEmpty()){
				clientCache.remove(deviceName);
			}
			
			// remove from the connection cache
			connectionCache.remove(c);
			
			// close the connection
			tearDownConnection(c);
			
			logger.log(Level.FINE,"Removed cached connection for device '"+c.getClientDevice().getNetworkDeviceName()+"'");
		}
	}
	
	/**
	 * Method responsible for closing a connection (if it has expired).
	 * 
	 * @param c Connection to be closed.
	 * @throws IOException
	 */
	public void closeConnection(ClientConnection c) throws IOException{
		synchronized (clientCache) {
			if (isConnectionExpired(c)){
				removeConnection(c);
			}
		}
	}

	/**
	 * Method responsible for trully closing the connection.
	 * 
	 * @param c Connection to the "teared down."
	 * @throws IOException
	 */
	private void tearDownConnection(ClientConnection c) throws IOException {
		if (c instanceof CachableConnection){
			logger.log(Level.FINE,"Tearing down connection for device '"+c.getClientDevice().getNetworkDeviceName()+"'");
			((CachableConnection) c).tearDown();
		}else{
			logger.log(Level.FINE,"Closing connection for device '"+c.getClientDevice().getNetworkDeviceName()+"'");
			c.closeConnection();
		}
	}
	
	/**
	 * Check if a connecion has expired.
	 * 
	 * @param c Connection to be checked.
	 * 
	 * @return <code>true</code> if the connection has expired. <code>false</code> otherwise.
	 */
	private boolean isConnectionExpired(ClientConnection c){
		CachedConnectionData connectionData = connectionCache.get(c);
		
		if (! connectionData.getConnection().isConnected()){
			return false;
		}
		
		if(connectionData != null) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(connectionData.getLastAccess());
			cal.add(Calendar.MILLISECOND, connectionTimeout);
			Date timeout = cal.getTime();
			
			logger.log(Level.FINE,"LastAccess: '"+connectionData.getLastAccess()+"' timeout: '"+timeout+"' result: "+timeout.before(connectionData.getLastAccess()));
			
			return timeout.before(new Date());
		}
		return true;
	}
	
	/**
	 * Return a valid cached connection for the informed device.
	 * 
	 * @param deviceName Name of the device who detains the cached connection.
	 * 
	 * @return Cached ClientConnection.
	 */
	public ClientConnection getConnection(String deviceName){
		synchronized (clientCache) {
			List<ClientConnection> clientConnections = clientCache.get(deviceName);
			if (clientConnections != null && !clientConnections.isEmpty()){
				for (ClientConnection c : clientConnections){
					if (!isConnectionExpired(c)){
						return c;
					}else{
						try {
							// if the connection has expired, close it.
							removeConnection(c);
						} catch (IOException e) {
							logger.log(Level.SEVERE,"Failure removing cache",e);
						}
					}
				}
			}
		}
		return null;
	}
	
	/**
	 * Logger for the CacheChecker class.
	 */
	private static final Logger cacheChekerLogger = Logger.getLogger(CacheChecker.class.getName());
	/**
	 * This Class is responsible for checking which connections has expired and mus be closed and
	 * removed from the cache.
	 * 
	 * @author Fabricio Nogueira Buzeto
	 *
	 */
	private class CacheChecker implements Runnable{
		@Override
		public void run() {
			cacheChekerLogger.log(Level.FINE,"Starting Connection Cache Checker.");
			while (true){
				try {
					Thread.sleep(checkWaitTime);
				} catch (InterruptedException e) {
					cacheChekerLogger.log(Level.SEVERE,"Interrupted while waiting.",e);
					//return;
				}
				synchronized (clientCache) {
					// if there is no connection to cache
					// this check is no longer needed
					if(connectionCache.isEmpty()){
						break;
					}
					
					Set<ClientConnection> connectionCacheToBeRemoved = new HashSet<ClientConnection>();
					
					for (ClientConnection c : connectionCache.keySet()){
						cacheChekerLogger.log(Level.FINE,"Checking Chached Connection for device '"+c.getClientDevice().getNetworkDeviceName()+"'");
						if (isConnectionExpired(c)){
							cacheChekerLogger.log(Level.FINE,"Tearing Down Chached Connection for device '"+c.getClientDevice().getNetworkDeviceName()+"'");
							connectionCacheToBeRemoved.add(c);
						}
					}
					
					for (ClientConnection clientConnection : connectionCacheToBeRemoved) {
						try {
							removeConnection(clientConnection);
						} catch (IOException e) {
							cacheChekerLogger.log(Level.SEVERE,"Failure in cache",e);
						}
					}
				}
			}
			// indicate the inexistence of a Cache Checker
			cacheChecker = null;
			cacheChekerLogger.log(Level.FINE,"Stoping Connection Cache Checker.");
		}
	}
	
}
