package org.unbiquitous.uos.core.integration;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import org.unbiquitous.uos.core.InitialProperties;
import org.unbiquitous.uos.core.network.connectionManager.ChannelManager;
import org.unbiquitous.uos.core.network.connectionManager.ConnectionManager;
import org.unbiquitous.uos.core.network.connectionManager.ConnectionManagerListener;
import org.unbiquitous.uos.core.network.exceptions.NetworkException;
import org.unbiquitous.uos.core.network.model.NetworkDevice;
import org.unbiquitous.uos.core.network.model.connection.ClientConnection;


public class IntegrationConnectionManager implements ConnectionManager {
	
	private IntegrationDevice device;
	private ConnectionManagerListener connectionListener;
	
	private static final CM channelMng = new CM();
	private InitialProperties bundle;
	private static class CM implements ChannelManager {
		/*    PC (Driver side)              CELL (App side)
		 * 
		 * Writes on 'pcOut' ----------->  Reads on 'cellIn'
		 * 
		 *  Reads on 'pcIn'  <----------- writes on 'cellOut'
		 */
		IntegrationDevice pc;
		PipedInputStream cellIn;
		PipedOutputStream cellOut;
		ClientConnection toPcConn;
		IntegrationConnectionManager pcManager;
		
		IntegrationDevice cell;
		PipedInputStream pcIn;
		PipedOutputStream pcOut;
		ClientConnection toCellConn;
		IntegrationConnectionManager cellManager;
		
		private void loadStreams() throws IOException{
			cellOut = new PipedOutputStream();
			pcOut = new PipedOutputStream();
			cellIn = new PipedInputStream(pcOut);
			pcIn = new PipedInputStream(cellOut);
		}
		
		public CM() {
			try {
				loadStreams();
				pc = new IntegrationDevice("my.pc");
				cell = new IntegrationDevice("my.cell");
				
				toPcConn = new ClientConnection(pc) {
					public DataOutputStream getDataOutputStream() throws IOException {
						return new DataOutputStream(cellOut);
					}
					public DataInputStream getDataInputStream() throws IOException {
						return new DataInputStream(cellIn);
					}
					public void closeConnection() throws IOException {
						loadStreams();
					}
					public boolean isConnected() {
						return true; //FIXME not right
					}
				}; 
				toCellConn = new ClientConnection(cell) {
					public DataOutputStream getDataOutputStream() throws IOException {
						return new DataOutputStream(pcOut);
					}
					public DataInputStream getDataInputStream() throws IOException {
						return new DataInputStream(pcIn);
					}
					public void closeConnection() throws IOException {
						loadStreams();
					}
					public boolean isConnected() {
						return true; //FIXME not right
					}
				}; 
				
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		
		public void tearDown() throws NetworkException, IOException {}
		
		public ClientConnection openPassiveConnection(String networkDeviceName)
				throws NetworkException, IOException {
			// TODO Auto-generated method stub
			return null;
		}
		
		public ClientConnection openActiveConnection(String networkDeviceName)
				throws NetworkException, IOException {
			if(networkDeviceName.equals("my.pc")){
				pcManager.connectionListener.handleClientConnection(toCellConn);
				return toPcConn;
			}else if(networkDeviceName.equals("my.cell")){
				cellManager.connectionListener.handleClientConnection(toPcConn);
				return toCellConn;
			}
			return null;
		}
		
		// TODO: Check this guy
		public NetworkDevice getAvailableNetworkDevice() {return new IntegrationDevice("non.existant");}
	}
	
	@Override
	public void run() {} //Here happens the loop for receiving connections

	@Override
	public void setConnectionManagerListener(ConnectionManagerListener connectionManagerListener) {
		this.connectionListener = connectionManagerListener; // The guy who is notified of new connections received 
	}

	@Override
	public void setResourceBundle(InitialProperties bundle) {
		this.bundle = bundle;
		String deviceName = bundle.getString("ubiquitos.uos.deviceName");
		if (deviceName.equals("my.pc")){
			device = channelMng.pc;
			channelMng.pcManager = this;
		}else if (deviceName.equals("my.cell")){
			device = channelMng.cell;
			channelMng.cellManager = this;
		}else{
			device = null;
		}
	}
	
	public InitialProperties getResourceBundle(){
		return this.bundle;
	}

	@Override
	public void tearDown() {} // Some clean-up

	@Override
	public NetworkDevice getNetworkDevice() {
		return device; // It is called, but does it matter?
	}

	@Override
	public ChannelManager getChannelManager() {
		return channelMng;
	}

}
