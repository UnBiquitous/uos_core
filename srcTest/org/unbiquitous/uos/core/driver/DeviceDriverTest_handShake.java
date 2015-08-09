package org.unbiquitous.uos.core.driver;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.ListResourceBundle;
import java.util.ResourceBundle;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.unbiquitous.uos.core.UOS;
import org.unbiquitous.uos.core.adaptabitilyEngine.AdaptabilityEngine;
import org.unbiquitous.uos.core.adaptabitilyEngine.SmartSpaceGateway;
import org.unbiquitous.uos.core.deviceManager.DeviceManager;
import org.unbiquitous.uos.core.driverManager.DriverManager;
import org.unbiquitous.uos.core.messageEngine.dataType.UpDevice;
import org.unbiquitous.uos.core.messageEngine.dataType.UpDriver;
import org.unbiquitous.uos.core.messageEngine.messages.Call;
import org.unbiquitous.uos.core.messageEngine.messages.Response;
import org.unbiquitous.uos.core.ontology.OntologyReasonerTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class DeviceDriverTest_handShake {
	private final ObjectMapper mapper;
	private final JsonNodeFactory factory;
	{
		mapper = new ObjectMapper();
		factory = mapper.getNodeFactory();
	}
	
	private DeviceDriver driver;
	private DeviceManager deviceManager;
	private UpDevice currentDevice;
	private DriverManager driverManager;

	@Before
	public void setUp() throws Exception {
		new File("resources/uoscontext.owl").createNewFile();

		ResourceBundle bundle = new ListResourceBundle() {
			protected Object[][] getContents() {
				return new Object[][] { { "ubiquitos.ontology.path", "resources/uoscontext.owl" },
						{ "ubiquitos.ontology.reasonerFactory", OntologyReasonerTest.class.getName() }, };
			}
		};
		UOS ctx = new UOS();
		ctx.start(bundle);
		currentDevice = ctx.getGateway().getCurrentDevice();
		currentDevice = mapper.readValue(mapper.writeValueAsString(currentDevice), UpDevice.class);

		driver = new DeviceDriver();

		driverManager = ctx.getFactory().get(AdaptabilityEngine.class).driverManager();
		driverManager.deployDriver(driver.getDriver(), driver);
		driverManager.initDrivers(ctx.getGateway(), null);

		deviceManager = ctx.getFactory().get(AdaptabilityEngine.class).deviceManager();
	}

	@After
	public void tearDown() throws Exception {
		new File("resources/uoscontext.owl").delete();
	}

	@Test
	public void registerDeviceOnSystem() throws IOException {
		UpDevice toRegister = new UpDevice("Dummy").addNetworkInterface("HERE", "LOCAL");

		Call call = new Call().addParameter("device", mapper.writeValueAsString(toRegister));

		driver.handshake(call, new Response(), null);
		assertThat(deviceManager.listDevices()).contains(toRegister);
	}

	@Test
	public void returnsTheCurrentDeviceOnSuccess() throws Exception {
		UpDevice toRegister = new UpDevice("Dummy").addNetworkInterface("HERE", "LOCAL");

		Call call = new Call().addParameter("device", mapper.writeValueAsString(toRegister));

		Response response = new Response();
		driver.handshake(call, response, null);
		assertThat(response.getResponseData("device").toString()).isEqualTo(mapper.writeValueAsString(currentDevice));
	}

	@Test
	public void doesNothingWhenTheSameDeviceShowsUpAgain() throws Exception {

		SmartSpaceGateway gateway = mockGateway(currentDevice);

		driver.init(gateway, null, "id");
		when(gateway.callService((UpDevice) any(), (Call) any())).thenReturn(new Response());

		UpDevice toRegister = new UpDevice("Dummy").addNetworkInterface("HERE", "LOCAL");

		Call call = new Call().addParameter("device", mapper.writeValueAsString(toRegister));

		driver.handshake(call, new Response(), null);
		Response response = new Response();
		driver.handshake(call, response, null);

		assertThat(deviceManager.listDevices()).contains(toRegister);
		assertThat(response.getResponseData("device").toString()).isEqualTo(mapper.writeValueAsString(currentDevice));
		assertThat(response.getError()).isNullOrEmpty();
		// FIXME: error when the driver is already registered
		assertThat(deviceManager.listDevices()).contains(toRegister);
	}

	@Test
	public void doesNothingWhenThereIsNoDevice() throws Exception {
		Response response = new Response();
		driver.handshake(new Call(), response, null);

		assertThat(response.getResponseData()).isNullOrEmpty();
		assertThat(response.getError()).isNotEmpty();
	}

	@Test
	public void doesNothingWhenTheDeviceIsNotCorrectlyTransfered() throws Exception {

		Call call = new Call().addParameter("device", "Not valid JSON");
		Response response = new Response();
		driver.handshake(call, response, null);

		assertThat(response.getResponseData()).isNullOrEmpty();
		assertThat(response.getError()).isNotEmpty();
	}

	// TODO: with myself

	@Test
	public void mustNotBreakWhenItsDoneTwice() throws Exception {
		SmartSpaceGateway gateway = mockGateway(new UpDevice());

		ObjectNode driversList = factory.objectNode();
		UpDriver dummyInterface = new UpDriver("ddd");
		dummyInterface.addService("s");
		driversList.set("id_d", mapper.valueToTree(dummyInterface));

		when(gateway.callService((UpDevice) any(), (Call) any()))
				.thenReturn(new Response().addParameter("driverList", driversList));

		driver.init(gateway, null, "id");
		UpDevice toRegister = new UpDevice("Dummy").addNetworkInterface("HERE", "LOCAL");

		Call call = new Call().addParameter("device", mapper.writeValueAsString(toRegister));

		Response response = new Response();
		driver.handshake(call, response, null);
		driver.handshake(call, response, null);

		ArgumentCaptor<Call> getCall = ArgumentCaptor.forClass(Call.class);
		verify(gateway, times(2)).callService(eq(toRegister), getCall.capture());
		assertThat(getCall.getValue().getDriver()).isEqualTo("uos.DeviceDriver");
		assertThat(getCall.getValue().getService()).isEqualTo("listDrivers");

		assertThat(driverManager.listDrivers("ddd", "Dummy")).isNotEmpty();
		assertThat(response.getError()).isNullOrEmpty();
	}

	@Test
	public void mustRegisterTheOtherDriversLocallyAlso() throws Exception {
		SmartSpaceGateway gateway = mockGateway(new UpDevice());

		ObjectNode driversList = factory.objectNode();
		UpDriver dummyInterface = new UpDriver("ddd");
		dummyInterface.addService("s");
		driversList.set("id_d", mapper.valueToTree(dummyInterface));

		when(gateway.callService((UpDevice) any(), (Call) any()))
				.thenReturn(new Response().addParameter("driverList", driversList));

		driver.init(gateway, null, "id");
		UpDevice toRegister = new UpDevice("Dummy").addNetworkInterface("HERE", "LOCAL");

		Call call = new Call().addParameter("device", mapper.writeValueAsString(toRegister));

		Response response = new Response();
		driver.handshake(call, response, null);

		ArgumentCaptor<Call> getCall = ArgumentCaptor.forClass(Call.class);
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
