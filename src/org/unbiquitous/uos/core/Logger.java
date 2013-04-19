package org.unbiquitous.uos.core;

import java.util.logging.Level;

@SuppressWarnings("rawtypes")
public class Logger {
	private java.util.logging.Logger logger;
	private final Class clazz;

	public static Logger getLogger(Class clazz){
		return new Logger(clazz);
	}
	
	public Logger( Class clazz) {
		this.clazz = clazz;
		logger = java.util.logging.Logger.getLogger(clazz.getName());
		logger.setLevel(Level.FINE);
//		java.util.logging.Logger.getGlobal().setLevel(Level.FINE);
//		logger.setParent(java.util.logging.Logger.getGlobal());
	}
	
	public void error(Throwable e){
		//TODO: discover calling method
		logger.log(Level.SEVERE,clazz.getName(), e);
	}
	
	public void error(String msg, Throwable e){
		logger.log(Level.SEVERE,msg, e);
	}
	
	public void error(String msg){
		logger.log(Level.SEVERE,msg);
	}
	
	public void fatal(String msg){
		logger.log(Level.SEVERE,msg);
	}
	
	public void fatal(String msg, Throwable e){
		logger.log(Level.SEVERE,msg, e);
	}
	
	public void warn(String msg){
		logger.log(Level.WARNING,msg);
	}
	
	public void debug(String msg){
//		logger.fine(msg);
		logger.log(Level.INFO, msg);
	}
	
	public void debug(String msg, Throwable e){
//		logger.log(Level.FINE,msg, e);
		logger.log(Level.INFO,msg, e);
	}
	
	public void info(String msg){
		logger.log(Level.INFO,msg);
	}
	
	public void info(Throwable e){
		logger.log(Level.INFO,"", e);
	}
	
	public void info(String msg,Throwable e){
		logger.log(Level.INFO,msg, e);
	}
	
}
