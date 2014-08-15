package org.unbiquitous.uos.core;

import static org.junit.Assert.assertTrue;

public class TestUtils {

	public static interface EventuallyAssert{
		boolean assertion();
	}
	
	public static void assertEventuallyTrue(String msg, long wait, EventuallyAssert assertion) throws InterruptedException{
		long time = 0;
		while (time <= wait && !assertion.assertion()){
			Thread.sleep(10);
			time += 10;
		}
		assertTrue(msg,assertion.assertion());
	}
}
