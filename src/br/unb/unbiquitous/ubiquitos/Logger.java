package br.unb.unbiquitous.ubiquitos;

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
	}
	
	public void error(Throwable e){
		//TODO: discover calling method
		logger.throwing(clazz.getName(), "", e);
	}
	
	public void error(String msg, Throwable e){
		logger.log(Level.SEVERE,msg, e);
	}
	
	public void error(String msg){
		logger.severe(msg);
	}
	
	public void fatal(String msg){
		logger.log(Level.SEVERE,msg);
	}
	
	public void fatal(String msg, Throwable e){
		logger.log(Level.SEVERE,msg, e);
	}
	
	public void warn(String msg){
		logger.warning(msg);
	}
	
	public void debug(String msg){
//		logger.fine(msg);
		logger.log(Level.OFF, msg);
	}
	
	public void debug(String msg, Throwable e){
//		logger.log(Level.FINE,msg, e);
		logger.log(Level.OFF,msg, e);
	}
	
	public void info(String msg){
		logger.info(msg);
	}
	
	public void info(Throwable e){
		logger.log(Level.INFO,"", e);
	}
	
	public void info(String msg,Throwable e){
		logger.log(Level.INFO,msg, e);
	}
	
}
