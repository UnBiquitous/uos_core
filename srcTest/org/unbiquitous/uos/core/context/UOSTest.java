package org.unbiquitous.uos.core.context;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ListResourceBundle;
import java.util.ResourceBundle;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.unbiquitous.uos.core.UOS;
import org.unbiquitous.uos.core.applicationManager.DummyApp;
import org.unbiquitous.uos.core.ontology.OntologyReasonerTest;


public class UOSTest {

	private UOS ctx;
	
	@Before public void setUp() throws IOException{
		new File("resources/uoscontext.owl").delete();
	}
	
	@After public void tearDown(){
		ctx.stop();
		new File("resources/uoscontext.owl").delete();
	}
	
	@Test public void shouldInitCurrentDeviceWithDefaultValues() throws Exception{
		ctx = new UOS();
		ResourceBundle prop = new ListResourceBundle() {
			protected Object[][] getContents() {
				return new Object[][] {};
			}
		};
		ctx.start(prop);
		
		assertEquals("When no deviceName is specified use hostname",
				InetAddress.getLocalHost().getHostName(),
				ctx.getGateway().getCurrentDevice().getName()); 
		assertEquals("Platform is defined by system propery ",
				System.getProperty("java.vm.name"),
				ctx.getGateway().getCurrentDevice().getProperty("platform")); 
	}
	
	@Test public void shouldInitCurrentDeviceWithRandomValueIfLocalhosIsTheDeviceName() throws Exception{
		ctx = new UOS();
		ResourceBundle prop = new ListResourceBundle() {
			protected Object[][] getContents() {
				return new Object[][] {{"ubiquitos.uos.deviceName","localhost"}};
			}
		};
		ctx.start(prop);
		
		assertThat(ctx.getGateway().getCurrentDevice().getName()).isNotEqualTo("localhost");
	}
	
	@Test public void shouldInitCurrentDeviceWithInformeName() throws Exception{
		ctx = new UOS();
		ResourceBundle prop = new ListResourceBundle() {
			protected Object[][] getContents() {
				return new Object[][] {{"ubiquitos.uos.deviceName","MyName"}};
			}
		};
		ctx.start(prop);
		
		assertEquals("When deviceName is specified use it","MyName",
								ctx.getGateway().getCurrentDevice().getName()); 
	}
	
	@Test public void startApplicationsInSpecifiedInTheProperties() throws Exception{
		ctx = new UOS();
		new File("resources/uoscontext.owl").createNewFile();
		ResourceBundle prop = new ListResourceBundle() {
			protected Object[][] getContents() {
				return new Object[][] {
					{"ubiquitos.application.deploylist",DummyApp.class.getName()},
					{"ubiquitos.ontology.path","resources/uoscontext.owl"},
					{"ubiquitos.ontology.reasonerFactory",OntologyReasonerTest.class.getName()},
				};
			}
		};
		ctx.start(prop);
		
		assertThat(DummyApp.lastInstance.inited).isTrue();
	}
	
}
