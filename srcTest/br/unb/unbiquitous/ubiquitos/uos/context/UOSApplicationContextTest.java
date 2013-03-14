package br.unb.unbiquitous.ubiquitos.uos.context;

import static org.junit.Assert.assertEquals;

import java.net.InetAddress;
import java.util.ListResourceBundle;
import java.util.ResourceBundle;

import org.junit.After;
import org.junit.Test;

public class UOSApplicationContextTest {

	private UOSApplicationContext ctx;
	
	@After public void tearDown(){ctx.tearDown();}
	
	@Test public void shouldInitCurrentDeviceWithDefaultValues() throws Exception{
		ctx = new UOSApplicationContext();
		ResourceBundle prop = new ListResourceBundle() {
			protected Object[][] getContents() {
				return new Object[][] {};
			}
		};
		ctx.init(prop);
		
		// TODO: Should use other in case of "localhost" like on android devices
		assertEquals("When no deviceName is specified use hostname",
				InetAddress.getLocalHost().getHostName(),
				ctx.device().getName()); 
		assertEquals("Platform is defined by system propery ",
				System.getProperty("java.vm.name"),
				ctx.device().getProperty("platform")); 
	}
	
	@Test public void shouldInitCurrentDeviceWithInformeName() throws Exception{
		ctx = new UOSApplicationContext();
		ResourceBundle prop = new ListResourceBundle() {
			protected Object[][] getContents() {
				return new Object[][] {{"ubiquitos.uos.deviceName","MyName"}};
			}
		};
		ctx.init(prop);
		
		// TODO: Should use other in case of "localhost" like on android devices
		assertEquals("When deviceName is specified use it","MyName",
														ctx.device().getName()); 
	}
	
}
