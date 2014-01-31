package org.unbiquitous.uos.core;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class UOSLogging {
	static Logger logger ;
	
	public static Logger getLogger(){
		if (logger == null){
			initLogger();
		}
		return logger;
	}
	
	private static void initLogger() {
		logger = Logger.getLogger("ubiquitos");
		logger.setLevel(Level.FINE);
		ConsoleHandler handler = new ConsoleHandler();
		handler.setLevel(logger.getLevel());
		handler.setFormatter(new Formatter() {
			
			@Override
			public String format(LogRecord record) {
				String baseMessage = String.format("%s -[%s.%s] (%s.%s) : %s\n",
						record.getLevel(),
						record.getThreadID(),
						record.getSequenceNumber(),
						record.getSourceClassName(),
						record.getSourceMethodName(),
						record.getMessage()
						);
				
				if (record.getThrown() != null ){
					StringWriter sw = new StringWriter();
					PrintWriter pw = new PrintWriter(sw);
					record.getThrown().printStackTrace(pw);
					baseMessage += sw.toString()+'\n';
				}
				return baseMessage;
			}
		});
		logger.setUseParentHandlers(false);
		logger.addHandler(handler);
	}
	
}
