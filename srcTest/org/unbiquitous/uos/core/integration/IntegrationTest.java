package org.unbiquitous.uos.core.integration;

import static org.junit.Assert.assertTrue;
import static org.fest.assertions.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.util.ListResourceBundle;
import java.util.Map.Entry;
import java.util.ResourceBundle;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.unbiquitous.uos.core.ContextException;
import org.unbiquitous.uos.core.UOSApplicationContext;
import org.unbiquitous.uos.core.driver.DeviceDriver;


public class IntegrationTest {
	
    @Before public void setUp() throws IOException{
		new File("resources/owl/uoscontext.owl").createNewFile();
	}
	
	@After public void tearDown(){
		new File("resources/owl/uoscontext.owl").delete();
	}
	
	//TODO: Better explain the purpose of this test
	@Test public void execute() throws Exception{
		//Driver Side
		// TODO: This bypass does not test how the middleware instantiates drivers and applications
		UOSApplicationContext pc = startContext("my.pc");
		EchoDriver echo = new EchoDriver();
		pc.getDriverManager().deployDriver(echo.getDriver(), echo);
		pc.getDriverManager().initDrivers(pc.getGateway());//TODO: What an ugly thing to do, should be initialized automaticali

		//App side
		UOSApplicationContext cell = startContext("my.cell");
		PingApp ping = new PingApp();
		cell.getApplicationManager().deploy(ping, "pingApp"); //TODO: id should be plausibly auto assigned
		
		//promote radar handshake		
		cell.getRadarControlCenter().deviceEntered(new IntegrationDevice("my.pc"));
		
		//Test if handshake was successfull
		assertThat(cell.getGateway().listDrivers(echo.getDriver().getName())).
			isNotEmpty();
		assertThat(cell.getGateway().listDrivers("uos.DeviceDriver")).hasSize(2);
		assertThat(pc.getGateway().listDrivers("uos.DeviceDriver")).hasSize(2);
		
		
		//TODO: remove sync by time, do somethign plugable
		Thread.sleep(5000);//Some time to things set up straight
		//finish instances
		pc.tearDown();
		cell.tearDown();
		
		//checkresults
		for (Entry<String, Boolean> e : ping.assertions.entrySet()){
			assertTrue(e.getKey(),e.getValue());
		}
		for (Entry<String, Boolean> e : echo.assertions.entrySet()){
			assertTrue(e.getKey(),e.getValue());
		}
		
		//TODO: Estimulate the deviceLeft
		

	}

	private UOSApplicationContext startContext(final String deviceName) throws ContextException{
		ResourceBundle pcBundle = new ListResourceBundle() {
			protected Object[][] getContents() {
				return new Object[][] {
					{"ubiquitos.message.response.timeout", "100"}, //Optional
					{"ubiquitos.message.response.retry", "30"},//Optional
					{"ubiquitos.connectionManager", IntegrationConnectionManager.class.getName()},
					{"ubiquitos.uos.deviceName", deviceName}, //TODO: Should not be mandatory, and could be automatic
					{"ubiquitos.driver.deploylist", DeviceDriver.class.getName()}, //TODO: Should not be mandatory
					{"ubiquitos.ontology.path","resources/owl/uoscontext.owl"}, //TODO: Should not be mandatory
		        };
			}
		};
		
		UOSApplicationContext instance = new UOSApplicationContext();
		instance.init(pcBundle);
		return instance;
	}
	
}
