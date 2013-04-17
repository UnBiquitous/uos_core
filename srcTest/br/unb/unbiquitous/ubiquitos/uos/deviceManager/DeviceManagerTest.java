package br.unb.unbiquitous.ubiquitos.uos.deviceManager;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import br.unb.unbiquitous.json.JSONArray;
import br.unb.unbiquitous.json.JSONException;
import br.unb.unbiquitous.json.JSONObject;
import br.unb.unbiquitous.ubiquitos.network.connectionManager.ConnectionManagerControlCenter;
import br.unb.unbiquitous.ubiquitos.network.exceptions.NetworkException;
import br.unb.unbiquitous.ubiquitos.network.model.NetworkDevice;
import br.unb.unbiquitous.ubiquitos.uos.adaptabitilyEngine.Gateway;
import br.unb.unbiquitous.ubiquitos.uos.adaptabitilyEngine.ServiceCallException;
import br.unb.unbiquitous.ubiquitos.uos.connectivity.ConnectivityManager;
import br.unb.unbiquitous.ubiquitos.uos.driverManager.DriverDao;
import br.unb.unbiquitous.ubiquitos.uos.driverManager.DriverData;
import br.unb.unbiquitous.ubiquitos.uos.driverManager.DriverManager;
import br.unb.unbiquitous.ubiquitos.uos.driverManager.DriverManagerException;
import br.unb.unbiquitous.ubiquitos.uos.driverManager.DriverModel;
import br.unb.unbiquitous.ubiquitos.uos.driverManager.DriverNotFoundException;
import br.unb.unbiquitous.ubiquitos.uos.driverManager.ReflectionServiceCaller;
import br.unb.unbiquitous.ubiquitos.uos.driverManager.drivers.Pointer;
import br.unb.unbiquitous.ubiquitos.uos.messageEngine.dataType.UpDevice;
import br.unb.unbiquitous.ubiquitos.uos.messageEngine.dataType.UpDriver;
import br.unb.unbiquitous.ubiquitos.uos.messageEngine.dataType.UpService;
import br.unb.unbiquitous.ubiquitos.uos.messageEngine.dataType.UpService.ParameterType;
import br.unb.unbiquitous.ubiquitos.uos.messageEngine.dataType.json.JSONDriver;
import br.unb.unbiquitous.ubiquitos.uos.messageEngine.messages.ServiceCall;
import br.unb.unbiquitous.ubiquitos.uos.messageEngine.messages.ServiceResponse;

public class DeviceManagerTest {

	private DeviceDao dao;
	private DeviceManager deviceManager;
	private DriverManager driverManager;
	private UpDevice currentDevice;
	private DriverDao driverDao;
	private ConnectionManagerControlCenter connManager;
	private Gateway gateway;
	private ConnectivityManager proxier;

	@Before
	public void setUp() {
		dao = new DeviceDao(null);
		driverDao = new DriverDao(null);
		connManager = mock(ConnectionManagerControlCenter.class);
		gateway = mock(Gateway.class);
		proxier = mock(ConnectivityManager.class);
		currentDevice = new UpDevice("myDevice").addNetworkInterface(
				"127.0.0.1:80", "Ethernet:TCP");
		driverManager = new DriverManager(currentDevice, driverDao, dao,
				new ReflectionServiceCaller(null));
		deviceManager = new DeviceManager(currentDevice, dao, driverDao,
				connManager, proxier, gateway, driverManager);
	}

	@After
	public void tearDown() {
		((DeviceDao) dao).clear();
		driverDao.clear();
	}

	// public DeviceManager( UpDevice currentDevice,
	// ConnectionManagerControlCenter connectionManagerControlCenter,
	// ConnectivityManager connectivityManager, ResourceBundle resourceBundle,
	// Gateway gateway) {
	@Test
	public void shouldSaveCurrentDeviceOnStart() {
		List<UpDevice> devices = dao.list(null, null);
		assertNotNull(devices);
		assertEquals(1, devices.size());
		assertEquals(currentDevice, devices.get(0));
	}

	// public void registerDevice(UpDevice device){
	@Test
	public void shouldSaveWhenRegistering() {
		UpDevice toRegister = new UpDevice("aDevice").addNetworkInterface(
				"127.0.0.2", "Ethernet:TCP");
		deviceManager.registerDevice(toRegister);
		List<UpDevice> devices = dao.list(null, null);
		assertNotNull(devices);
		assertEquals(2, devices.size());
		assertTrue(devices.contains(toRegister));
	}

	// public UpDevice retrieveDevice(String deviceName){
	@Test
	public void shouldRetrieveRegisteredDevice() {
		UpDevice toRegister = new UpDevice("aDevice").addNetworkInterface(
				"127.0.0.2", "Ethernet:TCP");
		deviceManager.registerDevice(toRegister);
		assertNotNull(deviceManager.retrieveDevice("aDevice"));
		assertEquals(toRegister, deviceManager.retrieveDevice("aDevice"));
	}

	@Test
	public void shouldNotRetrieveAUnRegisteredDevice() {
		UpDevice toRegister = new UpDevice("aDevice").addNetworkInterface(
				"127.0.0.2", "Ethernet:TCP");
		deviceManager.registerDevice(toRegister);
		assertNull(deviceManager.retrieveDevice("NotDevice"));
	}

	// public UpDevice retrieveDevice(String networkAdrress, String
	// networkType){
	@Test
	public void shouldRetrieveARegisteredDeviceByNetworkAddress() {
		UpDevice toRegister = new UpDevice("aDevice").addNetworkInterface(
				"127.0.0.2", "Ethernet:TCP");
		deviceManager.registerDevice(toRegister);
		assertNotNull(deviceManager.retrieveDevice("127.0.0.2", "Ethernet:TCP"));
		assertEquals(toRegister,
				deviceManager.retrieveDevice("127.0.0.2", "Ethernet:TCP"));
	}

	@Test
	public void shouldNotRetrieveAUnRegisteredDeviceByNetworkAddress() {
		UpDevice toRegister = new UpDevice("aDevice").addNetworkInterface(
				"127.0.0.2", "Ethernet:TCP");
		deviceManager.registerDevice(toRegister);
		assertNull(deviceManager.retrieveDevice("127.0.0.3", "Ethernet:TCP"));
		assertNull(deviceManager.retrieveDevice("127.0.0.2", "Ethernet:UDP"));
	}

	
	@Test
	public void listsAllRegisteredDevices() {
		UpDevice first = new UpDevice("firstDevice")
						.addNetworkInterface("127.0.0.1", "Ethernet:TCP");
		deviceManager.registerDevice(first);
		UpDevice second = new UpDevice("secondDevice")
						.addNetworkInterface("127.0.0.2", "Ethernet:TCP");
		deviceManager.registerDevice(second);
		assertThat(deviceManager.listDevices()).containsOnly(currentDevice,first,second);
	}
	
	//TODO: two devices with the same name == trouble
	
	@Test
	public void shouldRetrieveMultipleInstancesByDriverName()
			throws DriverManagerException, DriverNotFoundException {
		UpDriver driver = new UpDriver("d1");
		UpDevice myPhone = new UpDevice("my.Phone");
		deviceManager.registerDevice(myPhone);
		driverManager.insert(new DriverModel("id1", driver, "my.Phone"));
		driverManager.insert(new DriverModel("id2", driver, currentDevice
				.getName()));
		driverManager.insert(new DriverModel("id3", new UpDriver("d3"),
				"my.tablet"));
		List<DriverData> list = driverManager.listDrivers("d1", null);
		assertNotNull(list);
		assertEquals(2, list.size());
		assertEquals("id1", list.get(0).getInstanceID());
		assertEquals(myPhone, list.get(0).getDevice());
		assertEquals("id2", list.get(1).getInstanceID());
		assertEquals(currentDevice, list.get(1).getDevice());
	}

	@Test
	public void shouldRetrieveMultipleInstancesByDeviceName()
			throws DriverManagerException, DriverNotFoundException {
		UpDriver driver = new UpDriver("d1");
				 driver.addService("s1").addParameter("p1", ParameterType.MANDATORY);
		UpDriver driverD3 = new UpDriver("d3");
				 driverD3.addEvent("e1");
		UpDevice myPhone = new UpDevice("my.Phone");
		deviceManager.registerDevice(myPhone);
		driverManager.insert(new DriverModel("id1", driver, "my.Phone"));
		driverManager.insert(new DriverModel("id2", driver, currentDevice
				.getName()));
		driverManager.insert(new DriverModel("id3", driverD3, "my.Phone"));
		List<DriverData> list = driverManager.listDrivers(null, "my.Phone");
		assertNotNull(list);
		assertEquals(2, list.size());
		assertEquals("id1", list.get(0).getInstanceID());
		assertEquals(myPhone, list.get(0).getDevice());
		assertEquals(driver, list.get(0).getDriver());
		assertEquals("id3", list.get(1).getInstanceID());
		assertEquals(myPhone, list.get(1).getDevice());
		assertEquals(driverD3, list.get(1).getDriver());
	}

	@Test
	@SuppressWarnings("unchecked")
	public void ifTheDeviceIsAlreadyKnownShouldDoNothing() throws Exception {
		deviceManager.registerDevice(new UpDevice("IShouldKnow")
				.addNetworkInterface("ADDR_KNOWN", "UNEXISTANT"));
		assertEquals(2, dao.list().size());
		when(connManager.getHost(eq("ADDR_KNOWN"))).thenReturn("ADDR_KNOWN");
		NetworkDevice enteree = mock(NetworkDevice.class);
		when(enteree.getNetworkDeviceName()).thenReturn("ADDR_KNOWN");
		when(enteree.getNetworkDeviceType()).thenReturn("UNEXISTANT");
		deviceManager.deviceEntered(enteree);
		verify(gateway, never()).callService(any(UpDevice.class),
				any(String.class), any(String.class), any(String.class),
				any(String.class), any(Map.class));
		assertEquals(2, dao.list().size());
	}

	// null device or device data (addr and type)
	@Test
	@SuppressWarnings("unchecked")
	public void ifTheDeviceIsNullShouldDoNothing() throws Exception {
		assertEquals(1, dao.list().size());
		deviceManager.deviceEntered(null);
		verify(gateway, never()).callService(any(UpDevice.class),
				any(String.class), any(String.class), any(String.class),
				any(String.class), any(Map.class));
		assertEquals(1, dao.list().size());
	}

	@Test
	@SuppressWarnings("unchecked")
	public void ifTheDeviceProvidesNoDatalShouldDoNothing() throws Exception {
		assertEquals(1, dao.list().size());
		deviceManager.deviceEntered(mock(NetworkDevice.class));
		verify(gateway, never()).callService(any(UpDevice.class),
				any(String.class), any(String.class), any(String.class),
				any(String.class), any(Map.class));
		assertEquals(1, dao.list().size());
	}

	// if doesn't knows the device, call 'handshake' on him passing
	// currentDevice information
	// Then register the device on the database

	@Test
	public void IfDontKnowTheDeviceCallAHandshakeWithIt() throws Exception {
		NetworkDevice enteree = createKnownNetworkDevice();
		deviceManager.deviceEntered(enteree);
		ArgumentCaptor<ServiceCall> scCacther = ArgumentCaptor
				.forClass(ServiceCall.class);
		verify(gateway, times(2)).callService(any(UpDevice.class),
				scCacther.capture());
		ServiceCall parameter = scCacther.getAllValues().get(0);
		assertEquals("handshake", parameter.getService());
		assertEquals("br.unb.unbiquitous.ubiquitos.driver.DeviceDriver",
				parameter.getDriver());
		assertNull(parameter.getInstanceId());
		assertNull(parameter.getSecurityType());
		assertEquals(currentDevice.toString(), parameter.getParameter("device"));
	}

	@Test
	public void IfTheHandShakeWorksRegisterTheReturnedDevice() throws Exception {
		NetworkDevice enteree = createKnownNetworkDevice();
		UpDevice newGuy = new UpDevice("TheNewGuy").addNetworkInterface(
				"ADDR_UNKNOWN", "UNEXISTANT").addNetworkInterface(
				"127.255.255.666", "Ethernet:TFH");
		when(gatewayHandshakeCall())
				.thenReturn(
						new ServiceResponse().addParameter("device",
								newGuy.toString()));
		deviceManager.deviceEntered(enteree);
		assertEquals(2, dao.list().size());
		assertEquals(newGuy, dao.find(newGuy.getName()));
	}
	
	@Test
	public void IfTheHandShakeHappensTwiceDoNothing() throws Exception {
		NetworkDevice enteree = createKnownNetworkDevice();
		UpDevice newGuy = new UpDevice("TheNewGuy")
			.addNetworkInterface("ADDR_UNKNOWN", "UNEXISTANT")
			.addNetworkInterface("127.255.255.666", "Ethernet:TFH");
		when(gatewayHandshakeCall())
				.thenReturn(
						new ServiceResponse().addParameter("device",
								newGuy.toString()));
		deviceManager.deviceEntered(enteree);
		deviceManager.deviceEntered(enteree);
		assertEquals(2, dao.list().size());
		assertEquals(newGuy, dao.find(newGuy.getName()));
	}

	@Test
	public void IfTheHandShakeDoesNotWorksNothingHappens_NoResponse()
			throws Exception {
		NetworkDevice enteree = createKnownNetworkDevice();
		when(gatewayHandshakeCall()).thenReturn(null);
		deviceManager.deviceEntered(enteree);
		assertEquals(1, dao.list().size());
	}

	@Test
	public void IfTheHandShakeDoesNotWorksNothingHappens_NoData()
			throws Exception {
		NetworkDevice enteree = createKnownNetworkDevice();
		when(gatewayHandshakeCall()).thenReturn(new ServiceResponse());
		deviceManager.deviceEntered(enteree);
		assertEquals(1, dao.list().size());
	}

	@Test
	public void IfTheHandShakeDoesNotWorksNothingHappens_ErrorOnResponse()
			throws Exception {
		NetworkDevice enteree = createKnownNetworkDevice();
		ServiceResponse error = new ServiceResponse();
		error.setError("Just Kidding");
		when(gatewayHandshakeCall()).thenReturn(error);
		deviceManager.deviceEntered(enteree);
		assertEquals(1, dao.list().size());
	}

	@Test
	public void IfTheHandShakeDoesNotWorksNothingHappens_Exception()
			throws Exception {
		NetworkDevice enteree = createKnownNetworkDevice();
		when(gatewayHandshakeCall()).thenThrow(new RuntimeException());
		deviceManager.deviceEntered(enteree);
		assertEquals(1, dao.list().size());
	}

	// if doesn't knows the device, call 'listDrivers'
	// Then register the driver instances on the database

	@Test
	public void IfDontKnowTheDeviceCallListDriversOnIt() throws Exception {
		NetworkDevice enteree = createKnownNetworkDevice();
		when(gatewayHandshakeCall()).thenReturn(
				new ServiceResponse().addParameter("device", new UpDevice("A")
						.addNetworkInterface("A", "T").toString()));
		deviceManager.deviceEntered(enteree);
		ServiceCall listDrivers = new ServiceCall(
				"br.unb.unbiquitous.ubiquitos.driver.DeviceDriver",
				"listDrivers", null);
		verify(gateway, times(1)).callService(any(UpDevice.class),
				eq(listDrivers));
	}

	@Test
	public void AfterListingTheDriverTheyMustBeRegistered() throws Exception {
		NetworkDevice enteree = createKnownNetworkDevice();
		when(gatewayHandshakeCall()).thenReturn(
				new ServiceResponse().addParameter("device", new UpDevice("A")
						.addNetworkInterface("A", "T").toString()));
		JSONObject driverList = new JSONObject();
		UpDriver dummy = new UpDriver("DummyDriver");
		dummy.addService("s1").addParameter("s1p1", ParameterType.MANDATORY)
				.addParameter("s1p2", ParameterType.OPTIONAL);
		dummy.addService("s2").addParameter("s2p1", ParameterType.MANDATORY);

		driverList.put("id1", new JSONDriver(dummy));
		driverList.put("id2", new JSONDriver(dummy));
		when(gatewayListDriversCall()).thenReturn(
				new ServiceResponse().addParameter("driverList",
						driverList.toString()));
		deviceManager.deviceEntered(enteree);
		List<DriverModel> newGuyDrivers = driverDao.list(null, "A");
		assertEquals(2, newGuyDrivers.size());
		assertEquals("id1", newGuyDrivers.get(0).id());
		assertEquals(dummy, newGuyDrivers.get(0).driver());
		assertEquals("id2", newGuyDrivers.get(1).id());
		assertEquals(dummy, newGuyDrivers.get(1).driver());
	}

	@Test
	public void shouldNotRegisterADriverDueToInterfaceValidationError()
			throws ServiceCallException, JSONException {
		NetworkDevice enteree = createKnownNetworkDevice();
		UpDevice device = new UpDevice("A").addNetworkInterface("A", "T");

		when(gatewayHandshakeCall())
				.thenReturn(
						new ServiceResponse().addParameter("device",
								device.toString()));

		UpDriver dummy = new UpDriver("DummyDriver");
		dummy.addService("s1");
		dummy.addEquivalentDrivers("equivalentDriver");

		JSONObject driverList = new JSONObject();
		driverList.put("id1", new JSONDriver(dummy));
		
		when(gatewayListDriversCall()).thenReturn(
				new ServiceResponse().addParameter("driverList", driverList.toString()));

		List<JSONDriver> jsonList = new ArrayList<JSONDriver>();

		UpDriver equivalentDriver = new UpDriver("equivalentDriver");
		
		equivalentDriver.addEquivalentDrivers(Pointer.DRIVER_NAME);
		
		UpService service = new UpService(Pointer.MOVE_EVENT);
		service.addParameter(Pointer.AXIS_X, ParameterType.MANDATORY);
		service.addParameter(Pointer.AXIS_Y, ParameterType.MANDATORY);
		equivalentDriver.addEvent(service);
		
		UpService register = new UpService("registerListener").addParameter("eventKey", ParameterType.MANDATORY);
		equivalentDriver.addService(register);
		
		UpService unregister = new UpService("unregisterListener").addParameter("eventKey", ParameterType.OPTIONAL);
		equivalentDriver.addService(unregister);

		jsonList.add(new JSONDriver(equivalentDriver));
		
		when(gatewayTellEquivalentDriverCall()).thenReturn(new ServiceResponse().addParameter("interfaces",new JSONArray(jsonList).toString()));
		
		deviceManager.deviceEntered(enteree);

		List<DriverModel> newGuyDrivers = driverDao.list(null, "A");
		assertEquals(0, newGuyDrivers.size());
	}
	
	@Test
	public void shouldNotRegisterADriverDueToNullEquivalentDriverResponse()
			throws ServiceCallException, JSONException {
		NetworkDevice enteree = createKnownNetworkDevice();
		UpDevice device = new UpDevice("A").addNetworkInterface("A", "T");

		when(gatewayHandshakeCall())
				.thenReturn(
						new ServiceResponse().addParameter("device",
								device.toString()));

		UpDriver dummy = new UpDriver("DummyDriver");
		dummy.addService("s1");
		dummy.addEquivalentDrivers("equivalentDriver");

		JSONObject driverList = new JSONObject();
		driverList.put("id1", new JSONDriver(dummy));
		
		when(gatewayListDriversCall()).thenReturn(
				new ServiceResponse().addParameter("driverList", driverList.toString()));

		when(gatewayTellEquivalentDriverCall()).thenReturn(null);
		
		deviceManager.deviceEntered(enteree);

		List<DriverModel> newGuyDrivers = driverDao.list(null, "A");
		assertEquals(0, newGuyDrivers.size());
	}
	
	@Test
	public void shouldNotRegisterADriverDueToInterfaceNotProvided()
			throws ServiceCallException, JSONException {
		NetworkDevice enteree = createKnownNetworkDevice();
		UpDevice device = new UpDevice("A").addNetworkInterface("A", "T");

		when(gatewayHandshakeCall())
				.thenReturn(
						new ServiceResponse().addParameter("device",
								device.toString()));

		UpDriver dummy = new UpDriver("DummyDriver");
		dummy.addService("s1");
		dummy.addEquivalentDrivers("equivalentDriver");

		JSONObject driverList = new JSONObject();
		driverList.put("id1", new JSONDriver(dummy));
		
		when(gatewayListDriversCall()).thenReturn(
				new ServiceResponse().addParameter("driverList", driverList.toString()));

		when(gatewayTellEquivalentDriverCall()).thenReturn(
				new ServiceResponse().addParameter("interfaces", null));
		
		deviceManager.deviceEntered(enteree);

		List<DriverModel> newGuyDrivers = driverDao.list(null, "A");
		assertEquals(0, newGuyDrivers.size());
	}
	
	@Test
	public void shouldNotRegisterADriverDueToWrongJSONObject()
			throws ServiceCallException, JSONException {
		NetworkDevice enteree = createKnownNetworkDevice();
		UpDevice device = new UpDevice("A").addNetworkInterface("A", "T");

		when(gatewayHandshakeCall())
				.thenReturn(
						new ServiceResponse().addParameter("device",
								device.toString()));

		UpDriver dummy = new UpDriver("DummyDriver");
		dummy.addService("s1");
		dummy.addEquivalentDrivers(Pointer.DRIVER_NAME);

		when(gatewayListDriversCall()).thenReturn(
				new ServiceResponse().addParameter("driverList", new String("wrongJSONObject")));

		deviceManager.deviceEntered(enteree);

		List<DriverModel> newGuyDrivers = driverDao.list(null, "A");
		assertEquals(0, newGuyDrivers.size());
	}
	
	@Test
	public void shouldNotRegisterADriverDueToMissingServer()
			throws ServiceCallException, JSONException {
		NetworkDevice enteree = createKnownNetworkDevice();
		UpDevice device = new UpDevice("A").addNetworkInterface("A", "T");

		when(gatewayHandshakeCall())
				.thenReturn(
						new ServiceResponse().addParameter("device",
								device.toString()));

		JSONObject driverList = new JSONObject();
		UpDriver dummy = new UpDriver("DummyDriver");
		dummy.addService("s1");
		dummy.addEquivalentDrivers(Pointer.DRIVER_NAME);

		driverList.put("id1", new JSONDriver(dummy));

		when(gatewayListDriversCall()).thenReturn(
				new ServiceResponse().addParameter("driverList",
						driverList.toString()));

		deviceManager.deviceEntered(enteree);

		List<DriverModel> newGuyDrivers = driverDao.list(null, "A");
		assertEquals(0, newGuyDrivers.size());
	}
	
	@Test
	public void shouldNotRegisterADriverDueToEquivalentDriverInterfaceValidationError()
			throws ServiceCallException, JSONException {
		NetworkDevice enteree = createKnownNetworkDevice();
		UpDevice device = new UpDevice("A").addNetworkInterface("A", "T");

		when(gatewayHandshakeCall())
				.thenReturn(
						new ServiceResponse().addParameter("device",
								device.toString()));

		JSONObject driverList = new JSONObject();
		UpDriver dummy = new UpDriver("DummyDriver");
		dummy.addService("s1");
		dummy.addEquivalentDrivers("equivalentDriver");

		driverList.put("id1", new JSONDriver(dummy));

		when(gatewayListDriversCall()).thenReturn(
				new ServiceResponse().addParameter("driverList",
						driverList.toString()));

		List<JSONDriver> jsonList = new ArrayList<JSONDriver>();

		UpDriver equivalentDriver = new UpDriver("equivalentDriver");
		equivalentDriver.addEquivalentDrivers(Pointer.DRIVER_NAME);
		equivalentDriver.addService("s1");

		jsonList.add(new JSONDriver(equivalentDriver));

		when(gatewayTellEquivalentDriverCall()).thenReturn(
				new ServiceResponse().addParameter("interfaces", new JSONArray(
						jsonList).toString()));

		deviceManager.deviceEntered(enteree);

		List<DriverModel> newGuyDrivers = driverDao.list(null, "A");
		assertEquals(0, newGuyDrivers.size());
	}
	
	@Test
	public void shouldNotRegisterADriverWithEquivalentDriverNotInformed()
			throws ServiceCallException, JSONException {
		NetworkDevice enteree = createKnownNetworkDevice();
		UpDevice device = new UpDevice("A").addNetworkInterface("A", "T");

		when(gatewayHandshakeCall())
				.thenReturn(
						new ServiceResponse().addParameter("device",
								device.toString()));

		JSONObject driverList = new JSONObject();
		UpDriver dummy = new UpDriver("DummyDriver");
		dummy.addService("s1");
		dummy.addEquivalentDrivers("equivalentDriver");

		driverList.put("id1", new JSONDriver(dummy));

		when(gatewayListDriversCall()).thenReturn(
				new ServiceResponse().addParameter("driverList",
						driverList.toString()));

		List<JSONDriver> jsonList = new ArrayList<JSONDriver>();

		UpDriver equivalentDriver = new UpDriver("equivalentDriver");
		equivalentDriver.addService("s1");
		equivalentDriver.addEquivalentDrivers("notInformedEquivalentDriver");

		jsonList.add(new JSONDriver(equivalentDriver));

		when(gatewayTellEquivalentDriverCall()).thenReturn(
				new ServiceResponse().addParameter("interfaces", new JSONArray(
						jsonList).toString()));

		deviceManager.deviceEntered(enteree);

		List<DriverModel> newGuyDrivers = driverDao.list(null, "A");
		assertEquals(0, newGuyDrivers.size());
	}
	
	public void shouldNotRegisterADriverWithTwoEquivalentDriversNotInformed()
			throws ServiceCallException, JSONException {
		NetworkDevice enteree = createKnownNetworkDevice();
		UpDevice device = new UpDevice("A").addNetworkInterface("A", "T");

		when(gatewayHandshakeCall())
				.thenReturn(
						new ServiceResponse().addParameter("device",
								device.toString()));

		JSONObject driverList = new JSONObject();
		UpDriver dummy = new UpDriver("DummyDriver");
		dummy.addService("s1");
		dummy.addEquivalentDrivers("equivalentDriver1");
		dummy.addEquivalentDrivers("equivalentDriver2");

		driverList.put("id1", new JSONDriver(dummy));

		when(gatewayListDriversCall()).thenReturn(
				new ServiceResponse().addParameter("driverList",
						driverList.toString()));

		List<JSONDriver> jsonList = new ArrayList<JSONDriver>();

		UpDriver equivalentDriver1 = new UpDriver("equivalentDriver1");
		equivalentDriver1.addService("s1");
		equivalentDriver1.addEquivalentDrivers("notInformedEquivalentDriver1");
		UpDriver equivalentDriver2 = new UpDriver("equivalentDriver2");
		equivalentDriver2.addEquivalentDrivers("notInformedEquivalentDriver2");
		equivalentDriver2.addService("s1");

		jsonList.add(new JSONDriver(equivalentDriver1));
		jsonList.add(new JSONDriver(equivalentDriver2));

		when(gatewayTellEquivalentDriverCall()).thenReturn(
				new ServiceResponse().addParameter("interfaces", new JSONArray(
						jsonList).toString()));

		deviceManager.deviceEntered(enteree);

		List<DriverModel> newGuyDrivers = driverDao.list(null, "A");
		assertEquals(0, newGuyDrivers.size());
	}
	
	@Test
	public void shouldRegisterADriverWithUnknownEquivalentDriver()
			throws ServiceCallException, JSONException {
		NetworkDevice enteree = createKnownNetworkDevice();
		UpDevice device = new UpDevice("A").addNetworkInterface("A", "T");

		when(gatewayHandshakeCall())
				.thenReturn(
						new ServiceResponse().addParameter("device",
								device.toString()));

		JSONObject driverList = new JSONObject();
		UpDriver dummy = new UpDriver("DummyDriver");
		dummy.addService("s1");
		dummy.addEquivalentDrivers("equivalentDriver");

		driverList.put("id1", new JSONDriver(dummy));

		when(gatewayListDriversCall()).thenReturn(
				new ServiceResponse().addParameter("driverList",
						driverList.toString()));

		List<JSONDriver> jsonList = new ArrayList<JSONDriver>();

		UpDriver equivalentDriver = new UpDriver("equivalentDriver");
		equivalentDriver.addService("s1");

		jsonList.add(new JSONDriver(equivalentDriver));

		when(gatewayTellEquivalentDriverCall()).thenReturn(
				new ServiceResponse().addParameter("interfaces", new JSONArray(
						jsonList).toString()));

		deviceManager.deviceEntered(enteree);

		List<DriverModel> newGuyDrivers = driverDao.list(null, "A");
		assertEquals(1, newGuyDrivers.size());
		assertEquals("id1", newGuyDrivers.get(0).id());
		assertEquals(dummy, newGuyDrivers.get(0).driver());
	}

	@Test
	public void shouldRegisterTwoDriversWithTheSameUnknownEquivalentDriver()
			throws ServiceCallException, JSONException {
		NetworkDevice enteree = createKnownNetworkDevice();
		UpDevice device = new UpDevice("A").addNetworkInterface("A", "T");

		when(gatewayHandshakeCall())
				.thenReturn(
						new ServiceResponse().addParameter("device",
								device.toString()));

		JSONObject driverList = new JSONObject();

		UpDriver dummy1 = new UpDriver("DummyDriver1");
		dummy1.addService("s1");
		dummy1.addEquivalentDrivers("equivalentDriver");

		UpDriver dummy2 = new UpDriver("DummyDriver2");
		dummy2.addService("s1");
		dummy2.addEquivalentDrivers("equivalentDriver");

		driverList.put("iddummy1", new JSONDriver(dummy1));
		driverList.put("iddummy2", new JSONDriver(dummy2));

		when(gatewayListDriversCall()).thenReturn(
				new ServiceResponse().addParameter("driverList",
						driverList.toString()));

		List<JSONDriver> jsonList = new ArrayList<JSONDriver>();

		UpDriver equivalentDriver = new UpDriver("equivalentDriver");
		equivalentDriver.addService("s1");

		jsonList.add(new JSONDriver(equivalentDriver));

		when(gatewayTellEquivalentDriverCall()).thenReturn(
				new ServiceResponse().addParameter("interfaces", new JSONArray(
						jsonList).toString()));

		deviceManager.deviceEntered(enteree);

		List<DriverModel> newGuyDrivers = driverDao.list(null, "A");

		assertEquals(2, newGuyDrivers.size());
		assertEquals("iddummy1", newGuyDrivers.get(0).id());
		assertEquals(dummy1, newGuyDrivers.get(0).driver());
		assertEquals("iddummy2", newGuyDrivers.get(1).id());
		assertEquals(dummy2, newGuyDrivers.get(1).driver());
	}

	@Test
	public void shouldRegisterADriverWithTwoLevelsOfUnknownEquivalentDrivers()
			throws ServiceCallException, JSONException {
		NetworkDevice enteree = createKnownNetworkDevice();
		UpDevice device = new UpDevice("A").addNetworkInterface("A", "T");

		when(gatewayHandshakeCall())
				.thenReturn(
						new ServiceResponse().addParameter("device",
								device.toString()));

		JSONObject driverList = new JSONObject();
		UpDriver dummy = new UpDriver("DummyDriver");
		dummy.addService("s1");
		dummy.addEquivalentDrivers("equivalentDriver");

		driverList.put("id1", new JSONDriver(dummy));

		when(gatewayListDriversCall()).thenReturn(
				new ServiceResponse().addParameter("driverList",
						driverList.toString()));

		UpDriver equivalentDriver = new UpDriver("equivalentDriver");
		equivalentDriver.addService("s1");
		equivalentDriver
				.addEquivalentDrivers("equivalentToTheEquivalentDriver");

		UpDriver equivalentToTheEquivalentDriver = new UpDriver(
				"equivalentToTheEquivalentDriver");
		equivalentToTheEquivalentDriver.addService("s1");

		List<JSONDriver> equivalentDrivers = new ArrayList<JSONDriver>();
		equivalentDrivers.add(new JSONDriver(equivalentDriver));
		equivalentDrivers.add(new JSONDriver(equivalentToTheEquivalentDriver));

		when(gatewayTellEquivalentDriverCall()).thenReturn(
				new ServiceResponse().addParameter("interfaces", new JSONArray(
						equivalentDrivers).toString()));

		deviceManager.deviceEntered(enteree);
		List<DriverModel> newGuyDrivers = driverDao.list(null, "A");

		// verify(gatewayTellEquivalentDriverCall(), times(2));
		assertEquals(1, newGuyDrivers.size());
		assertEquals("id1", newGuyDrivers.get(0).id());
		assertEquals(dummy, newGuyDrivers.get(0).driver());
	}
	
	@Test
	public void shouldRegisterTwoDriversWithTheSameUnknownEquivalentDriverFromDifferentLevels()
			throws ServiceCallException, JSONException {
		NetworkDevice enteree = createKnownNetworkDevice();
		UpDevice device = new UpDevice("A").addNetworkInterface("A", "T");
		
		when(gatewayHandshakeCall())
		.thenReturn(
				new ServiceResponse().addParameter("device",
						device.toString()));
		
		JSONObject driverList = new JSONObject();
		
		UpDriver dummy1 = new UpDriver("D1");
		dummy1.addService("s1");
		dummy1.addEquivalentDrivers("D0");
		
		UpDriver dummy2 = new UpDriver("D4");
		dummy2.addService("s1");
		dummy2.addEquivalentDrivers("D3");
		
		driverList.put("iddummy1", new JSONDriver(dummy1));
		driverList.put("iddummy2", new JSONDriver(dummy2));
		
		when(gatewayListDriversCall()).thenReturn(
				new ServiceResponse().addParameter("driverList",
						driverList.toString()));
		
		List<JSONDriver> jsonList = new ArrayList<JSONDriver>();
		
		UpDriver d0 = new UpDriver("D0");
		d0.addService("s1");
		
		UpDriver d2 = new UpDriver("D2");
		d2.addService("s1");
		d2.addEquivalentDrivers(d0.getName());
		
		UpDriver d3 = new UpDriver("D3");
		d3.addService("s1");
		d3.addEquivalentDrivers(d2.getName());
		
		jsonList.add(new JSONDriver(d3));
		jsonList.add(new JSONDriver(d0));
		jsonList.add(new JSONDriver(d2));
		
		when(gatewayTellTwoEquivalentDrivers()).thenReturn(
				new ServiceResponse().addParameter("interfaces", new JSONArray(
						jsonList).toString()));
		
		deviceManager.deviceEntered(enteree);
		
		List<DriverModel> newGuyDrivers = driverDao.list(null, "A");
		
		//TODO: This
		assertEquals(2, newGuyDrivers.size());
		if (newGuyDrivers.get(0).id().equals("iddummy1")){
			assertEquals("iddummy1", newGuyDrivers.get(0).id());
			assertEquals(dummy1, newGuyDrivers.get(0).driver());
			assertEquals("iddummy2", newGuyDrivers.get(1).id());
			assertEquals(dummy2, newGuyDrivers.get(1).driver());
		}else{
			assertEquals("iddummy2", newGuyDrivers.get(0).id());
			assertEquals(dummy2, newGuyDrivers.get(0).driver());
			assertEquals("iddummy1", newGuyDrivers.get(1).id());
			assertEquals(dummy1, newGuyDrivers.get(1).driver());
		}
	}

	@Test
	public void IfTheListDriversDoesNotWorksNothingHappens_NoResponse()
			throws Exception {
		NetworkDevice enteree = createKnownNetworkDevice();
		when(gatewayHandshakeCall()).thenReturn(
				new ServiceResponse().addParameter("device", new UpDevice("A")
						.addNetworkInterface("A", "T").toString()));
		when(gatewayListDriversCall()).thenReturn(null);
		deviceManager.deviceEntered(enteree);
		assertEquals(0, driverDao.list(null, "A").size());
	}

	@Test
	public void IfTheListDriversDoesNotWorksNothingHappens_NoData()
			throws Exception {
		NetworkDevice enteree = createKnownNetworkDevice();
		when(gatewayHandshakeCall()).thenReturn(
				new ServiceResponse().addParameter("device", new UpDevice("A")
						.addNetworkInterface("A", "T").toString()));
		when(gatewayListDriversCall()).thenReturn(new ServiceResponse());
		deviceManager.deviceEntered(enteree);
		assertEquals(0, driverDao.list(null, "A").size());
	}

	
	//FIXME: [B&M] Sempre passa! Como testar caso de erro?!
	@Test
	public void shouldNotHandshakeWithItsSelf() throws NetworkException, ServiceCallException, JSONException {
		NetworkDevice enteree = mock(NetworkDevice.class);
		
		when(enteree.getNetworkDeviceName()).thenReturn("127.0.0.1:8080");		
		when(enteree.getNetworkDeviceType()).thenReturn("Ethernet:TCP");
		when(connManager.getHost("127.0.0.1:8080")).thenReturn("127.0.0.1");
		when(connManager.getHost("127.0.0.1:80")).thenReturn("127.0.0.1");
		when(gatewayHandshakeCall()).thenReturn(
				new ServiceResponse().addParameter("device", new UpDevice("myDevice").addNetworkInterface("127.0.0.1:8080", "Ethernet:TCP").toString()));
		
		JSONObject driverList = new JSONObject();
		driverList.put("id1", new JSONDriver(new UpDriver("DummyDriver")));

		when(gatewayListDriversCall()).thenReturn(
				new ServiceResponse().addParameter("driverList",
						driverList.toString()));
		
		System.out.println("---------------------- AQUI EMBAIXO ----------------------");
		deviceManager.deviceEntered(enteree);
		System.out.println("---------------------- AQUI EM CIMA ----------------------");
	}
	

	@Test
	public void IfTheListDriversDoesNotWorksNothingHappens_ThrowingException()
			throws Exception {
		NetworkDevice enteree = createKnownNetworkDevice();
		when(gatewayHandshakeCall()).thenReturn(
				new ServiceResponse().addParameter("device", new UpDevice("A")
						.addNetworkInterface("A", "T").toString()));
		when(gatewayListDriversCall()).thenThrow(new RuntimeException());
		deviceManager.deviceEntered(enteree);
		assertEquals(0, driverDao.list(null, "A").size());
	}

	private NetworkDevice createKnownNetworkDevice() {
		NetworkDevice enteree = mock(NetworkDevice.class);
		when(enteree.getNetworkDeviceName()).thenReturn("ADDR_UNKNOWN");
		when(enteree.getNetworkDeviceType()).thenReturn("UNEXISTANT");
		return enteree;
	}

	private ServiceResponse gatewayHandshakeCall() throws ServiceCallException {
		ServiceCall handshake = new ServiceCall(
				"br.unb.unbiquitous.ubiquitos.driver.DeviceDriver",
				"handshake", null);
		handshake.addParameter("device", currentDevice.toString());
		return gateway.callService(any(UpDevice.class), eq(handshake));
	}

	private ServiceResponse gatewayListDriversCall()
			throws ServiceCallException {
		ServiceCall listDrivers = new ServiceCall(
				"br.unb.unbiquitous.ubiquitos.driver.DeviceDriver",
				"listDrivers", null);
		return gateway.callService(any(UpDevice.class), eq(listDrivers));
	}

	private ServiceResponse gatewayTellEquivalentDriverCall()
			throws ServiceCallException, JSONException {
		ServiceCall tellEquivalentDriver = new ServiceCall(
				"br.unb.unbiquitous.ubiquitos.driver.DeviceDriver",
				"tellEquivalentDrivers", null);
		List<String> equivalents = new ArrayList<String>();
		equivalents.add("equivalentDriver");
		tellEquivalentDriver.addParameter("driversName", new JSONArray(
				equivalents).toString());
		return gateway.callService(any(UpDevice.class),
				eq(tellEquivalentDriver));
	}

	private ServiceResponse gatewayTellTwoEquivalentDrivers()
			throws ServiceCallException, JSONException {
		ServiceCall tellEquivalentDriver = new ServiceCall(
				"br.unb.unbiquitous.ubiquitos.driver.DeviceDriver",
				"tellEquivalentDrivers", null);
		List<String> equivalents = new ArrayList<String>();
		equivalents.add("D3");
		equivalents.add("D0");
		tellEquivalentDriver.addParameter("driversName", new JSONArray(
				equivalents).toString());
		return gateway.callService(any(UpDevice.class),
				eq(tellEquivalentDriver));
	}
	// TODO: What about proxying
	@Test
	public void AfterListingIfProxyingIsEnabledRegisterIt() throws Exception {
		NetworkDevice enteree = createKnownNetworkDevice();
		when(gatewayHandshakeCall()).thenReturn(
				new ServiceResponse().addParameter("device", new UpDevice("A")
						.addNetworkInterface("A", "T").toString()));
		JSONObject driverList = new JSONObject();
		UpDriver dummy = new UpDriver("DummyDriver");
		dummy.addService("s1").addParameter("s1p1", ParameterType.MANDATORY)
				.addParameter("s1p2", ParameterType.OPTIONAL);
		dummy.addService("s2").addParameter("s2p1", ParameterType.MANDATORY);

		driverList.put("id1", new JSONDriver(dummy));
		driverList.put("id2", new JSONDriver(dummy));
		when(gatewayListDriversCall()).thenReturn(
				new ServiceResponse().addParameter("driverList",
						driverList.toString()));
		when(proxier.doProxying()).thenReturn(true);
		deviceManager.deviceEntered(enteree);
		ArgumentCaptor<UpDevice> deviceCatcher = ArgumentCaptor
				.forClass(UpDevice.class);
		ArgumentCaptor<String> idCatcher = ArgumentCaptor
				.forClass(String.class);
		verify(proxier, times(2)).registerProxyDriver(eq(dummy),
				deviceCatcher.capture(), idCatcher.capture());
		assertEquals("A", deviceCatcher.getValue().getName());
		assertEquals("id2", idCatcher.getAllValues().get(0));
		assertEquals("id1", idCatcher.getAllValues().get(1));
	}

	@Test
	public void AfterListingIfProxyingIsNotEnabledDoNothing() throws Exception {
		NetworkDevice enteree = createKnownNetworkDevice();
		when(gatewayHandshakeCall()).thenReturn(
				new ServiceResponse().addParameter("device", new UpDevice("A")
						.addNetworkInterface("A", "T").toString()));
		JSONObject driverList = new JSONObject();
		UpDriver dummy = new UpDriver("DummyDriver");
		dummy.addService("s1").addParameter("s1p1", ParameterType.MANDATORY)
				.addParameter("s1p2", ParameterType.OPTIONAL);
		dummy.addService("s2").addParameter("s2p1", ParameterType.MANDATORY);

		driverList.put("id1", new JSONDriver(dummy));
		driverList.put("id2", new JSONDriver(dummy));
		when(gatewayListDriversCall()).thenReturn(
				new ServiceResponse().addParameter("driverList",
						driverList.toString()));
		when(proxier.doProxying()).thenReturn(false);
		deviceManager.deviceEntered(enteree);
		verify(proxier, never()).registerProxyDriver(any(UpDriver.class),
				any(UpDevice.class), any(String.class));
	}

	// public void deviceLeft(NetworkDevice device) {
	@Test
	public void removeTheDeviceFromDatabaseOnLeft() throws Exception {
		NetworkDevice leavingGuy = createKnownNetworkDevice();
		UpDevice oldGuy = new UpDevice("OldMan").addNetworkInterface(
				leavingGuy.getNetworkDeviceName(),
				leavingGuy.getNetworkDeviceType());
		when(connManager.getHost(eq(leavingGuy.getNetworkDeviceName())))
				.thenReturn("ADDR_UNKNOWN");
		deviceManager.registerDevice(oldGuy);
		assertEquals(2, dao.list().size());
		UpDriver driver = new UpDriver("DD");
		driver.addService("s");
		driverDao.insert(new DriverModel("id1", driver, oldGuy.getName()));
		driverDao.insert(new DriverModel("id2", driver, oldGuy.getName()));
		driverDao.insert(new DriverModel("id3", driver, oldGuy.getName()));
		assertEquals(3, driverDao.list().size());

		deviceManager.deviceLeft(leavingGuy);
		assertEquals(1, dao.list().size());
		assertEquals(0, driverDao.list().size());
	}

	@Test
	public void doNothingWhenLeftingANullDevice() throws Exception {
		NetworkDevice leavingGuy = createKnownNetworkDevice();
		UpDevice oldGuy = new UpDevice("OldMan").addNetworkInterface(
				leavingGuy.getNetworkDeviceName(),
				leavingGuy.getNetworkDeviceType());
		when(connManager.getHost(eq(leavingGuy.getNetworkDeviceName())))
				.thenReturn("ADDR_UNKNOWN");
		deviceManager.registerDevice(oldGuy);
		assertEquals(2, dao.list().size());
		UpDriver driver = new UpDriver("DD");
		driver.addService("s");
		driverDao.insert(new DriverModel("id1", driver, oldGuy.getName()));
		driverDao.insert(new DriverModel("id2", driver, oldGuy.getName()));
		driverDao.insert(new DriverModel("id3", driver, oldGuy.getName()));
		assertEquals(3, driverDao.list().size());

		deviceManager.deviceLeft(null);
		assertEquals(2, dao.list().size());
		assertEquals(3, driverDao.list().size());
	}

	@Test
	public void doNothingWhenLeftingADeviceWithNullData() throws Exception {
		NetworkDevice leavingGuy = createKnownNetworkDevice();
		UpDevice oldGuy = new UpDevice("OldMan").addNetworkInterface(
				leavingGuy.getNetworkDeviceName(),
				leavingGuy.getNetworkDeviceType());
		deviceManager.registerDevice(oldGuy);
		assertEquals(2, dao.list().size());
		UpDriver driver = new UpDriver("DD");
		driver.addService("s");
		driverDao.insert(new DriverModel("id1", driver, oldGuy.getName()));
		driverDao.insert(new DriverModel("id2", driver, oldGuy.getName()));
		driverDao.insert(new DriverModel("id3", driver, oldGuy.getName()));
		assertEquals(3, driverDao.list().size());

		deviceManager.deviceLeft(mock(NetworkDevice.class));
		assertEquals(2, dao.list().size());
		assertEquals(3, driverDao.list().size());
	}

	@Test
	public void doNothingWhenLeftingAnUnkownDevice() throws Exception {
		NetworkDevice leavingGuy = createKnownNetworkDevice();
		assertEquals(1, dao.list().size());
		UpDriver driver = new UpDriver("DD");
		driver.addService("s");
		driverDao
				.insert(new DriverModel("id1", driver, currentDevice.getName()));
		driverDao
				.insert(new DriverModel("id2", driver, currentDevice.getName()));
		assertEquals(2, driverDao.list().size());

		deviceManager.deviceLeft(leavingGuy);
		assertEquals(1, dao.list().size());
		assertEquals(2, driverDao.list().size());
	}
}
