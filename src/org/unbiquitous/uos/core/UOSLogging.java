package org.unbiquitous.uos.core;

import java.util.logging.Logger;

public class UOSLogging {

	public static Logger getLogger(){
		StackTraceElement clazz = Thread.currentThread().getStackTrace()[1];
		String className = clazz.getClassName();
		return Logger.getLogger(className );
	}
	
}
