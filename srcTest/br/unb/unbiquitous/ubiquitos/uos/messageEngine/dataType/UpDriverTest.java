package br.unb.unbiquitous.ubiquitos.uos.messageEngine.dataType;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class UpDriverTest {

	@Test
	public void equalsNull() {
		assertFalse(new UpDriver(null).equals(null));
	}
	
	@Test public void notEquals(){
		assertFalse(new UpDriver("oneThing").equals(new UpDriver("otherThing")));
	}
	
	@Test public void notEqualsToOtherThing(){
		assertFalse(new UpDriver("oneThing").equals("otherThing"));
	}
	
	@Test public void notEqualsWithNullName(){
		assertFalse(new UpDriver(null).equals(new UpDriver("name")));
	}
	
	@Test public void notEqualsWithDifferentServices(){
		UpDriver driver1 = new UpDriver("driver");
		driver1.addService("s1");
		UpDriver driver2 = new UpDriver("driver");
		driver2.addService("s2");
		assertFalse(driver1.equals(driver2));
	}
	
	@Test public void notEqualsWithDifferentWithOneDriverWithNoServices(){
		UpDriver driver1 = new UpDriver("driver");
		UpDriver driver2 = new UpDriver("driver");
		driver2.addService("s2");
		assertFalse(driver1.equals(driver2));
	}
	
	@Test public void notEqualsWithDifferentEvents(){
		UpDriver driver1 = new UpDriver("driver");
		driver1.addEvent("e1");
		UpDriver driver2 = new UpDriver("driver");
		driver2.addEvent("e2");
		assertFalse(driver1.equals(driver2));
	}
	
	@Test public void notEqualsWithDifferentWithOneDriverWithNoEvents(){
		UpDriver driver1 = new UpDriver("driver");
		UpDriver driver2 = new UpDriver("driver");
		driver2.addEvent("e2");
		assertFalse(driver1.equals(driver2));
	}
	
	@Test public void equalsWithBothWithNullName(){
		assertTrue(new UpDriver(null).equals(new UpDriver(null)));
	}

	@Test public void equalsWithNameEquals(){
		assertTrue(new UpDriver("driver").equals(new UpDriver("driver")));
	}
	
	@Test public void equalsWithEqualsServices(){
		UpDriver driver1 = new UpDriver("driver");
		driver1.addService("s1");
		UpDriver driver2 = new UpDriver("driver");
		driver2.addService("s1");
		assertTrue(driver1.equals(driver2));
	}
	
	@Test public void equalsWithEqualsEvents(){
		UpDriver driver1 = new UpDriver("driver");
		driver1.addEvent("e1");
		UpDriver driver2 = new UpDriver("driver");
		driver2.addEvent("e1");
		assertTrue(driver1.equals(driver2));
	}

}
