package br.unb.unbiquitous.ubiquitos.uos.driverManager;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import br.unb.unbiquitous.ubiquitos.uos.messageEngine.dataType.UpDevice;
import br.unb.unbiquitous.ubiquitos.uos.messageEngine.dataType.UpDriver;


public class DriverDataTest {

	
	@Test public void notEqualsNull(){
		assertFalse(new DriverData(null,null,null).equals(null));
	}
	
	@Test public void notEqualsOtherThings(){
		assertFalse(new DriverData(null,null,null).equals(new Object()));
	}
	
	@Test public void notEqualsWithDifferentDriver(){
		DriverData thiz = new DriverData(new UpDriver("a"),null,null);
		DriverData that = new DriverData(new UpDriver("b"),null,null);
		assertFalse(thiz.equals(that));
	}
	
	@Test public void notEqualsWithDifferentDevice(){
		DriverData thiz = new DriverData(new UpDriver("a"),new UpDevice("da"),null);
		DriverData that = new DriverData(new UpDriver("b"),new UpDevice("db"),null);
		assertFalse(thiz.equals(that));
	}
	
	@Test public void notEqualsWithDifferentInstanceId(){
		DriverData thiz = new DriverData(new UpDriver("a"),new UpDevice("da"),"ida");
		DriverData that = new DriverData(new UpDriver("b"),new UpDevice("db"),"idb");
		assertFalse(thiz.equals(that));
	}
	
	@Test public void equalsWithAllDataEquals(){
		DriverData thiz = new DriverData(new UpDriver("a"),new UpDevice("da"),"ida");
		DriverData that = new DriverData(new UpDriver("a"),new UpDevice("da"),"ida");
		assertTrue(thiz.equals(that));
	}
	
}
