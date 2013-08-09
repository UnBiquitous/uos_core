package org.unbiquitous.uos.core.network.connectionManager;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.unbiquitous.uos.core.UOSLogging;
import org.unbiquitous.uos.core.network.model.connection.ClientConnection;

/**
 * Auxiliary Threaded class for notifying the connectionManagerListeners about 
 * connections received from the client
 * 
 * @author Passarinho
 */
public class ThreadedConnectionHandler extends Thread {
    
	private static char MESSAGE_SEPARATOR = '\n';
	private static int MAX_NOT_READY_TRIES = 30;
	private static int NOT_READY_SLEEP_TIME = 100;
	
	/* *****************************
	 *   	ATRUBUTES
	 * *****************************/
	
	/** Object for logging registration.*/
    private static final Logger logger = UOSLogging.getLogger();
	
    /** A Connection to the client */
	private ClientConnection con = null;
	
	/** A Connection Manager Listener */
	private MessageListener messageListener = null;
	
	
	/* *****************************
	 *   	CONSTRUCTOR
	 * *****************************/
	
	/**
	 * A constructor who receives a ClientConnection and a ConnectionManagerListener as params
	 */
	public ThreadedConnectionHandler(ClientConnection con, MessageListener messageListener) {
        this.con = con;
        this.messageListener = messageListener;
    }
	
	/* *****************************
     *   	PUBLIC  METHODS - Thread
     * *****************************/

	
    public void run() {
    	handleUbiquitOSSmartSpaceConnection();
    }
    
    
    /* *****************************
     *   	PRIVATE  METHODS
     * *****************************/
    
    /**
     * Handle a ubiquitos-client connection request.
     * @param con The connection established.
     */
    private void handleUbiquitOSSmartSpaceConnection() {
        logger.fine("Connection received from an ubiquitos-client device :'"+con.getClientDevice().getNetworkDeviceName()+"' on '"+con.getClientDevice().getNetworkDeviceType()+"'.");
        
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(con.getDataInputStream()));
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(con.getDataOutputStream()));
            /**
             * The strategy is as follows:
             * 
             * For performance purpose we verify if the buffer is ready to be read. If not, we try again,
             * pooling can waste too much CPU, so we retry sometimes before go for a snooze before trying again. 
             * 
             * One connection can handle multiple messages, so we collect each character from the message until its 
             * separator come.
             * 
             */
            int notReadyCount = 0;
            while(con.isConnected()){
            	if (reader.ready()){
	            	logger.info("Receiving Message ...");
	            	
	            	StringBuilder builder = new StringBuilder();
	            	for(Character c = (char)reader.read();c != MESSAGE_SEPARATOR;c = (char)reader.read()){
	            		builder.append(c);
	            	}
	            	logger.info("Received Message: "+builder.toString());
	            	
	            	String returnedMessage;
					try {
						returnedMessage = messageListener.handleIncomingMessage(builder.toString(),con.getClientDevice());
						writer.write(returnedMessage+MESSAGE_SEPARATOR);
						writer.flush();
						logger.fine("Message Handled");
					} catch (Exception e) {
						logger.log(Level.SEVERE,"Failed to handle ubiquitos-smartspace connection.", e);
					}


            	}else{
            		notReadyCount++;
            	}
            	
            	if (notReadyCount < MAX_NOT_READY_TRIES){
            		Thread.sleep(NOT_READY_SLEEP_TIME);
            	}
            }
            
        } catch (Exception e) {
            logger.log(Level.SEVERE,"Failed to handle ubiquitos-smartspace connection.", e);
        } finally {
            try {
                con.closeConnection();
                con = null;
                logger.log(Level.INFO,"Closing Connection !!!");
            } catch (IOException ex) {
                logger.log(Level.SEVERE,"Failed to close ubiquitos-smartspace connection.", ex);
            }
        }
    }
} 
