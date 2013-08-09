package org.unbiquitous.uos.core.network.cache;

import java.io.IOException;

/**
 * Interface of cacheable connections.
 * 
 * @author Fabricio Nogueira Buzeto
 *
 */
public interface CachableConnection {
	
	/**
	 * Method called when a connection needs truly to be closed.
	 * @throws IOException
	 */
	public void tearDown() throws IOException ;
}
