package org.unbiquitous.uos.core.deviceManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.unbiquitous.uos.core.adaptabitilyEngine.Gateway;
import org.unbiquitous.uos.core.adaptabitilyEngine.ServiceCallException;
import org.unbiquitous.uos.core.connectivity.ConnectivityManager;
import org.unbiquitous.uos.core.driverManager.*;
import org.unbiquitous.uos.core.driverManager.drivers.Pointer;
import org.unbiquitous.uos.core.messageEngine.dataType.UpDevice;
import org.unbiquitous.uos.core.messageEngine.dataType.UpDriver;
import org.unbiquitous.uos.core.messageEngine.dataType.UpService;
import org.unbiquitous.uos.core.messageEngine.dataType.UpService.ParameterType;
import org.unbiquitous.uos.core.messageEngine.messages.Call;
import org.unbiquitous.uos.core.messageEngine.messages.Response;
import org.unbiquitous.uos.core.network.connectionManager.ConnectionManagerControlCenter;
import org.unbiquitous.uos.core.network.exceptions.NetworkException;
import org.unbiquitous.uos.core.network.model.NetworkDevice;

import java.io.IOException;
import java.util.*;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

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
    @SuppressWarnings("rawtypes")
    public void setUp() {
        dao = new DeviceDao(null);
        driverDao = new DriverDao(null);
        connManager = mock(ConnectionManagerControlCenter.class);
        when(connManager.getHost(anyString())).thenAnswer(new Answer() {
            @Override
            public String answer(InvocationOnMock invocation) throws Throwable {
                return (String) invocation.getArguments()[0];
            }
        });
        gateway = mock(Gateway.class);
        proxier = mock(ConnectivityManager.class);
        currentDevice = new UpDevice("myDevice").addNetworkInterface("127.0.0.1:80", "Ethernet:TCP");
        driverManager = new DriverManager(currentDevice, driverDao, dao, new ReflectionServiceCaller(null));
        deviceManager = new DeviceManager(currentDevice, dao, driverDao, connManager, proxier, gateway, driverManager);
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
        UpDevice toRegister = new UpDevice("aDevice").addNetworkInterface("127.0.0.2", "Ethernet:TCP");
        deviceManager.registerDevice(toRegister);
        List<UpDevice> devices = dao.list(null, null);
        assertNotNull(devices);
        assertEquals(2, devices.size());
        assertTrue(devices.contains(toRegister));
    }

    // public UpDevice retrieveDevice(String deviceName){
    @Test
    public void shouldRetrieveRegisteredDevice() {
        UpDevice toRegister = new UpDevice("aDevice").addNetworkInterface("127.0.0.2", "Ethernet:TCP");
        deviceManager.registerDevice(toRegister);
        assertNotNull(deviceManager.retrieveDevice("aDevice"));
        assertEquals(toRegister, deviceManager.retrieveDevice("aDevice"));
    }

    @Test
    public void shouldNotRetrieveAUnRegisteredDevice() {
        UpDevice toRegister = new UpDevice("aDevice").addNetworkInterface("127.0.0.2", "Ethernet:TCP");
        deviceManager.registerDevice(toRegister);
        assertNull(deviceManager.retrieveDevice("NotDevice"));
    }

    // public UpDevice retrieveDevice(String networkAdrress, String
    // networkType){
    @Test
    public void shouldRetrieveARegisteredDeviceByNetworkAddress() {
        UpDevice toRegister = new UpDevice("aDevice").addNetworkInterface("127.0.0.2", "Ethernet:TCP");
        deviceManager.registerDevice(toRegister);
        assertNotNull(deviceManager.retrieveDevice("127.0.0.2", "Ethernet:TCP"));
        assertEquals(toRegister, deviceManager.retrieveDevice("127.0.0.2", "Ethernet:TCP"));
    }

    @Test
    public void shouldNotRetrieveAUnRegisteredDeviceByNetworkAddress() {
        UpDevice toRegister = new UpDevice("aDevice").addNetworkInterface("127.0.0.2", "Ethernet:TCP");
        deviceManager.registerDevice(toRegister);
        assertNull(deviceManager.retrieveDevice("127.0.0.3", "Ethernet:TCP"));
        assertNull(deviceManager.retrieveDevice("127.0.0.2", "Ethernet:UDP"));
    }

    @Test
    public void listsAllRegisteredDevices() {
        UpDevice first = new UpDevice("firstDevice").addNetworkInterface("127.0.0.1", "Ethernet:TCP");
        deviceManager.registerDevice(first);
        UpDevice second = new UpDevice("secondDevice").addNetworkInterface("127.0.0.2", "Ethernet:TCP");
        deviceManager.registerDevice(second);
        assertThat(deviceManager.listDevices()).containsOnly(currentDevice, first, second);
    }

    // TODO: two devices with the same name == trouble

    @Test
    public void shouldRetrieveMultipleInstancesByDriverName() throws DriverManagerException, DriverNotFoundException {
        UpDriver driver = new UpDriver("d1");
        UpDevice myPhone = new UpDevice("my.Phone");
        deviceManager.registerDevice(myPhone);
        driverManager.insert(new DriverModel("id1", driver, "my.Phone"));
        driverManager.insert(new DriverModel("id2", driver, currentDevice.getName()));
        driverManager.insert(new DriverModel("id3", new UpDriver("d3"), "my.tablet"));
        List<DriverData> list = driverManager.listDrivers("d1", null);
        assertNotNull(list);
        assertEquals(2, list.size());
        assertEquals("id1", list.get(0).getInstanceID());
        assertEquals(myPhone, list.get(0).getDevice());
        assertEquals("id2", list.get(1).getInstanceID());
        assertEquals(currentDevice, list.get(1).getDevice());
    }

    @Test
    public void shouldRetrieveMultipleInstancesByDeviceName() throws DriverManagerException, DriverNotFoundException {
        UpDriver driver = new UpDriver("d1");
        driver.addService("s1").addParameter("p1", ParameterType.MANDATORY);
        UpDriver driverD3 = new UpDriver("d3");
        driverD3.addEvent("e1");
        UpDevice myPhone = new UpDevice("my.Phone");
        deviceManager.registerDevice(myPhone);
        driverManager.insert(new DriverModel("id1", driver, "my.Phone"));
        driverManager.insert(new DriverModel("id2", driver, currentDevice.getName()));
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
        deviceManager.registerDevice(new UpDevice("IShouldKnow").addNetworkInterface("ADDR_KNOWN", "INEXISTENT"));
        assertEquals(2, dao.list().size());
        when(connManager.getHost(eq("ADDR_KNOWN"))).thenReturn("ADDR_KNOWN");
        NetworkDevice enteree = mock(NetworkDevice.class);
        when(enteree.getNetworkDeviceName()).thenReturn("ADDR_KNOWN");
        when(enteree.getNetworkDeviceType()).thenReturn("INEXISTENT");
        deviceManager.deviceEntered(enteree);
        verify(gateway, never()).callService(any(UpDevice.class), any(String.class), any(String.class),
                any(String.class), any(String.class), any(Map.class));
        assertEquals(2, dao.list().size());
    }

    // null device or device data (addr and type)
    @Test
    @SuppressWarnings("unchecked")
    public void ifTheDeviceIsNullShouldDoNothing() throws Exception {
        assertEquals(1, dao.list().size());
        deviceManager.deviceEntered(null);
        verify(gateway, never()).callService(any(UpDevice.class), any(String.class), any(String.class),
                any(String.class), any(String.class), any(Map.class));
        assertEquals(1, dao.list().size());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void ifTheDeviceProvidesNoDatalShouldDoNothing() throws Exception {
        assertEquals(1, dao.list().size());
        deviceManager.deviceEntered(mock(NetworkDevice.class));
        verify(gateway, never()).callService(any(UpDevice.class), any(String.class), any(String.class),
                any(String.class), any(String.class), any(Map.class));
        assertEquals(1, dao.list().size());
    }

    // if doesn't knows the device, call 'handshake' on him passing
    // currentDevice information
    // Then register the device on the database

    @Test
    public void IfDontKnowTheDeviceCallAHandshakeWithIt() throws Exception {
        when(gateway.callService((UpDevice) anyObject(), (Call) anyObject()))
                .thenReturn(new Response().addParameter("device", new UpDevice("The Guy")));
        NetworkDevice enteree = networkDevice("ADDR_UNKNOWN", "INEXISTENT");
        deviceManager.deviceEntered(enteree);
        ArgumentCaptor<Call> scCacther = ArgumentCaptor.forClass(Call.class);
        verify(gateway, times(2)).callService(any(UpDevice.class), scCacther.capture());
        Call parameter = scCacther.getAllValues().get(0);
        assertEquals("handshake", parameter.getService());
        assertEquals("uos.DeviceDriver", parameter.getDriver());
        assertNull(parameter.getInstanceId());
        assertNull(parameter.getSecurityType());
        assertEquals(currentDevice, parameter.getParameter("device"));
    }

    @Test
    public void IfTheHandShakeWorksRegisterTheReturnedDevice() throws Exception {
        NetworkDevice enteree = networkDevice("ADDR_UNKNOWN", "INEXISTENT");
        UpDevice newGuy = new UpDevice("TheNewGuy").addNetworkInterface("ADDR_UNKNOWN", "INEXISTENT")
                .addNetworkInterface("127.255.255.666", "Ethernet:TFH");
        when(gatewayHandshakeCall()).thenReturn(new Response().addParameter("device", newGuy));
        deviceManager.deviceEntered(enteree);
        assertEquals(2, dao.list().size());
        assertEquals(newGuy, dao.find(newGuy.getName()));
    }

    @Test
    public void IfTheHandShakeHappensTwiceDoNothing() throws Exception {
        NetworkDevice enteree = networkDevice("ADDR_UNKNOWN", "INEXISTENT");
        UpDevice newGuy = new UpDevice("TheNewGuy").addNetworkInterface("ADDR_UNKNOWN", "INEXISTENT")
                .addNetworkInterface("127.255.255.666", "Ethernet:TFH");
        when(gatewayHandshakeCall()).thenReturn(new Response().addParameter("device", newGuy));
        deviceManager.deviceEntered(enteree);
        deviceManager.deviceEntered(enteree);
        assertEquals(2, dao.list().size());
        assertEquals(newGuy, dao.find(newGuy.getName()));
    }

    @Test
    public void IfTheSecondHandShakeSucceedsRegisterDevice() throws Exception {
        NetworkDevice enteree = networkDevice("ADDR_UNKNOWN", "INEXISTENT");
        UpDevice newGuy = new UpDevice("TheNewGuy").addNetworkInterface("ADDR_UNKNOWN", "INEXISTENT")
                .addNetworkInterface("127.255.255.666", "Ethernet:TFH");
        deviceManager.deviceEntered(enteree);
        when(gatewayHandshakeCall()).thenReturn(new Response().addParameter("device", newGuy));
        deviceManager.deviceEntered(enteree);
        assertEquals(2, dao.list().size());
        assertEquals(newGuy, dao.find(newGuy.getName()));
    }

    @Test
    public void IfTheHandShakeDoesNotWorksNothingHappens_NoResponse() throws Exception {
        NetworkDevice enteree = networkDevice("ADDR_UNKNOWN", "INEXISTENT");
        when(gatewayHandshakeCall()).thenReturn(null);
        deviceManager.deviceEntered(enteree);
        assertEquals(1, dao.list().size());
    }

    @Test
    public void IfTheHandShakeDoesNotWorksNothingHappens_NoData() throws Exception {
        NetworkDevice enteree = networkDevice("ADDR_UNKNOWN", "INEXISTENT");
        when(gatewayHandshakeCall()).thenReturn(new Response());
        deviceManager.deviceEntered(enteree);
        assertEquals(1, dao.list().size());
    }

    @Test
    public void IfTheHandShakeDoesNotWorksNothingHappens_ErrorOnResponse() throws Exception {
        NetworkDevice enteree = networkDevice("ADDR_UNKNOWN", "INEXISTENT");
        Response error = new Response();
        error.setError("Just Kidding");
        when(gatewayHandshakeCall()).thenReturn(error);
        deviceManager.deviceEntered(enteree);
        assertEquals(1, dao.list().size());
    }

    @Test
    public void IfTheHandShakeDoesNotWorksNothingHappens_Exception() throws Exception {
        NetworkDevice enteree = networkDevice("ADDR_UNKNOWN", "INEXISTENT");
        when(gatewayHandshakeCall()).thenThrow(new RuntimeException());
        deviceManager.deviceEntered(enteree);
        assertEquals(1, dao.list().size());
    }

    // if doesn't knows the device, call 'listDrivers'
    // Then register the driver instances on the database

    @Test
    public void IfDontKnowTheDeviceCallListDriversOnIt() throws Exception {
        NetworkDevice enteree = networkDevice("ADDR_UNKNOWN", "INEXISTENT");
        when(gatewayHandshakeCall()).thenReturn(
                new Response().addParameter("device", new UpDevice("A").addNetworkInterface("A", "T")));
        deviceManager.deviceEntered(enteree);
        Call listDrivers = new Call("uos.DeviceDriver", "listDrivers", null);
        verify(gateway, times(1)).callService(any(UpDevice.class), eq(listDrivers));
    }

    @Test
    public void AfterListingTheDriverTheyMustBeRegistered() throws Exception {
        NetworkDevice enteree = networkDevice("ADDR_UNKNOWN", "INEXISTENT");
        when(gatewayHandshakeCall())
                .thenReturn(new Response().addParameter("device", new UpDevice("A").addNetworkInterface("A", "T")));
        Map<String, UpDriver> map = new HashMap<String, UpDriver>();
        UpDriver dummy = new UpDriver("DummyDriver");
        dummy.addService("s1").addParameter("s1p1", ParameterType.MANDATORY).addParameter("s1p2", ParameterType.OPTIONAL);
        dummy.addService("s2").addParameter("s2p1", ParameterType.MANDATORY);

        map.put("id1", dummy);
        map.put("id2", dummy);
        when(gatewayListDriversCall()).thenReturn(new Response().addParameter("driverList", map));
        deviceManager.deviceEntered(enteree);
        List<DriverModel> newGuyDrivers = driverDao.list(null, "A");
        assertEquals(2, newGuyDrivers.size());
        assertEquals("id1", newGuyDrivers.get(0).id());
        assertThat(newGuyDrivers.get(0).driver()).isEqualTo(dummy);
        assertEquals("id2", newGuyDrivers.get(1).id());
        assertThat(newGuyDrivers.get(1).driver()).isEqualTo(dummy);
    }

    @Test
    public void shouldNotRegisterADriverDueToInterfaceValidationError() throws ServiceCallException, IOException {
        NetworkDevice enteree = networkDevice("ADDR_UNKNOWN", "INEXISTENT");
        UpDevice device = new UpDevice("A").addNetworkInterface("A", "T");

        when(gatewayHandshakeCall()).thenReturn(new Response().addParameter("device", device));

        UpDriver dummy = new UpDriver("DummyDriver");
        dummy.addService("s1");
        dummy.addEquivalentDrivers("equivalentDriver");

        Map<String, UpDriver> map = new HashMap<String, UpDriver>();
        map.put("id1", dummy);

        when(gatewayListDriversCall()).thenReturn(new Response().addParameter("driverList", map));

        List<UpDriver> list = new ArrayList<UpDriver>();

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

        list.add(equivalentDriver);

        when(gatewayTellEquivalentDriverCall()).thenReturn(new Response().addParameter("interfaces", list));

        deviceManager.deviceEntered(enteree);

        List<DriverModel> newGuyDrivers = driverDao.list(null, "A");
        assertEquals(0, newGuyDrivers.size());
    }

    @Test
    public void shouldNotRegisterADriverDueToNullEquivalentDriverResponse() throws ServiceCallException {
        NetworkDevice enteree = networkDevice("ADDR_UNKNOWN", "INEXISTENT");
        UpDevice device = new UpDevice("A").addNetworkInterface("A", "T");

        when(gatewayHandshakeCall()).thenReturn(new Response().addParameter("device", device));

        UpDriver dummy = new UpDriver("DummyDriver");
        dummy.addService("s1");
        dummy.addEquivalentDrivers("equivalentDriver");

        Map<String, UpDriver> map = new HashMap<String, UpDriver>();
        map.put("id1", dummy);

        when(gatewayListDriversCall()).thenReturn(new Response().addParameter("driverList", map));

        when(gatewayTellEquivalentDriverCall()).thenReturn(null);

        deviceManager.deviceEntered(enteree);

        List<DriverModel> newGuyDrivers = driverDao.list(null, "A");
        assertEquals(0, newGuyDrivers.size());
    }

    @Test
    public void shouldNotRegisterADriverDueToInterfaceNotProvided() throws ServiceCallException {
        NetworkDevice enteree = networkDevice("ADDR_UNKNOWN", "INEXISTENT");
        UpDevice device = new UpDevice("A").addNetworkInterface("A", "T");

        when(gatewayHandshakeCall()).thenReturn(new Response().addParameter("device", device));

        UpDriver dummy = new UpDriver("DummyDriver");
        dummy.addService("s1");
        dummy.addEquivalentDrivers("equivalentDriver");

        Map<String, UpDriver> map = new HashMap<String, UpDriver>();
        map.put("id1", dummy);

        when(gatewayListDriversCall()).thenReturn(new Response().addParameter("driverList", map));

        when(gatewayTellEquivalentDriverCall()).thenReturn(new Response().addParameter("interfaces", null));

        deviceManager.deviceEntered(enteree);

        List<DriverModel> newGuyDrivers = driverDao.list(null, "A");
        assertEquals(0, newGuyDrivers.size());
    }

    @Test
    public void shouldNotRegisterADriverDueToWrongJSONObject() throws ServiceCallException {
        NetworkDevice enteree = networkDevice("ADDR_UNKNOWN", "INEXISTENT");
        UpDevice device = new UpDevice("A").addNetworkInterface("A", "T");

        when(gatewayHandshakeCall()).thenReturn(new Response().addParameter("device", device));

        UpDriver dummy = new UpDriver("DummyDriver");
        dummy.addService("s1");
        dummy.addEquivalentDrivers(Pointer.DRIVER_NAME);

        when(gatewayListDriversCall())
                .thenReturn(new Response().addParameter("driverList", new String("wrongJSONObject")));

        deviceManager.deviceEntered(enteree);

        List<DriverModel> newGuyDrivers = driverDao.list(null, "A");
        assertEquals(0, newGuyDrivers.size());
    }

    @Test
    public void shouldNotRegisterADriverDueToMissingServer() throws ServiceCallException {
        NetworkDevice enteree = networkDevice("ADDR_UNKNOWN", "INEXISTENT");
        UpDevice device = new UpDevice("A").addNetworkInterface("A", "T");

        when(gatewayHandshakeCall()).thenReturn(new Response().addParameter("device", device));

        Map<String, UpDriver> map = new HashMap<String, UpDriver>();
        UpDriver dummy = new UpDriver("DummyDriver");
        dummy.addService("s1");
        dummy.addEquivalentDrivers(Pointer.DRIVER_NAME);

        map.put("id1", dummy);

        when(gatewayListDriversCall()).thenReturn(new Response().addParameter("driverList", map));

        deviceManager.deviceEntered(enteree);

        List<DriverModel> newGuyDrivers = driverDao.list(null, "A");
        assertEquals(0, newGuyDrivers.size());
    }

    @Test
    public void shouldNotRegisterADriverDueToEquivalentDriverInterfaceValidationError() throws ServiceCallException {
        NetworkDevice enteree = networkDevice("ADDR_UNKNOWN", "INEXISTENT");
        UpDevice device = new UpDevice("A").addNetworkInterface("A", "T");

        when(gatewayHandshakeCall()).thenReturn(new Response().addParameter("device", device));

        Map<String, UpDriver> map = new HashMap<String, UpDriver>();
        UpDriver dummy = new UpDriver("DummyDriver");
        dummy.addService("s1");
        dummy.addEquivalentDrivers("equivalentDriver");

        map.put("id1", dummy);

        when(gatewayListDriversCall()).thenReturn(new Response().addParameter("driverList", map));

        List<UpDriver> list = new ArrayList<UpDriver>();

        UpDriver equivalentDriver = new UpDriver("equivalentDriver");
        equivalentDriver.addEquivalentDrivers(Pointer.DRIVER_NAME);
        equivalentDriver.addService("s1");

        list.add(equivalentDriver);

        when(gatewayTellEquivalentDriverCall())
                .thenReturn(new Response().addParameter("interfaces", list));

        deviceManager.deviceEntered(enteree);

        List<DriverModel> newGuyDrivers = driverDao.list(null, "A");
        assertEquals(0, newGuyDrivers.size());
    }

    @Test
    public void shouldNotRegisterADriverWithEquivalentDriverNotInformed() throws ServiceCallException {
        NetworkDevice enteree = networkDevice("ADDR_UNKNOWN", "INEXISTENT");
        UpDevice device = new UpDevice("A").addNetworkInterface("A", "T");

        when(gatewayHandshakeCall()).thenReturn(new Response().addParameter("device", device));

        Map<String, UpDriver> map = new HashMap<String, UpDriver>();
        UpDriver dummy = new UpDriver("DummyDriver");
        dummy.addService("s1");
        dummy.addEquivalentDrivers("equivalentDriver");

        map.put("id1", dummy);

        when(gatewayListDriversCall()).thenReturn(new Response().addParameter("driverList", map));

        List<UpDriver> list = new ArrayList<UpDriver>();

        UpDriver equivalentDriver = new UpDriver("equivalentDriver");
        equivalentDriver.addService("s1");
        equivalentDriver.addEquivalentDrivers("notInformedEquivalentDriver");

        list.add(equivalentDriver);

        when(gatewayTellEquivalentDriverCall())
                .thenReturn(new Response().addParameter("interfaces", list));

        deviceManager.deviceEntered(enteree);

        List<DriverModel> newGuyDrivers = driverDao.list(null, "A");
        assertEquals(0, newGuyDrivers.size());
    }

    public void shouldNotRegisterADriverWithTwoEquivalentDriversNotInformed() throws ServiceCallException {
        NetworkDevice enteree = networkDevice("ADDR_UNKNOWN", "INEXISTENT");
        UpDevice device = new UpDevice("A").addNetworkInterface("A", "T");

        when(gatewayHandshakeCall()).thenReturn(new Response().addParameter("device", device));

        Map<String, UpDriver> map = new HashMap<String, UpDriver>();
        UpDriver dummy = new UpDriver("DummyDriver");
        dummy.addService("s1");
        dummy.addEquivalentDrivers("equivalentDriver1");
        dummy.addEquivalentDrivers("equivalentDriver2");

        map.put("id1", dummy);

        when(gatewayListDriversCall()).thenReturn(new Response().addParameter("driverList", map));

        List<UpDriver> list = new ArrayList<UpDriver>();

        UpDriver equivalentDriver1 = new UpDriver("equivalentDriver1");
        equivalentDriver1.addService("s1");
        equivalentDriver1.addEquivalentDrivers("notInformedEquivalentDriver1");
        UpDriver equivalentDriver2 = new UpDriver("equivalentDriver2");
        equivalentDriver2.addEquivalentDrivers("notInformedEquivalentDriver2");
        equivalentDriver2.addService("s1");

        list.add(equivalentDriver1);
        list.add(equivalentDriver2);

        when(gatewayTellEquivalentDriverCall())
                .thenReturn(new Response().addParameter("interfaces", list));

        deviceManager.deviceEntered(enteree);

        List<DriverModel> newGuyDrivers = driverDao.list(null, "A");
        assertEquals(0, newGuyDrivers.size());
    }

    @Test
    public void shouldRegisterADriverWithUnknownEquivalentDriver() throws ServiceCallException {
        NetworkDevice enteree = networkDevice("ADDR_UNKNOWN", "INEXISTENT");
        UpDevice device = new UpDevice("A").addNetworkInterface("A", "T");

        when(gatewayHandshakeCall()).thenReturn(new Response().addParameter("device", device));

        Map<String, UpDriver> map = new HashMap<String, UpDriver>();
        UpDriver dummy = new UpDriver("DummyDriver");
        dummy.addService("s1");
        dummy.addEquivalentDrivers("equivalentDriver");
        map.put("id1", dummy);

        when(gatewayListDriversCall()).thenReturn(new Response().addParameter("driverList", map));

        List<UpDriver> list = new ArrayList<UpDriver>();
        UpDriver equivalentDriver = new UpDriver("equivalentDriver");
        equivalentDriver.addService("s1");
        list.add(equivalentDriver);

        when(gatewayTellEquivalentDriverCall()).thenReturn(new Response().addParameter("interfaces", list));

        deviceManager.deviceEntered(enteree);

        List<DriverModel> newGuyDrivers = driverDao.list(null, "A");
        assertEquals(1, newGuyDrivers.size());
        assertEquals("id1", newGuyDrivers.get(0).id());
        assertThat(newGuyDrivers.get(0).driver()).isEqualTo(dummy);
    }

    @Test
    public void shouldRegisterTwoDriversWithTheSameUnknownEquivalentDriver() throws ServiceCallException {
        NetworkDevice enteree = networkDevice("ADDR_UNKNOWN", "INEXISTENT");
        UpDevice device = new UpDevice("A").addNetworkInterface("A", "T");

        when(gatewayHandshakeCall()).thenReturn(new Response().addParameter("device", device));

        Map<String, UpDriver> map = new HashMap<String, UpDriver>();

        UpDriver dummy1 = new UpDriver("DummyDriver1");
        dummy1.addService("s1");
        dummy1.addEquivalentDrivers("equivalentDriver");

        UpDriver dummy2 = new UpDriver("DummyDriver2");
        dummy2.addService("s1");
        dummy2.addEquivalentDrivers("equivalentDriver");

        map.put("iddummy1", dummy1);
        map.put("iddummy2", dummy2);

        when(gatewayListDriversCall()).thenReturn(new Response().addParameter("driverList", map));

        List<UpDriver> list = new ArrayList<UpDriver>();

        UpDriver equivalentDriver = new UpDriver("equivalentDriver");
        equivalentDriver.addService("s1");

        list.add(equivalentDriver);

        when(gatewayTellEquivalentDriverCall())
                .thenReturn(new Response().addParameter("interfaces", list));

        deviceManager.deviceEntered(enteree);

        List<DriverModel> newGuyDrivers = driverDao.list(null, "A");

        assertEquals(2, newGuyDrivers.size());
        assertEquals("iddummy1", newGuyDrivers.get(0).id());
        assertThat(newGuyDrivers.get(0).driver()).isEqualTo(dummy1);
        assertEquals("iddummy2", newGuyDrivers.get(1).id());
        assertThat(newGuyDrivers.get(1).driver()).isEqualTo(dummy2);
    }

    @Test
    public void shouldRegisterADriverWithTwoLevelsOfUnknownEquivalentDrivers() throws ServiceCallException {
        NetworkDevice enteree = networkDevice("ADDR_UNKNOWN", "INEXISTENT");
        UpDevice device = new UpDevice("A").addNetworkInterface("A", "T");

        when(gatewayHandshakeCall()).thenReturn(new Response().addParameter("device", device));

        Map<String, UpDriver> map = new HashMap<String, UpDriver>();
        UpDriver dummy = new UpDriver("DummyDriver");
        dummy.addService("s1");
        dummy.addEquivalentDrivers("equivalentDriver");

        map.put("id1", dummy);

        when(gatewayListDriversCall()).thenReturn(new Response().addParameter("driverList", map));

        UpDriver equivalentDriver = new UpDriver("equivalentDriver");
        equivalentDriver.addService("s1");
        equivalentDriver.addEquivalentDrivers("equivalentToTheEquivalentDriver");

        UpDriver equivalentToTheEquivalentDriver = new UpDriver("equivalentToTheEquivalentDriver");
        equivalentToTheEquivalentDriver.addService("s1");

        List<UpDriver> equivalentDrivers = new ArrayList<UpDriver>();
        equivalentDrivers.add(equivalentDriver);
        equivalentDrivers.add(equivalentToTheEquivalentDriver);

        when(gatewayTellEquivalentDriverCall())
                .thenReturn(new Response().addParameter("interfaces", equivalentDrivers));

        deviceManager.deviceEntered(enteree);
        List<DriverModel> newGuyDrivers = driverDao.list(null, "A");

        // verify(gatewayTellEquivalentDriverCall(), times(2));
        assertEquals(1, newGuyDrivers.size());
        assertEquals("id1", newGuyDrivers.get(0).id());
        assertThat(newGuyDrivers.get(0).driver()).isEqualTo(dummy);
    }

    @Test
    public void shouldRegisterTwoDriversWithTheSameUnknownEquivalentDriverFromDifferentLevels() throws ServiceCallException {
        NetworkDevice enteree = networkDevice("ADDR_UNKNOWN", "INEXISTENT");
        UpDevice device = new UpDevice("A").addNetworkInterface("A", "T");

        when(gatewayHandshakeCall()).thenReturn(new Response().addParameter("device", device));

        Map<String, UpDriver> map = new HashMap<String, UpDriver>();

        UpDriver dummy1 = new UpDriver("D1");
        dummy1.addService("s1");
        dummy1.addEquivalentDrivers("D0");

        UpDriver dummy2 = new UpDriver("D4");
        dummy2.addService("s1");
        dummy2.addEquivalentDrivers("D3");

        map.put("iddummy1", dummy1);
        map.put("iddummy2", dummy2);

        when(gatewayListDriversCall()).thenReturn(new Response().addParameter("driverList", map));

        List<UpDriver> list = new ArrayList<UpDriver>();

        UpDriver d0 = new UpDriver("D0");
        d0.addService("s1");

        UpDriver d2 = new UpDriver("D2");
        d2.addService("s1");
        d2.addEquivalentDrivers(d0.getName());

        UpDriver d3 = new UpDriver("D3");
        d3.addService("s1");
        d3.addEquivalentDrivers(d2.getName());

        list.add(d3);
        list.add(d0);
        list.add(d2);

        when(gatewayTellTwoEquivalentDrivers()).thenReturn(new Response().addParameter("interfaces", list));

        deviceManager.deviceEntered(enteree);

        List<DriverModel> newGuyDrivers = driverDao.list(null, "A");

        // TODO: This
        assertEquals(2, newGuyDrivers.size());
        if (newGuyDrivers.get(0).id().equals("iddummy1")) {
            assertEquals("iddummy1", newGuyDrivers.get(0).id());
            assertThat(newGuyDrivers.get(0).driver()).isEqualTo(dummy1);
            assertEquals("iddummy2", newGuyDrivers.get(1).id());
            assertThat(newGuyDrivers.get(1).driver()).isEqualTo(dummy2);
        } else {
            assertEquals("iddummy2", newGuyDrivers.get(0).id());
            assertThat(newGuyDrivers.get(0).driver()).isEqualTo(dummy2);
            assertEquals("iddummy1", newGuyDrivers.get(1).id());
            assertThat(newGuyDrivers.get(1).driver()).isEqualTo(dummy1);
        }
    }

    @Test
    public void IfTheListDriversDoesNotWorksNothingHappens_NoResponse() throws Exception {
        NetworkDevice enteree = networkDevice("ADDR_UNKNOWN", "INEXISTENT");
        when(gatewayHandshakeCall()).thenReturn(
                new Response().addParameter("device", new UpDevice("A").addNetworkInterface("A", "T")));
        when(gatewayListDriversCall()).thenReturn(null);
        deviceManager.deviceEntered(enteree);
        assertEquals(0, driverDao.list(null, "A").size());
    }

    @Test
    public void IfTheListDriversDoesNotWorksNothingHappens_NoData() throws Exception {
        NetworkDevice enteree = networkDevice("ADDR_UNKNOWN", "INEXISTENT");
        when(gatewayHandshakeCall()).thenReturn(
                new Response().addParameter("device", new UpDevice("A").addNetworkInterface("A", "T")));
        when(gatewayListDriversCall()).thenReturn(new Response());
        deviceManager.deviceEntered(enteree);
        assertEquals(0, driverDao.list(null, "A").size());
    }

    // FIXME: [B&M] Sempre passa! Como testar caso de erro?!
    @Test
    public void shouldNotHandshakeWithItsSelf() throws NetworkException, ServiceCallException {
        NetworkDevice enteree = mock(NetworkDevice.class);

        when(enteree.getNetworkDeviceName()).thenReturn("127.0.0.1:8080");
        when(enteree.getNetworkDeviceType()).thenReturn("Ethernet:TCP");
        when(connManager.getHost("127.0.0.1:8080")).thenReturn("127.0.0.1");
        when(connManager.getHost("127.0.0.1:80")).thenReturn("127.0.0.1");
        deviceManager.deviceEntered(enteree);
        verify(gateway, never()).callService((UpDevice) any(), (Call) any());
    }

    @Test
    public void IfTheListDriversDoesNotWorksNothingHappens_ThrowingException() throws Exception {
        NetworkDevice enteree = networkDevice("ADDR_UNKNOWN", "INEXISTENT");
        when(gatewayHandshakeCall()).thenReturn(
                new Response().addParameter("device", new UpDevice("A").addNetworkInterface("A", "T")));
        when(gatewayListDriversCall()).thenThrow(new RuntimeException());
        deviceManager.deviceEntered(enteree);
        assertEquals(0, driverDao.list(null, "A").size());
    }

    private NetworkDevice networkDevice(String address, String type) {
        NetworkDevice enteree = mock(NetworkDevice.class);
        when(enteree.getNetworkDeviceName()).thenReturn(address);
        when(enteree.getNetworkDeviceType()).thenReturn(type);
        return enteree;
    }

    private Response gatewayHandshakeCall() throws ServiceCallException {
        Call handshake = new Call("uos.DeviceDriver", "handshake", null);
        handshake.addParameter("device", currentDevice);
        return gateway.callService(any(UpDevice.class), eq(handshake));
    }

    private Response gatewayListDriversCall() throws ServiceCallException {
        Call listDrivers = new Call("uos.DeviceDriver", "listDrivers", null);
        return gateway.callService(any(UpDevice.class), eq(listDrivers));
    }

    private Response gatewayTellEquivalentDriverCall() throws ServiceCallException {
        Call tellEquivalentDriver = new Call("uos.DeviceDriver", "tellEquivalentDrivers", null);
        List<String> equivalents = new ArrayList<String>();
        equivalents.add("equivalentDriver");
        tellEquivalentDriver.addParameter("driversName", equivalents);
        return gateway.callService(any(UpDevice.class), eq(tellEquivalentDriver));
    }

    private Response gatewayTellTwoEquivalentDrivers() throws ServiceCallException {
        Call tellEquivalentDriver = new Call("uos.DeviceDriver", "tellEquivalentDrivers", null);
        List<String> equivalents = new ArrayList<String>();
        equivalents.add("D0");
        equivalents.add("D3");
        tellEquivalentDriver.addParameter("driversName", equivalents);
        return gateway.callService(any(UpDevice.class), eq(tellEquivalentDriver));
    }

    // TODO: What about proxying
    @Test
    public void AfterListingIfProxyingIsEnabledRegisterIt() throws Exception {
        NetworkDevice enteree = networkDevice("ADDR_UNKNOWN", "INEXISTENT");
        when(gatewayHandshakeCall()).thenReturn(
                new Response()
                        .addParameter("device", new UpDevice("A").addNetworkInterface("A", "T"))
        );

        Map<String, UpDriver> map = new HashMap<String, UpDriver>();
        UpDriver dummy = new UpDriver("DummyDriver");
        dummy.addService("s1")
                .addParameter("s1p1", ParameterType.MANDATORY)
                .addParameter("s1p2", ParameterType.OPTIONAL);
        dummy.addService("s2").addParameter("s2p1", ParameterType.MANDATORY);

        map.put("id1", dummy);
        map.put("id2", dummy);
        when(gatewayListDriversCall()).thenReturn(new Response().addParameter("driverList", map));
        when(proxier.doProxying()).thenReturn(true);
        deviceManager.deviceEntered(enteree);
        ArgumentCaptor<UpDevice> deviceCatcher = ArgumentCaptor.forClass(UpDevice.class);
        ArgumentCaptor<String> idCatcher = ArgumentCaptor.forClass(String.class);
        verify(proxier, times(2)).registerProxyDriver(eq(dummy), deviceCatcher.capture(), idCatcher.capture());
        assertEquals("A", deviceCatcher.getValue().getName());
        assertThat(new HashSet<String>(idCatcher.getAllValues())).isEqualTo(new HashSet<String>() {
            {
                add("id1");
                add("id2");
            }
        });
    }

    @Test
    public void AfterListingIfProxyingIsNotEnabledDoNothing() throws Exception {
        NetworkDevice enteree = networkDevice("ADDR_UNKNOWN", "INEXISTENT");
        when(gatewayHandshakeCall()).thenReturn(
                new Response().addParameter("device", new UpDevice("A").addNetworkInterface("A", "T")));
        Map<String, UpDriver> map = new HashMap<String, UpDriver>();
        UpDriver dummy = new UpDriver("DummyDriver");
        dummy.addService("s1").addParameter("s1p1", ParameterType.MANDATORY).addParameter("s1p2",
                ParameterType.OPTIONAL);
        dummy.addService("s2").addParameter("s2p1", ParameterType.MANDATORY);

        map.put("id1", dummy);
        map.put("id2", dummy);
        when(gatewayListDriversCall()).thenReturn(new Response().addParameter("driverList", map));
        when(proxier.doProxying()).thenReturn(false);
        deviceManager.deviceEntered(enteree);
        verify(proxier, never()).registerProxyDriver(any(UpDriver.class), any(UpDevice.class), any(String.class));
    }

    // public void deviceLeft(NetworkDevice device) {
    @Test
    public void removeTheDeviceFromDatabaseOnLeft() throws Exception {
        NetworkDevice leavingCard = networkDevice("ADDR_UNKNOWN_A", "INEXISTENT");
        UpDevice leavingGuy = upDevice("leavingMan", leavingCard);
        when(connManager.getHost(eq(leavingCard.getNetworkDeviceName()))).thenReturn("ADDR_UNKNOWN_A");
        deviceManager.registerDevice(leavingGuy);

        NetworkDevice stayingCard = networkDevice("ADDR_UNKNOWN_B", "INEXISTENT");
        UpDevice stayingGuy = upDevice("stayingMan", stayingCard);
        when(connManager.getHost(eq(stayingCard.getNetworkDeviceName()))).thenReturn("ADDR_UNKNOWN_B");
        deviceManager.registerDevice(stayingGuy);

        assertEquals(3, dao.list().size());
        UpDriver driver = new UpDriver("DD");
        driver.addService("s");
        driverDao.insert(new DriverModel("id1", driver, leavingGuy.getName()));
        driverDao.insert(new DriverModel("id2", driver, leavingGuy.getName()));
        driverDao.insert(new DriverModel("id3", driver, leavingGuy.getName()));
        driverDao.insert(new DriverModel("id4", driver, stayingGuy.getName()));
        assertEquals(4, driverDao.list().size());

        deviceManager.deviceLeft(leavingCard);
        assertEquals(2, dao.list().size());
        assertEquals(1, driverDao.list().size());
    }

    private UpDevice upDevice(String name, NetworkDevice networkCard) {
        return new UpDevice(name).addNetworkInterface(networkCard.getNetworkDeviceName(),
                networkCard.getNetworkDeviceType());
    }

    @Test
    public void doNothingWhenLeftingANullDevice() throws Exception {
        NetworkDevice leavingGuy = networkDevice("ADDR_UNKNOWN", "INEXISTENT");
        UpDevice oldGuy = upDevice("OldMan", leavingGuy);
        when(connManager.getHost(eq(leavingGuy.getNetworkDeviceName()))).thenReturn("ADDR_UNKNOWN");
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
        NetworkDevice leavingGuy = networkDevice("ADDR_UNKNOWN", "INEXISTENT");
        UpDevice oldGuy = upDevice("OldMan", leavingGuy);
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
        NetworkDevice leavingGuy = networkDevice("ADDR_UNKNOWN", "INEXISTENT");
        assertEquals(1, dao.list().size());
        UpDriver driver = new UpDriver("DD");
        driver.addService("s");
        driverDao.insert(new DriverModel("id1", driver, currentDevice.getName()));
        driverDao.insert(new DriverModel("id2", driver, currentDevice.getName()));
        assertEquals(2, driverDao.list().size());

        deviceManager.deviceLeft(leavingGuy);
        assertEquals(1, dao.list().size());
        assertEquals(2, driverDao.list().size());
    }

    @Test
    public void doNothingWhenLeftingAnUnkownDevicexxxx() throws Exception {
        NetworkDevice knownCard = networkDevice("ADDR_KNOWN", "THIS_ONE");
        UpDevice knownGuy = upDevice("OldMan", knownCard);
        deviceManager.registerDevice(knownGuy);

        NetworkDevice leavingGuy = networkDevice("ADDR_UNKNOWN", "THIS_ONE");
        assertEquals(2, dao.list().size());
        UpDriver driver = new UpDriver("DD");
        driver.addService("s");
        driverDao.insert(new DriverModel("id1", driver, currentDevice.getName()));
        driverDao.insert(new DriverModel("id2", driver, currentDevice.getName()));
        assertEquals(2, driverDao.list().size());

        deviceManager.deviceLeft(leavingGuy);
        assertEquals(2, dao.list().size());
        assertEquals(2, driverDao.list().size());
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowIfAddingNullDeviceListener() throws Exception {
        deviceManager.addDeviceListener(null);
    }

    @Test
    public void shouldNotThrowIfRemovingInvalidDeviceListener() throws Exception {
        deviceManager.removeDeviceListener(null);
        deviceManager.removeDeviceListener(mock(DeviceListener.class));
    }

    @Test
    public void shouldCallDeviceEnteredOnListenersIfRegisteredSuccessfully() throws Exception {
        DeviceListener listener = mock(DeviceListener.class);
        deviceManager.addDeviceListener(listener);
        NetworkDevice enteree = networkDevice("ADDR_UNKNOWN", "INEXISTENT");
        UpDevice newGuy = new UpDevice("TheNewGuy").addNetworkInterface("ADDR_UNKNOWN", "INEXISTENT")
                .addNetworkInterface("127.255.255.666", "Ethernet:TFH");
        when(gatewayHandshakeCall()).thenReturn(new Response().addParameter("device", newGuy));
        deviceManager.deviceEntered(enteree);
        verify(listener, times(1)).deviceRegistered(newGuy);
    }

    @Test
    public void shouldCallDeviceLeftOnListenersIfKnownDeviceLeft() throws Exception {
        DeviceListener listener = mock(DeviceListener.class);
        deviceManager.addDeviceListener(listener);
        NetworkDevice enteree = networkDevice("ADDR_UNKNOWN", "INEXISTENT");
        UpDevice newGuy = new UpDevice("TheNewGuy").addNetworkInterface("ADDR_UNKNOWN", "INEXISTENT")
                .addNetworkInterface("127.255.255.666", "Ethernet:TFH");
        when(gatewayHandshakeCall()).thenReturn(new Response().addParameter("device", newGuy));
        deviceManager.deviceEntered(enteree);
        verify(listener, times(1)).deviceRegistered(newGuy);
        deviceManager.deviceLeft(enteree);
        verify(listener, times(1)).deviceUnregistered(newGuy);
        verify(listener, times(1)).deviceRegistered(newGuy);
    }
}
