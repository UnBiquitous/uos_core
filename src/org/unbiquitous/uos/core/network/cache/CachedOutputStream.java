package org.unbiquitous.uos.core.network.cache;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * This class encapsulate the cached control over the opened output stream of a cached connection.
 * Each method, delegates its actions to the DataOutputStream class, after updating the access information
 * of the cached connection.
 * 
 * @author Fabricio Nogueira Buzeto.
 *
 */
public class CachedOutputStream extends DataOutputStream {

	private CachedConnectionData connectionData;
	
	public CachedOutputStream(OutputStream in, CachedConnectionData data) {
		super(in);
		connectionData = data;
	}

	@Override
	public void close() throws IOException {
		// closing must be delegated to the cache controller.
		connectionData.getController().closeConnection(connectionData.getConnection());
	}
	
	@Override
	public void flush() throws IOException {
		connectionData.updateAccess();
		super.flush();
	}
	
	@Override
	public void write(byte[] b) throws IOException {
		connectionData.updateAccess();
		super.write(b);
	}
	
	@Override
	public void write(byte[] b, int off, int len)
			throws IOException {
		connectionData.updateAccess();
		super.write(b, off, len);
	}
	
	@Override
	public void write(int b) throws IOException {
		connectionData.updateAccess();
		super.write(b);
	}
}
