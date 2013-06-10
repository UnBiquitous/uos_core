package org.unbiquitous.uos.core.network.cache;

import static org.fest.assertions.api.Assertions.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.junit.Test;
import org.unbiquitous.uos.core.network.model.NetworkDevice;
import org.unbiquitous.uos.core.network.model.connection.ClientConnection;

public class CacheControllerTest {

	CacheController ctl = new CacheController();
	
	@Test
	public void whenAddAConnectionRetrieveIt(){
		String addr = "192.168.2.3";
		ClientConnection conn = createClientConn(addr);
		ctl.addConnection(conn);
		assertThat(ctl.getConnection(addr)).isSameAs(conn);
	}
	
	@Test
	public void whenSearchForUnkownGetsNull(){
		assertThat(ctl.getConnection("192.168.2.1")).isNull();
	}
	
	@Test
	public void dontConfusesConnectionWithTheDeviceAddr(){
		String addrCached = "192.168.2.3:14984";
		String addrNotCached = "192.168.2.3:14985";
		ClientConnection conn = createClientConn(addrCached);
		ctl.addConnection(conn);
		assertThat(ctl.getConnection(addrNotCached)).isNotSameAs(conn);
		assertThat(ctl.getConnection("192.168.2.3")).isNotSameAs(conn);
		assertThat(ctl.getConnection("192.168.2.1")).isNull();
	}

	private ClientConnection createClientConn(String addr) {
		return new ClientConnection(createNetworkDevice(addr)) {
			
			public boolean isConnected() {
				return false;
			}
			
			public DataOutputStream getDataOutputStream() throws IOException {
				return null;
			}
			
			public DataInputStream getDataInputStream() throws IOException {
				return null;
			}
			
			public void closeConnection() throws IOException {
			}
		};
	}

	private NetworkDevice createNetworkDevice(final String addr) {
		return new NetworkDevice() {
			
			public String getNetworkDeviceType() {
				return null;
			}
			
			public String getNetworkDeviceName() {
				return addr;
			}
		};
	}
	
}
