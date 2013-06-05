package org.unbiquitous.uos.core.driver;

import static org.fest.assertions.api.Assertions.assertThat;

import java.io.File;
import java.util.ListResourceBundle;
import java.util.ResourceBundle;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.unbiquitous.uos.core.UOSApplicationContext;
import org.unbiquitous.uos.core.deviceManager.DeviceManager;
import org.unbiquitous.uos.core.driverManager.DriverManager;
import org.unbiquitous.uos.core.messageEngine.dataType.UpDevice;
import org.unbiquitous.uos.core.messageEngine.dataType.json.JSONDevice;
import org.unbiquitous.uos.core.messageEngine.messages.ServiceCall;
import org.unbiquitous.uos.core.messageEngine.messages.ServiceResponse;
import org.unbiquitous.uos.core.ontology.OntologyReasonerTest;

public class DeviceDriverTest_handShake {

	private DeviceDriver driver;
	private DeviceManager deviceManager;
	private UpDevice currentDevice;


	@Before public void setUp() throws Exception{
		new File("resources/owl/uoscontext.owl").createNewFile();
		
		ResourceBundle bundle = new ListResourceBundle() {
			protected Object[][] getContents() {
				return new Object[][] {
						{"ubiquitos.ontology.path","resources/owl/uoscontext.owl"},
						{"ubiquitos.ontology.reasonerFactory",OntologyReasonerTest.class.getName()},
				};
			}
		};
		UOSApplicationContext ctx = new UOSApplicationContext();
		ctx.init(bundle);
		currentDevice = ctx.getGateway().getCurrentDevice();
		
		driver = new DeviceDriver();
		
		DriverManager driverManager = ctx.getDriverManager();
		driverManager.deployDriver(driver.getDriver(), driver);
		driverManager.initDrivers(ctx.getGateway());
		
		deviceManager = ctx.getDeviceManager();
	}
	
	@After public void tearDown() throws Exception{
		new File("resources/owl/uoscontext.owl").delete();
	}
	
	
	@Test public void registerDeviceOnSystem() throws Exception{
		UpDevice toRegister = new UpDevice("Dummy")
									.addNetworkInterface("HERE", "LOCAL");
		
		ServiceCall call = new ServiceCall()
								.addParameter(	"device", 
												new JSONDevice(toRegister)
													.toString()
											);
		
		driver.handshake(call, new ServiceResponse(), null);
		assertThat(deviceManager.listDevices()).contains(toRegister);
	}
	
	@Test public void returnsTheCurrentDeviceOnSuccess() throws Exception{
		UpDevice toRegister = new UpDevice("Dummy")
									.addNetworkInterface("HERE", "LOCAL");
		
		ServiceCall call = new ServiceCall()
								.addParameter(	"device", 
												new JSONDevice(toRegister)
													.toString()
											);
		
		ServiceResponse response = new ServiceResponse();
		driver.handshake(call, response, null);
		assertThat(response.getResponseData("device"))
			.isEqualTo(new JSONDevice(currentDevice).toString());
	}
	
	@Test public void doesNothingWhenTheSameDeviceShowsUpAgain() throws Exception{
		UpDevice toRegister = new UpDevice("Dummy")
									.addNetworkInterface("HERE", "LOCAL");
		
		ServiceCall call = new ServiceCall()
								.addParameter(	"device", 
												new JSONDevice(toRegister)
													.toString()
											);
		
		driver.handshake(call, new ServiceResponse(), null);
		ServiceResponse response = new ServiceResponse();
		driver.handshake(call, response, null);
		
		assertThat(deviceManager.listDevices()).contains(toRegister);
		assertThat(response.getResponseData()).isNullOrEmpty();
		assertThat(response.getError()).isNotEmpty();
	}
	
	@Test public void doesNothingWhenThereIsNoDevice() throws Exception{
		ServiceResponse response = new ServiceResponse();
		driver.handshake(new ServiceCall(), response, null);
		
		assertThat(response.getResponseData()).isNullOrEmpty();
		assertThat(response.getError()).isNotEmpty();
	}
	
	@Test public void doesNothingWhenTheDeviceIsNotCorrectlyTransfered() throws Exception{

		ServiceCall call = new ServiceCall()
								.addParameter(	"device", 
												"Not valid JSON"
											);
		ServiceResponse response = new ServiceResponse();
		driver.handshake(call, response, null);
		
		assertThat(response.getResponseData()).isNullOrEmpty();
		assertThat(response.getError()).isNotEmpty();
	}
	
	//TODO: with myself
	
}
