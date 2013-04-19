package org.unbiquitous.uos.core.context;

import static org.junit.Assert.assertEquals;
import static org.fest.assertions.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ListResourceBundle;
import java.util.ResourceBundle;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.unbiquitous.uos.core.UOSApplicationContext;
import org.unbiquitous.uos.core.applicationManager.ApplicationDeployer;
import org.unbiquitous.uos.core.applicationManager.DummyApp;


public class UOSApplicationContextTest {

	private UOSApplicationContext ctx;
	
	@Before public void setUp() throws IOException{
		new File("resources/owl/uoscontext.owl").createNewFile();
	}
	
	@After public void tearDown(){
		ctx.tearDown();
		new File("resources/owl/uoscontext.owl").delete();
	}
	
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
	
	@Test public void startApplicationsInSpecifiedInTheProperties() throws Exception{
		ctx = new UOSApplicationContext();
		ResourceBundle prop = new ListResourceBundle() {
			protected Object[][] getContents() {
				return new Object[][] {
					{ApplicationDeployer.APPLICATION_LIST,DummyApp.class.getName()},
					{"ubiquitos.ontology.path","resources/owl/uoscontext.owl"},
				};
			}
		};
		ctx.init(prop);
		
		assertThat(DummyApp.lastInstance.inited).isTrue();
	}
	
}
