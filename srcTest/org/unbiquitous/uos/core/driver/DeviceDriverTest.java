package org.unbiquitous.uos.core.driver;

import static org.fest.assertions.api.Assertions.assertThat;

import org.junit.Test;
import org.unbiquitous.uos.core.messageEngine.dataType.UpService;

public class DeviceDriverTest {

	DeviceDriver instance = new DeviceDriver();

	@Test public void declareTheInterfaceProperly(){
		assertThat(instance.getDriver().getName()).isEqualTo("uos.DeviceDriver");	
	}
	
	@Test public void declareListDriver(){
		assertThat(instance.getDriver().getServices())
		.contains(
				new UpService("listDrivers")
					.addParameter("driverName", 
							UpService.ParameterType.OPTIONAL)
				);	
	}
	
	@Test public void declareAuthenticate(){
		assertThat(instance.getDriver().getServices())
				.contains(
					new UpService("authenticate")
						.addParameter("securityType",
							UpService.ParameterType.MANDATORY)
				);	
	}
	
	@Test public void declareGoodbye(){
		assertThat(instance.getDriver().getServices())
		.contains(
				new UpService("goodbye")
				);	
	}
	
	@Test public void declareHandshake(){
		assertThat(instance.getDriver().getServices())
				.contains(
					new UpService("handshake")
						.addParameter("device",
							UpService.ParameterType.MANDATORY)
				);	
	}
	
	@Test public void declareTellEquivalentDriver(){
		assertThat(instance.getDriver().getServices())
				.contains(
					new UpService("tellEquivalentDriver")
						.addParameter("driverName",
							UpService.ParameterType.MANDATORY)
				);	
	}
	
}
