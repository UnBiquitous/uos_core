package org.unbiquitous.uos.core.driverManager;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;

import org.fest.assertions.core.Condition;
import org.junit.Before;
import org.junit.Test;
import org.unbiquitous.uos.core.adaptabitilyEngine.Gateway;
import org.unbiquitous.uos.core.applicationManager.CallContext;
import org.unbiquitous.uos.core.deviceManager.DeviceDao;
import org.unbiquitous.uos.core.driver.DeviceDriver;
import org.unbiquitous.uos.core.messageEngine.dataType.UpDevice;
import org.unbiquitous.uos.core.messageEngine.dataType.UpDriver;
import org.unbiquitous.uos.core.messageEngine.dataType.UpService;
import org.unbiquitous.uos.core.messageEngine.dataType.UpService.ParameterType;
import org.unbiquitous.uos.core.messageEngine.messages.Call;
import org.unbiquitous.uos.core.messageEngine.messages.Response;


public class DriverManagerTest {
	
	private DriverManager manager;
	private DriverSpy driver;
	private UpDevice currentDevice;
	private DriverDao dao;
	private DeviceDao deviceDao;
	
	@Before
	public void setUp(){
		dao = new DriverDao(null);
		deviceDao = new DeviceDao(null);
		currentDevice = new UpDevice("Optimus");
		deviceDao.save(currentDevice);
		manager = new DriverManager(currentDevice,dao,deviceDao,new ReflectionServiceCaller(null));
		driver = new DriverSpy();
	}
	
	
	@Test
	public void shouldCallServiceOnDriverUsingInstanceId() throws DriverManagerException, InterfaceValidationException, DriverNotFoundException {
		manager.deployDriver(driver.upDriver, driver,"id");
		manager.handleServiceCall(new Call("driver","service","id"), null);
		
		assertTrue(driver.called);
	}
	
	@Test
	public void shouldDeployDriverWithEquivalentDriversAndCallServiceOnDriverUsingInstanceId() throws DriverManagerException, InterfaceValidationException, DriverNotFoundException {
		driver.upDriver.addEquivalentDrivers("equivalentDriver");
		UpDriver equivalentDriver = new UpDriver("equivalentDriver");
		equivalentDriver.addService("s").addParameter("p", ParameterType.MANDATORY);
		driver.getParent().add(equivalentDriver);
		
		manager.deployDriver(driver.upDriver, driver,"id");
		manager.handleServiceCall(new Call("driver","service","id"), null);
		
		assertTrue(driver.called);
	}
	
	@Test(expected=DriverManagerException.class)
	public void shouldFailCallingServiceOnDriverUsingAnInvalidInstanceId() throws DriverManagerException, InterfaceValidationException, DriverNotFoundException{
		manager.handleServiceCall(new Call("driver","service","id"), null);
	}
	
	@Test(expected=DriverManagerException.class)
	public void shouldFailCallingServiceOnDriverFromOtherDevice() throws DriverManagerException, InterfaceValidationException, DriverNotFoundException{
		dao.insert(new DriverModel("id", new UpDriver("driver"), "otherDevice"));
		manager.handleServiceCall(new Call("driver","service","id"), null);
	}
	
	@Test
	public void shouldNotFailCallingServiceHomonymousDriverFromOtherDevice() throws DriverManagerException, InterfaceValidationException, DriverNotFoundException{
		dao.insert(new DriverModel("id", new UpDriver("driver"), "otherDevice"));
		manager.deployDriver(driver.upDriver, driver,"id");
		manager.handleServiceCall(new Call("driver","service","id"), null);
		assertTrue(driver.called);
	}
	
	@Test
	public void shouldCallServiceOnDriverUsingDriver() throws DriverManagerException, InterfaceValidationException, DriverNotFoundException{
		manager.deployDriver(driver.upDriver, driver);
		manager.handleServiceCall(new Call("driver","service"), null);
		
		assertTrue(driver.called);
	}
	
	@Test(expected=DriverManagerException.class)
	public void shouldFailCallingServiceOnDriverUsingAnInvalidDriver() throws DriverManagerException, InterfaceValidationException, DriverNotFoundException{
		manager.handleServiceCall(new Call("driver","service"), null);
	}
	
	@Test
	public void shouldCallServiceFromFirstLevelEquivalentDriver() throws DriverManagerException, InterfaceValidationException, DriverNotFoundException{
		manager.insert(new DriverModel("id1",driver.upDriver,"my.Phone"));
		DriverSpy equivalentDriver = new DriverSpy("Bumblebee");
		equivalentDriver.upDriver.addEquivalentDrivers(driver.upDriver.getName());
		manager.deployDriver(equivalentDriver.upDriver, equivalentDriver);
		manager.handleServiceCall(new Call("driver","service"), null);
		assertTrue(equivalentDriver.called);
	}
	
	@Test
	public void shouldCallServiceFromSecondLevelEquivalentDriver() throws DriverManagerException, InterfaceValidationException, DriverNotFoundException{
		manager.insert(new DriverModel("id1",driver.upDriver,"my.Phone"));
		
		DriverSpy firstLevelEquivalentDriver = new DriverSpy("Ironhide");
		firstLevelEquivalentDriver.upDriver.addEquivalentDrivers(driver.upDriver.getName());
		manager.insert(new DriverModel("id2",firstLevelEquivalentDriver.upDriver,"my.SecondPhone"));
		
		DriverSpy secondLevelEquivalentDriver = new DriverSpy("Bumblebee");
		secondLevelEquivalentDriver.upDriver.addEquivalentDrivers(firstLevelEquivalentDriver.upDriver.getName());
		
		manager.deployDriver(secondLevelEquivalentDriver.upDriver, secondLevelEquivalentDriver);
		manager.handleServiceCall(new Call("driver","service"), null);
		assertTrue(secondLevelEquivalentDriver.called);
	}
	
	@Test(expected=DriverManagerException.class)
	public void shouldFailOnTryingToFindAnEquivalentDriverToHandleServiceCall() throws DriverManagerException, DriverNotFoundException, InterfaceValidationException {
		manager.insert(new DriverModel("id1",driver.upDriver,"my.Phone"));
		
		DriverSpy firstLevelEquivalentDriver = new DriverSpy("Ironhide");
		firstLevelEquivalentDriver.upDriver.addEquivalentDrivers(driver.upDriver.getName());
		manager.insert(new DriverModel("id2",firstLevelEquivalentDriver.upDriver,"my.SecondPhone"));
		manager.handleServiceCall(new Call("driver","service"), null);
	}
	
	@Test
	public void shouldDeployOnTheInformedDevice() throws DriverManagerException, InterfaceValidationException, DriverNotFoundException{
		manager.deployDriver(driver.upDriver, driver);
		List<DriverModel> list = dao.list(null, currentDevice.getName());
		assertNotNull(list);
		assertFalse(list.isEmpty());
	}
	
	@Test
	public void shouldListADeployedDriver() throws DriverManagerException, InterfaceValidationException, DriverNotFoundException{
		assertNull(manager.listDrivers());
		manager.deployDriver(driver.upDriver, driver);
		assertEquals(1,manager.listDrivers().size());
		assertEquals(driver,manager.listDrivers().get(0));
	}
	
	@Test
	public void shouldListAllDeployedDrivers() throws DriverManagerException, InterfaceValidationException, DriverNotFoundException{
		assertNull(manager.listDrivers());
		manager.deployDriver(driver.upDriver, driver);
		manager.deployDriver(driver.upDriver, new DriverSpy());
		manager.deployDriver(driver.upDriver, new DriverSpy());
		assertEquals(3,manager.listDrivers().size());
	}
	
	@Test(expected=DriverManagerException.class)
	public void shouldFailOnDeployingNotEquivalentDriverWithoutOneService() throws DriverManagerException, DriverNotFoundException, InterfaceValidationException {
		
		driver.upDriver.addService("s2");
		manager.insert(new DriverModel("id1",driver.upDriver,"my.Phone"));
		
		DriverSpy wrongEquivalentDriver = new DriverSpy("wrongEquivalent");
		wrongEquivalentDriver.upDriver.addEquivalentDrivers(driver.upDriver.getName());
		
		manager.deployDriver(wrongEquivalentDriver.upDriver, wrongEquivalentDriver);
	}
	
	@Test(expected=DriverManagerException.class)
	public void shouldFailOnDeployingNotEquivalentDriverWithDifferentNamesOfServices() throws DriverManagerException, DriverNotFoundException, InterfaceValidationException {
		
		driver.upDriver.addService("s2");
		manager.insert(new DriverModel("id1",driver.upDriver,"my.Phone"));
		
		DriverSpy wrongEquivalentDriver = new DriverSpy("wrongEquivalent");
		wrongEquivalentDriver.upDriver.addService("s3");
		wrongEquivalentDriver.upDriver.addEquivalentDrivers(driver.upDriver.getName());
		
		manager.deployDriver(wrongEquivalentDriver.upDriver, wrongEquivalentDriver);
	}
	
	@Test(expected=DriverManagerException.class)
	public void shouldFailOnDeployingNotEquivalentDriverWithDifferentNumberOfParameters() throws DriverManagerException, DriverNotFoundException, InterfaceValidationException {
		UpService service = new UpService("s2");
		service.addParameter("p1", ParameterType.MANDATORY);
		
		driver.upDriver.addService(service);
		manager.insert(new DriverModel("id1",driver.upDriver,"my.Phone"));
		
		UpService service2 = new UpService("s2");
		service2.addParameter("p1", ParameterType.MANDATORY);
		service2.addParameter("p2", ParameterType.MANDATORY);
		
		DriverSpy wrongEquivalentDriver = new DriverSpy("wrongEquivalent");
		wrongEquivalentDriver.upDriver.addService(service2);
		wrongEquivalentDriver.upDriver.addEquivalentDrivers(driver.upDriver.getName());
		
		manager.deployDriver(wrongEquivalentDriver.upDriver, wrongEquivalentDriver);
	}
	
	@Test(expected=DriverManagerException.class)
	public void shouldFailOnDeployingNotEquivalentDriverWithDifferentNamesOfParameters() throws DriverManagerException, DriverNotFoundException, InterfaceValidationException {
		UpService service = new UpService("s2");
		service.addParameter("p1", ParameterType.MANDATORY);
		
		driver.upDriver.addService(service);
		manager.insert(new DriverModel("id1",driver.upDriver,"my.Phone"));
		
		UpService service2 = new UpService("s2");
		service2.addParameter("p2", ParameterType.MANDATORY);
		
		DriverSpy wrongEquivalentDriver = new DriverSpy("wrongEquivalent");
		wrongEquivalentDriver.upDriver.addService(service2);
		wrongEquivalentDriver.upDriver.addEquivalentDrivers(driver.upDriver.getName());
		
		manager.deployDriver(wrongEquivalentDriver.upDriver, wrongEquivalentDriver);
	}
	
	@Test(expected=DriverManagerException.class)
	public void shouldFailOnDeployingNotEquivalentDriverWithDifferentTypesOfParameters() throws DriverManagerException, DriverNotFoundException, InterfaceValidationException {
		UpService service = new UpService("s2");
		service.addParameter("p1", ParameterType.MANDATORY);
		
		driver.upDriver.addService(service);
		manager.insert(new DriverModel("id1",driver.upDriver,"my.Phone"));
		
		UpService service2 = new UpService("s2");
		service2.addParameter("p1", ParameterType.OPTIONAL);

		DriverSpy wrongEquivalentDriver = new DriverSpy("wrongEquivalent");
		wrongEquivalentDriver.upDriver.addService(service2);
		wrongEquivalentDriver.upDriver.addEquivalentDrivers(driver.upDriver.getName());
		
		manager.deployDriver(wrongEquivalentDriver.upDriver, wrongEquivalentDriver);
	}
	
	@Test
	public void shouldAssignAnIdWhenNotInformed() throws DriverManagerException, InterfaceValidationException, DriverNotFoundException{
		assertNull(manager.listDrivers());
		manager.deployDriver(driver.upDriver, driver);
		manager.initDrivers(null);
		assertNotNull(driver.id);
	}
	
	@Test
	public void shouldKeepIdWhenInformed() throws DriverManagerException, InterfaceValidationException, DriverNotFoundException{
		assertNull(manager.listDrivers());
		manager.deployDriver(driver.upDriver, driver, "id");
		manager.initDrivers(null);
		assertEquals("id",driver.id);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void shouldNotAcceptAnDriverThatIsNotAUosDriver() throws DriverManagerException, InterfaceValidationException, DriverNotFoundException{
		assertNull(manager.listDrivers());
		manager.deployDriver(driver.upDriver, new Object(),"myId");
	}
	
	@Test
	public void shouldRemoveUndeployedDriver() throws Exception{
		assertNull(manager.listDrivers());
		manager.deployDriver(driver.upDriver, driver, "id");
		assertNotNull(manager.listDrivers());
		manager.undeployDriver("id");
		assertNull(manager.listDrivers());
	}
	
	@Test
	public void shouldRemoveUndeployedDriverEvenWithAnHomonymousOnAnotherDevice() throws Exception{
		assertNull(manager.listDrivers());
		dao.insert(new DriverModel("id", driver.getDriver(), "otherDevice"));
		manager.deployDriver(driver.upDriver, driver, "id");
		assertNotNull(manager.listDrivers());
		manager.undeployDriver("id");
		assertNull(manager.listDrivers());
	}
	
	@Test
	public void shouldDoNothingUndeploingAnNonExistingDriver() throws Exception{
		assertNull(manager.listDrivers());
		manager.deployDriver(driver.upDriver, driver, "id");
		assertNotNull(manager.listDrivers());
		manager.undeployDriver("notMyId");
		assertNotNull(manager.listDrivers());
	}
	
	@Test
	public void shouldListDeployedDrivers() throws Exception{
		manager.deployDriver(driver.upDriver, driver, "id");
		manager.deployDriver(driver.upDriver, new DriverSpy());
		manager.deployDriver(driver.upDriver, new DriverSpy());
		manager.undeployDriver("notMyId");
		assertNotNull(manager.listDrivers());
		assertEquals(3,manager.listDrivers().size());
	}
	
	@Test
	public void shouldFindDeployedDriverAtEquivalenceTree() throws Exception {
		manager.deployDriver(driver.upDriver, driver, "id");
		assertNotNull(manager.listDrivers());
		assertEquals(manager.listDrivers().size(), 1);
		assertTrue(manager.getDriverFromEquivalanceTree(driver.upDriver.getName()).equals(driver.upDriver));
	}
	
	@Test
	public void shouldNotFindNotDeployedDriverAtEquivalenceTree() throws Exception {
		manager.deployDriver(driver.upDriver, driver, "id");
		assertNotNull(manager.listDrivers());
		assertEquals(manager.listDrivers().size(), 1);
		assertNull(manager.getDriverFromEquivalanceTree(driver.upDriver.getName() + "x"));
	}
	
	
	@Test
	public void shouldListNullWhenNothingWasDeployed() throws Exception{
		assertNull(manager.listDrivers());
	}
	
	@Test
	public void shouldInitTheDeployedDriver() throws DriverManagerException, InterfaceValidationException, DriverNotFoundException{
		assertFalse(driver.inittiated);
		manager.deployDriver(driver.upDriver, driver, "id");
		assertFalse(driver.inittiated);
		manager.initDrivers(null);
		assertTrue(driver.inittiated);
	}
	
	@Test
	public void shouldInitTheDeployedDriverEvenWhenThereIsAHomonimousDriverOnAnotherDevice() throws DriverManagerException, InterfaceValidationException, DriverNotFoundException{
		assertFalse(driver.inittiated);
		dao.insert(new DriverModel("id", driver.getDriver(), "otherDevice"));
		manager.deployDriver(driver.upDriver, driver, "id");
		assertFalse(driver.inittiated);
		manager.initDrivers(null);
		assertTrue(driver.inittiated);
	}
	
	@Test
	public void shouldInitWithTheGivenGateway() throws DriverManagerException, InterfaceValidationException, DriverNotFoundException{
		assertFalse(driver.inittiated);
		manager.deployDriver(driver.upDriver, driver, "id");
		assertFalse(driver.inittiated);
		Gateway gtw = mock(Gateway.class);
		manager.initDrivers(gtw);
		assertEquals(gtw,driver.gateway);
	}
	
	@Test
	public void shouldInitAllDeployedDriver() throws DriverManagerException, InterfaceValidationException, DriverNotFoundException{
		DriverSpy drivers[] = new DriverSpy[]{new DriverSpy(),new DriverSpy(),new DriverSpy()};
		for (DriverSpy m : drivers){
			manager.deployDriver(m.upDriver, m);
		}
		manager.initDrivers(null);
		int i =0;
		for (DriverSpy m : drivers){
			assertTrue("driver["+i+++"]",m.inittiated);
		}
	}
	
	@Test
	public void shouldInitNewDeployedDrivers() throws DriverManagerException, InterfaceValidationException, DriverNotFoundException{
		// Init only one
		DriverSpy drivers[] = new DriverSpy[]{new DriverSpy(),new DriverSpy(),new DriverSpy()};
		manager.deployDriver(driver.upDriver, driver, "id");
		manager.initDrivers(null);
		assertTrue(driver.inittiated);
		int i =0;
		for (DriverSpy m : drivers){
			assertFalse("driver["+i+++"]",m.inittiated);
		}
		// deploy the rest
		for (DriverSpy m : drivers){
			manager.deployDriver(m.upDriver, m);
		}
		manager.initDrivers(null);
		i =0;
		for (DriverSpy m : drivers){
			assertTrue("driver["+i+++"]",m.inittiated);
		}
	}
	
	@Test
	public void shouldNotInitTheSameDriverTwice() throws DriverManagerException, InterfaceValidationException, DriverNotFoundException{
		// Init only one
		DriverSpy before = new DriverSpy();
		DriverSpy after = new DriverSpy();
		
		manager.deployDriver(before.upDriver, before);
		manager.initDrivers(null);
		
		assertEquals(1, before.initCount);
		assertEquals(0, after.initCount);
		
		manager.deployDriver(after.upDriver, after);
		manager.initDrivers(null);
		
		assertEquals(1, before.initCount);
		assertEquals(1, after.initCount);
	}
	
	@Test
	public void shouldDoNothingWithoutDriversToInit() throws DriverManagerException, InterfaceValidationException{
		manager.initDrivers(null);
	}
	
	@Test public void mustHaveADeviceDriverByDefault() throws Exception{
		manager.initDrivers(null);
		assertThat(manager.listDrivers())
			.haveExactly(1, new Condition<UosDriver>() {
				public boolean matches(UosDriver value) {
					return value.getDriver().getName()
								.equals("uos.DeviceDriver");
				}
			});
		
	}
	
	@Test public void mustHaveADeviceDriverByDefaultNoMatterHowMuchTimesWeCallInit() throws Exception{
		manager.initDrivers(null);
		manager.initDrivers(null);
		manager.initDrivers(null);
		assertThat(manager.listDrivers())
			.haveExactly(1, new Condition<UosDriver>() {
				public boolean matches(UosDriver value) {
					return value.getDriver().getName()
								.equals("uos.DeviceDriver");
				}
			});
		
	}
	
	@Test public void dontDeployANewDeviceDriverIfOneWasAlreadyDeclared() throws Exception{
		DeviceDriver deviceDriver = new DeviceDriver();
		manager.deployDriver(deviceDriver.getDriver(), deviceDriver,"my");
		manager.initDrivers(null);
		assertThat(manager.listDrivers())
			.haveExactly(1, new Condition<UosDriver>() {
				public boolean matches(UosDriver value) {
					return value.getDriver().getName()
								.equals("uos.DeviceDriver");
				}
			});
		
	}
	
	@Test
	public void shouldDoNothingWithoutDriversToTearDown() throws DriverManagerException, InterfaceValidationException{
		manager.tearDown();
	} 

	@Test
	public void shouldTearDownTheDeployedDriver() throws Exception{
		assertFalse(driver.destroyed);
		manager.deployDriver(driver.upDriver, driver);
		manager.initDrivers(null);
		assertFalse(driver.destroyed);
		manager.tearDown();
		assertTrue(driver.destroyed);
	}
	
	@Test
	public void shouldTearDownAllDeployedDrivers() throws DriverManagerException, InterfaceValidationException, DriverNotFoundException{
		DriverSpy drivers[] = new DriverSpy[]{new DriverSpy(),new DriverSpy(),new DriverSpy()};
		for (DriverSpy m : drivers) manager.deployDriver(m.upDriver, m);
		manager.initDrivers(null);
		manager.tearDown();
		int i =0;
		for (DriverSpy m : drivers)	assertTrue("driver["+i+++"]",m.destroyed);
	}
	
	@Test
	public void shouldNotTearDownADeployedDriverTwice() throws DriverManagerException, InterfaceValidationException, DriverNotFoundException{
		DriverSpy firstWave[] = new DriverSpy[]{new DriverSpy(),new DriverSpy(),new DriverSpy()};
		for (DriverSpy m : firstWave)	manager.deployDriver(m.upDriver, m);
		manager.initDrivers(null);
		manager.tearDown();
		int i =0;
		for (DriverSpy m : firstWave)	assertTrue("driver["+i+++"]",m.destroyed);
		
		DriverSpy secondWave[] = new DriverSpy[]{new DriverSpy(),new DriverSpy()};
		for (DriverSpy m : secondWave)	manager.deployDriver(m.upDriver, m);
		manager.initDrivers(null);
		manager.tearDown();
		i =0;
		for (DriverSpy m : secondWave)	assertTrue("driver["+i+++"]",m.destroyed);
		i =0;
		for (DriverSpy m : firstWave)	assertEquals("driver["+i+++"]",1,m.destroyCount);
	}

	@Test public void shouldNotTearDownUninitializedDrivers() throws DriverManagerException, InterfaceValidationException, DriverNotFoundException{
		DriverSpy firstWave[] = new DriverSpy[]{new DriverSpy(),new DriverSpy(),new DriverSpy()};
		for (DriverSpy m : firstWave) manager.deployDriver(m.upDriver, m);
		manager.initDrivers(null);
		DriverSpy secondeWave[] = new DriverSpy[]{new DriverSpy(),new DriverSpy()};
		for (DriverSpy m : secondeWave) manager.deployDriver(m.upDriver, m);
		manager.tearDown();
		int i =0;
		for (DriverSpy m : firstWave)	assertTrue("driver["+i+++"]",m.destroyed);
		i =0;
		for (DriverSpy m : secondeWave)	assertFalse("driver["+i+++"]",m.destroyed);
	}
	
	@Test
	public void shouldAllowToAccessAnLocalInstanceUsingTheId() throws DriverManagerException, InterfaceValidationException, DriverNotFoundException{
		manager.deployDriver(driver.upDriver, driver, "my.id");
		assertNotNull(manager.driver("my.id"));
		assertEquals(driver,manager.driver("my.id"));
	}
	
	@Test
	public void shouldAllowToAccessAnLocalInstanceUsingTheIdWithoutConfusingWIthOthers() throws DriverManagerException, InterfaceValidationException, DriverNotFoundException{
		dao.insert(new DriverModel("my.id", new UpDriver("driver"), "otherDevice"));
		manager.deployDriver(driver.upDriver, driver, "my.id");
		assertNotNull(manager.driver("my.id"));
		assertEquals(driver,manager.driver("my.id"));
	}
	
	@Test
	public void shouldNotAllowToAccessAnLocalInstanceUsingAnInvalidId() throws DriverManagerException, InterfaceValidationException, DriverNotFoundException{
		manager.deployDriver(driver.upDriver, driver, "my.id");
		assertNull(manager.driver("not.my.id"));
	}
	
	@Test(expected = DriverManagerException.class)
	public void shouldNotInsertDriverDueToIncorretParameters() throws DriverManagerException, DriverNotFoundException, InterfaceValidationException{
		UpDriver driver = new UpDriver("d1");
		driver.addService("s1").addParameter("param", ParameterType.MANDATORY);
		
		UpDriver notEquivalentdriver = new UpDriver("d2");
		notEquivalentdriver.addService("s1").addParameter("param", ParameterType.MANDATORY).addParameter("param2", ParameterType.OPTIONAL);
		notEquivalentdriver.addEquivalentDrivers(driver.getName());
		
		manager.insert(new DriverModel("id1",driver,currentDevice.getName()));
		List<DriverData> list = manager.listDrivers("d1", currentDevice.getName());
		assertNotNull(list);
		assertEquals(1,list.size());
		assertEquals("id1",list.get(0).getInstanceID());
		assertEquals(currentDevice,list.get(0).getDevice());
		assertEquals(driver,list.get(0).getDriver());
		
		manager.insert(new DriverModel("id2", notEquivalentdriver,currentDevice.getName()));
	}
	
	@Test(expected = DriverManagerException.class)
	public void shouldNotInsertDriverDueToParentServiceNullAndEquivalentServiceNotNull() throws DriverManagerException, DriverNotFoundException, InterfaceValidationException{
		UpDriver driver = new UpDriver("d1");
		
		UpDriver notEquivalentdriver = new UpDriver("d2");
		notEquivalentdriver.addService("s1").addParameter("param", ParameterType.MANDATORY).addParameter("param2", ParameterType.OPTIONAL);
		notEquivalentdriver.addEquivalentDrivers(driver.getName());
		
		manager.insert(new DriverModel("id1",driver,currentDevice.getName()));
		List<DriverData> list = manager.listDrivers("d1", currentDevice.getName());
		assertNotNull(list);
		assertEquals(1,list.size());
		assertEquals("id1",list.get(0).getInstanceID());
		assertEquals(currentDevice,list.get(0).getDevice());
		assertEquals(driver,list.get(0).getDriver());
		
		manager.insert(new DriverModel("id2", notEquivalentdriver,currentDevice.getName()));
	}
	
	@Test(expected = DriverManagerException.class)
	public void shouldNotInsertDriverDueToParentServiceMissingParameter() throws DriverManagerException, DriverNotFoundException, InterfaceValidationException{
		UpDriver driver = new UpDriver("d1");
		driver.addService("s1");
		
		UpDriver notEquivalentdriver = new UpDriver("d2");
		notEquivalentdriver.addService("s1").addParameter("param", ParameterType.MANDATORY);
		notEquivalentdriver.addEquivalentDrivers(driver.getName());
		
		manager.insert(new DriverModel("id1",driver,currentDevice.getName()));
		List<DriverData> list = manager.listDrivers("d1", currentDevice.getName());
		assertNotNull(list);
		assertEquals(1,list.size());
		assertEquals("id1",list.get(0).getInstanceID());
		assertEquals(currentDevice,list.get(0).getDevice());
		assertEquals(driver,list.get(0).getDriver());
		
		manager.insert(new DriverModel("id2", notEquivalentdriver,currentDevice.getName()));
	}
	
	@Test 
	public void shouldRetrieveALocalDriverByDriverName() throws DriverManagerException, DriverNotFoundException{
		UpDriver driver = new UpDriver("d1");
		manager.insert(new DriverModel("id1",driver,currentDevice.getName()));
		List<DriverData> list = manager.listDrivers("d1", currentDevice.getName());
		assertNotNull(list);
		assertEquals(1,list.size());
		assertEquals("id1",list.get(0).getInstanceID());
		assertEquals(currentDevice,list.get(0).getDevice());
		assertEquals(driver,list.get(0).getDriver());
	}
	
	@Test 
	public void shouldRetrieveALocalDriverAndItsEquivalentDriverByDriverName() throws DriverManagerException, DriverNotFoundException{
		UpDriver driver = new UpDriver("d1");
		manager.insert(new DriverModel("id1",driver,currentDevice.getName()));
		UpDriver equivalentDriver = new UpDriver("d2");
		equivalentDriver.addEquivalentDrivers(driver.getName());
		manager.insert(new DriverModel("id2",equivalentDriver,currentDevice.getName()));
		
		List<DriverData> list = manager.listDrivers("d1", currentDevice.getName());
		assertNotNull(list);
		assertEquals(2,list.size());
		assertEquals("id1",list.get(0).getInstanceID());
		assertEquals(currentDevice,list.get(0).getDevice());
		assertEquals(driver,list.get(0).getDriver());
		assertEquals("id2",list.get(1).getInstanceID());
		assertEquals(currentDevice,list.get(1).getDevice());
		assertEquals(equivalentDriver,list.get(1).getDriver());
	}
	
	@Test 
	public void shouldRetrieveALocalDriverAndItsEquivalentDriversByDriverName() throws DriverManagerException, DriverNotFoundException{
		UpDriver driver0 = new UpDriver("d0");
		manager.insert(new DriverModel("id0",driver0,currentDevice.getName()));
		
		UpDriver driver1 = new UpDriver("d1");
		driver1.addEquivalentDrivers(driver0.getName());
		manager.insert(new DriverModel("id1",driver1,currentDevice.getName()));
		
		UpDriver driver2 = new UpDriver("d2");
		driver2.addEquivalentDrivers(driver0.getName());
		manager.insert(new DriverModel("id2",driver2,currentDevice.getName()));
		
		UpDriver equivalentToDriver2 = new UpDriver("equivalentToD2");
		equivalentToDriver2.addEquivalentDrivers(driver2.getName());
		manager.insert(new DriverModel("id3",equivalentToDriver2,currentDevice.getName()));
		
		UpDriver equivalentToEquivalentToDriver2 = new UpDriver("equivalentToEquivalentToD2");
		equivalentToEquivalentToDriver2.addEquivalentDrivers(equivalentToDriver2.getName());
		equivalentToEquivalentToDriver2.addEquivalentDrivers(driver1.getName());
		manager.insert(new DriverModel("id4",equivalentToEquivalentToDriver2,currentDevice.getName()));
		
		List<DriverData> list = manager.listDrivers("d0", currentDevice.getName());
		assertNotNull(list);
		assertEquals(5,list.size());
		
		assertEquals("id0",list.get(0).getInstanceID());
		assertEquals(currentDevice,list.get(0).getDevice());
		assertEquals(driver0,list.get(0).getDriver());
		
		assertEquals("id1",list.get(1).getInstanceID());
		assertEquals(currentDevice,list.get(1).getDevice());
		assertEquals(driver1,list.get(1).getDriver());
		
		assertEquals("id4",list.get(2).getInstanceID());
		assertEquals(currentDevice,list.get(2).getDevice());
		assertEquals(equivalentToEquivalentToDriver2,list.get(2).getDriver());
		
		assertEquals("id2",list.get(3).getInstanceID());
		assertEquals(currentDevice,list.get(3).getDevice());
		assertEquals(driver2,list.get(3).getDriver());
		
		assertEquals("id3",list.get(4).getInstanceID());
		assertEquals(currentDevice,list.get(4).getDevice());
		assertEquals(equivalentToDriver2,list.get(4).getDriver());
	}
	
	@Test 
	public void shouldNotRetrieveALocalDriverByWrongDriverName() throws DriverManagerException, DriverNotFoundException{
		UpDriver driver = new UpDriver("d1");
		manager.insert(new DriverModel("id1",driver,currentDevice.getName()));
		assertNull(manager.listDrivers("d2", currentDevice.getName()));
	}
	
	@Test 
	public void shouldRetrieveMultipleInstancesLocalDriversByDriverName() throws DriverManagerException, DriverNotFoundException{
		UpDriver driver = new UpDriver("d1");
		manager.insert(new DriverModel("id1",driver,currentDevice.getName()));
		manager.insert(new DriverModel("id2",driver,currentDevice.getName()));
		manager.insert(new DriverModel("id3",driver,currentDevice.getName()));
		List<DriverData> list = manager.listDrivers("d1", currentDevice.getName());
		assertNotNull(list);
		assertEquals(3,list.size());
		assertEquals("id1",list.get(0).getInstanceID());
		assertEquals(driver,list.get(0).getDriver());
		assertEquals("id2",list.get(1).getInstanceID());
		assertEquals(driver,list.get(1).getDriver());
		assertEquals("id3",list.get(2).getInstanceID());
		assertEquals(driver,list.get(2).getDriver());
	}
	
	public static class DriverSpy implements UosDriver {
		boolean called = false;
		boolean inittiated = false;
		boolean destroyed = false;
		int initCount = 0;
		int destroyCount = 0;
		UpDriver upDriver;
		String id;
		Gateway gateway;
		List<UpDriver> parent;
		public DriverSpy() {
			this("driver");
		}
		public DriverSpy(String name) {
			upDriver = new UpDriver(name);
			UpService service = new UpService("s");
			service.addParameter("p", ParameterType.MANDATORY);
			upDriver.addService(service);
			parent = new ArrayList<UpDriver>();
		}
		public void init(Gateway gateway, String instanceId) {
			inittiated = true;
			initCount++;
			id = instanceId;
			this.gateway = gateway;
		}
		public UpDriver getDriver() {return upDriver;}
		public void destroy() {
			destroyed = true;
			destroyCount++;
		}
		public void service(Call s, Response r, CallContext c){
			called = true;
		}
		@Override
		public List<UpDriver> getParent() {
			return parent;
		}
	}
}
