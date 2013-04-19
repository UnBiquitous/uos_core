package org.unbiquitous.uos.core.network.cache;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * This class encapsulate the cached control over the opened input stream of a cached connection.
 * Each method, delegates its actions to the DataInputStream class, after updating the access information
 * of the cached connection.
 * 
 * @author Fabricio Nogueira Buzeto.
 *
 */
public class CachedInputStream extends DataInputStream implements DataInput {
	
	private CachedConnectionData connectionData;
	
	public CachedInputStream(InputStream in, CachedConnectionData data) {
		super(in);
		connectionData = data;
	}
	
	@Override
	public synchronized void mark(int readlimit) {
		connectionData.updateAccess();
		super.mark(readlimit);
	}
	
	@Override
	public int available() throws IOException {
		connectionData.updateAccess();
		return super.available();
	}
	
	@Override
	public boolean markSupported() {
		connectionData.updateAccess();
		return super.markSupported();
	}
	
	@Override
	public int read() throws IOException {
		connectionData.updateAccess();
		return super.read();
	}
	
	@Override
	public synchronized void reset() throws IOException {
		connectionData.updateAccess();
		super.reset();
	}
	
	@Override
	public long skip(long n) throws IOException {
		connectionData.updateAccess();
		return super.skip(n);
	}
	
	public void close() throws IOException{
		// closing must be delegated to the cache controller.
		connectionData.getController().closeConnection(connectionData.getConnection());
	}
	
}
