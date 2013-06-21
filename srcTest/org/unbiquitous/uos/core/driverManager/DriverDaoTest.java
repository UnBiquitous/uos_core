package org.unbiquitous.uos.core.driverManager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.unbiquitous.uos.core.driverManager.DriverDao;
import org.unbiquitous.uos.core.driverManager.DriverModel;
import org.unbiquitous.uos.core.messageEngine.dataType.UpDriver;
import org.unbiquitous.uos.core.messageEngine.dataType.UpService.ParameterType;



public class DriverDaoTest {

	private DriverDao dao;
	
	@Before public void setUp(){
		dao = new DriverDao(null);
	}
	
	@After public void tearDown(){
		dao.clear();
	}
	
	private DriverModel createDriver(String id, String name) {
		return new DriverModel(id, new UpDriver(name),"D");
	}
	private DriverModel createDriver(String id, String name, String device) {
		return new DriverModel(id, new UpDriver(name),device);
	}
	
	@Test public void mustBeResilientToMultipleDaos(){
		new DriverDao(null);
		new DriverDao(null);
		new DriverDao(null);
		//If don't throw an exception is okay
	}
	
	@Test public void returnsAnInsertedDriver(){
		UpDriver interface_ = new UpDriver("driver.name");
		interface_.addService("service").addParameter("param", ParameterType.MANDATORY).addParameter("opt", ParameterType.OPTIONAL);
		interface_.addEvent("evt");
		DriverModel driver = new DriverModel("instanceID",interface_,"deviceName"); 
		
		dao.insert(driver);
		assertNotNull(dao.list());
		assertFalse(dao.list().isEmpty());
		assertEquals(1,dao.list().size());
		assertEquals(driver.driver(),dao.list().get(0).driver());
		assertEquals(driver.id(),dao.list().get(0).id());
		assertEquals(driver.device(),dao.list().get(0).device());
		assertNotNull(driver.rowid()); // must set the rowid
		assertEquals(driver.rowid(),dao.list().get(0).rowid());
	}
	
	@Test public void insertMultipleDrivers(){
		dao.insert(createDriver("a", "a"));
		dao.insert(createDriver("b", "b"));
		dao.insert(createDriver("c", "c"));
		dao.insert(createDriver("d", "d"));
		assertEquals(4,dao.list().size());
	}
	
	@Test public void mustClearDataWhenRequested(){
		assertEquals(0,dao.list().size());
		dao.insert(createDriver("a", "a"));
		assertEquals(1,dao.list().size());
		dao.clear();
		assertEquals(0,dao.list().size());
	}
	
	@Test(expected=Exception.class)
	public void mustNotAcceptTwoDriversWithTheSameIdAndSameDevice(){
		dao.insert(createDriver("a", "a"));
		dao.insert(createDriver("a", "a"));
	}
	
	@Test public void mustAcceptTwoDriversWithTheSameIdAndDifferentDevices(){
		dao.insert(createDriver("a", "a", "a"));
		dao.insert(createDriver("a", "a", "b"));
		assertEquals(2,dao.list().size());
	}
	
	@Test public void mustDeleteADriver(){
		dao.insert(createDriver("a", "a"));
		dao.delete("a","D");
		assertEquals(0,dao.list().size());
	}
	
	@Test public void mustDeleteOnlyTheSpecificDriver(){
		dao.insert(createDriver("a", "a"));
		dao.insert(createDriver("b", "b"));
		dao.insert(createDriver("c", "c"));
		dao.delete("b","D");
		assertEquals(2,dao.list().size());
	}
	
	@Test public void mustDeleteOnlyTheSpecificDriverFromTheSpecificDevice(){
		dao.insert(createDriver("a", "a", "Da"));
		dao.insert(createDriver("a", "a"));
		dao.insert(createDriver("b", "b"));
		dao.insert(createDriver("c", "c"));
		dao.delete("a","Da");
		assertEquals(3,dao.list().size());
	}
	
	@Test public void mustDeleteOnlyOnTheDataSpecifyied(){
		dao.insert(createDriver("ida", "dra", "deva"));
		dao.insert(createDriver("id1", "dra", "deva"));
		dao.insert(createDriver("idb", "drb", "deva"));
		dao.insert(createDriver("ida", "dra", "devb"));
		dao.insert(createDriver("idd", "drd", "devb"));
		dao.delete("ida","devb");
		assertEquals(4,dao.list().size());
		assertEquals(3,dao.list(null,"deva").size());
		assertEquals(2,dao.list("dra").size());
	}
	
	@Test public void findADriverByName(){
		dao.insert(createDriver("a", "a"));
		assertEquals(1, dao.list("a").size());
	}
	
	@Test public void dontFindADriverByTheWronfName(){
		dao.insert(createDriver("a", "a"));
		assertEquals(0, dao.list("b").size());
	}
	
	@Test public void findADriverByNameIgnoringCase(){
		dao.insert(createDriver("a", "a"));
		dao.insert(createDriver("b", "B"));
		dao.insert(createDriver("c", "My.NaMe"));
		assertEquals(1, dao.list("A").size());
		assertEquals(1, dao.list("b").size());
		assertEquals(1, dao.list("mY.nAmE").size());
	}
	
	@Test public void findOnlyTheWantedDriverByName(){
		dao.insert(createDriver("a", "a"));
		dao.insert(createDriver("b", "b"));
		dao.insert(createDriver("c", "c"));
		DriverModel driver = dao.list("b").get(0);
		assertEquals("b", driver.id());
		assertEquals("b", driver.driver().getName());
	}
	
	@Test public void findMultipleWantedDriversByName(){
		dao.insert(createDriver("b1", "b"));
		dao.insert(createDriver("a", "a"));
		dao.insert(createDriver("b2", "b"));
		dao.insert(createDriver("c", "c"));
		dao.insert(createDriver("b3", "b"));
		assertEquals(3, dao.list("b").size());
	}
	
	@Test public void findAllWhenDriverNameIsNull(){
		dao.insert(createDriver("a", "a"));
		dao.insert(createDriver("b", "b"));
		assertEquals(2, dao.list(null).size());
	}
	
	@Test public void findADriverByDevice(){
		dao.insert(createDriver("a", "a", "D"));
		assertEquals(1, dao.list(null,"D").size());
	}
	
	@Test public void dontFindADriverByTheWrongDevice(){
		dao.insert(createDriver("a", "a", "D"));
		assertEquals(0, dao.list(null,"Da").size());
	}
	
	@Test public void findAllDriversMathcingDevice(){
		dao.insert(createDriver("a", "a", "A"));
		dao.insert(createDriver("b", "b", "D"));
		dao.insert(createDriver("c", "c", "C"));
		dao.insert(createDriver("d", "d", "D"));
		assertEquals(2, dao.list(null,"D").size());
	}
	
	@Test public void findADriverByDeviceIgnoringCase(){
		dao.insert(createDriver("a","a", "a"));
		dao.insert(createDriver("b","b", "B"));
		dao.insert(createDriver("c","c", "My.DeViCe"));
		assertEquals(1, dao.list(null,"A").size());
		assertEquals(1, dao.list(null,"b").size());
		assertEquals(1, dao.list(null,"mY.dEvIcE").size());
	}
	
	@Test public void findAllWhenDeviceIsNull(){
		dao.insert(createDriver("a","a", "a"));
		dao.insert(createDriver("b","b", "b"));
		assertEquals(2, dao.list(null,null).size());
	}
	
	@Test public void findAllDriversByNameAndDevice(){
		dao.insert(createDriver("a","a", "a"));
		dao.insert(createDriver("b1","b", "b"));
		dao.insert(createDriver("c","c", "b"));
		dao.insert(createDriver("b2","b", "b"));
		assertEquals(2, dao.list("b","b").size());
	}

	@Test public void retrieveAnDriverByIdAndDevice(){
		dao.insert(createDriver("id.a1","a", "Da"));
		DriverModel model =createDriver("id.b1","b", "Db");
		dao.insert(model);
		DriverModel driver = dao.retrieve("id.b1","Db");
		assertNotNull(driver);
		assertEquals(driver.driver(),model.driver());
		assertEquals(driver.id(),model.id());
		assertEquals(driver.device(),model.device());
		assertEquals(driver.rowid(),model.rowid());
	}
	
	@Test public void retrieveNullForNonExistingId(){
		dao.insert(createDriver("id.a1","a", "Da"));
		dao.insert(createDriver("id.b1","b", "Db"));
		assertNull(dao.retrieve("id.c1","Da"));
	}
	
	@Test public void retrieveNullForNonExistingDevice(){
		dao.insert(createDriver("id.a1","a", "Da"));
		dao.insert(createDriver("id.b1","b", "Db"));
		assertNull(dao.retrieve("id.a1","Dc"));
	}
}
