package org.unbiquitous.uos.core.driver;


import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.ListResourceBundle;
import java.util.ResourceBundle;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.unbiquitous.json.JSONObject;
import org.unbiquitous.uos.core.UOSApplicationContext;
import org.unbiquitous.uos.core.adaptabitilyEngine.SmartSpaceGateway;
import org.unbiquitous.uos.core.deviceManager.DeviceManager;
import org.unbiquitous.uos.core.driverManager.DriverManager;
import org.unbiquitous.uos.core.messageEngine.dataType.UpDevice;
import org.unbiquitous.uos.core.messageEngine.dataType.UpDriver;
import org.unbiquitous.uos.core.messageEngine.dataType.json.JSONDevice;
import org.unbiquitous.uos.core.messageEngine.dataType.json.JSONDriver;
import org.unbiquitous.uos.core.messageEngine.messages.ServiceCall;
import org.unbiquitous.uos.core.messageEngine.messages.ServiceResponse;
import org.unbiquitous.uos.core.ontology.OntologyReasonerTest;

public class DeviceDriverTest_handShake {

	private DeviceDriver driver;
	private DeviceManager deviceManager;
	private UpDevice currentDevice;
	private DriverManager driverManager;


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
		
		driverManager = ctx.getDriverManager();
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
		
		SmartSpaceGateway gateway = mockGateway(currentDevice);
		
		driver.init(gateway, "id");
		when(gateway.callService((UpDevice)any(), (ServiceCall)any()))
				.thenReturn(new ServiceResponse());
		
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
		assertThat(response.getResponseData("device"))
					.isEqualTo(new JSONDevice(currentDevice).toString());
		assertThat(response.getError()).isNullOrEmpty(); 
		// FIXME: error when the driver is already registered
		assertThat(deviceManager.listDevices()).contains(toRegister);
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
	
	
	@Test public void mustNotBreakWhenItsDoneTwice() throws Exception{
		SmartSpaceGateway gateway = mockGateway(new UpDevice());
		
		JSONObject driversList = new JSONObject();
		UpDriver dummyInterface = new UpDriver("ddd");
		dummyInterface.addService("s");
		driversList.put("id_d", new JSONDriver(dummyInterface));
		
		when(gateway.callService((UpDevice)any(), (ServiceCall)any()))
		.thenReturn(new ServiceResponse().addParameter("driverList", driversList ));
		
		driver.init(gateway, "id");
		UpDevice toRegister = new UpDevice("Dummy")
				.addNetworkInterface("HERE", "LOCAL");
		
		ServiceCall call = new ServiceCall()
				.addParameter("device",new JSONDevice(toRegister).toString());
		
		ServiceResponse response = new ServiceResponse();
		driver.handshake(call, response, null);
		driver.handshake(call, response, null);
		
		ArgumentCaptor<ServiceCall> getCall = ArgumentCaptor
				.forClass(ServiceCall.class); 
		verify(gateway, times(2)).callService(eq(toRegister), getCall.capture());
		assertThat(getCall.getValue().getDriver()).isEqualTo("uos.DeviceDriver");
		assertThat(getCall.getValue().getService()).isEqualTo("listDrivers");
		
		assertThat(driverManager.listDrivers("ddd", "Dummy")).isNotEmpty();
		assertThat(response.getError()).isNullOrEmpty();
	}
	@Test public void mustRegisterTheOtherDriversLocallyAlso() throws Exception{
		SmartSpaceGateway gateway = mockGateway(new UpDevice());

		JSONObject driversList = new JSONObject();
		UpDriver dummyInterface = new UpDriver("ddd");
		dummyInterface.addService("s");
		driversList.put("id_d", new JSONDriver(dummyInterface));
		
		when(gateway.callService((UpDevice)any(), (ServiceCall)any()))
			.thenReturn(new ServiceResponse().addParameter("driverList", driversList ));
		
		driver.init(gateway, "id");
		UpDevice toRegister = new UpDevice("Dummy")
										.addNetworkInterface("HERE", "LOCAL");

		ServiceCall call = new ServiceCall()
					.addParameter("device",new JSONDevice(toRegister).toString());
		
		ServiceResponse response = new ServiceResponse();
		driver.handshake(call, response, null);
		
		ArgumentCaptor<ServiceCall> getCall = ArgumentCaptor
													.forClass(ServiceCall.class); 
		verify(gateway, times(1)).callService(eq(toRegister), getCall.capture());
		assertThat(getCall.getValue().getDriver()).isEqualTo("uos.DeviceDriver");
		assertThat(getCall.getValue().getService()).isEqualTo("listDrivers");
		
		assertThat(driverManager.listDrivers("ddd", "Dummy")).isNotEmpty();
	}

	private SmartSpaceGateway mockGateway(UpDevice currentDevice) {
		SmartSpaceGateway gateway = mock(SmartSpaceGateway.class);
		when(gateway.getDeviceManager()).thenReturn(deviceManager);
		when(gateway.getDriverManager()).thenReturn(driverManager);
		when(gateway.getCurrentDevice()).thenReturn(currentDevice);
		return gateway;
	}
}
