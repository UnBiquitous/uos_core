package org.unbiquitous.uos.core.connectivity.proxying;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.unbiquitous.uos.core.UOSLogging;
import org.unbiquitous.uos.core.adaptabitilyEngine.Gateway;
import org.unbiquitous.uos.core.adaptabitilyEngine.ServiceCallException;
import org.unbiquitous.uos.core.adaptabitilyEngine.SmartSpaceGateway;
import org.unbiquitous.uos.core.applicationManager.CallContext;
import org.unbiquitous.uos.core.connectivity.ConnectivityException;
import org.unbiquitous.uos.core.messageEngine.dataType.UpDevice;
import org.unbiquitous.uos.core.messageEngine.dataType.UpDriver;
import org.unbiquitous.uos.core.messageEngine.dataType.UpNetworkInterface;
import org.unbiquitous.uos.core.messageEngine.messages.ServiceCall;
import org.unbiquitous.uos.core.messageEngine.messages.ServiceResponse;
import org.unbiquitous.uos.core.network.exceptions.NetworkException;


/**
 * Class responsible for representing a driver on a remote device, the real provider of the
 * driver we are representing. This driver only forwards to the real provider every service 
 * call made to us.
 * 
 * @author Lucas Paranhos Quintella
 *
 */
public class ProxyDriverImpl implements ProxyDriver {

	/** The real provider of this driver */
	private UpDevice provider;
	
	/** The interface of the real driver */
	private UpDriver driver;
	
	/** The context of the middleware */
	private Gateway gateway;
	
	/** Logging object */
	private static final Logger logger = UOSLogging.getLogger();
	
	/** Threading attribute */
	private boolean doneServiceCall;
	
	/** Time to establish the context correctly */
	private static final int TIME_TO_MAKE_THE_CONTEXT = 4000;
	
	
	/**
	 * Constructor
	 * @param driver The driver's interface
	 * @param provider The real provider of this driver
	 */
	public ProxyDriverImpl(UpDriver driver, UpDevice provider){
		this.driver = driver;
		this.provider = provider;
		this.doneServiceCall = false;
	}


	/**
	 * Method responsible for forwarding the service call to the real provider. Any service call made to
	 * us is redirected to the real provider by using this method. 
	 * @param serviceCall The service call
	 * @param serviceResponse The service response
	 * @param messageContext Our message context of streams respective to the caller device
	 */
	public synchronized void forwardServiceCall(ServiceCall serviceCall,
			ServiceResponse serviceResponse, CallContext messageContext) {

		//Sets the right channel type
		if(serviceCall.getServiceType().equals(ServiceCall.ServiceType.STREAM)){
			//Sets the right interface to do the service call
			try {
				UpNetworkInterface netInt = ((SmartSpaceGateway)this.gateway).getConnectivityManager().getAppropriateInterface(this.provider,
						serviceCall);
				serviceCall.setChannelType(netInt.getNetType());
			} catch (NetworkException e) {
				logger.severe(e.getMessage());
			}
		}
		
		//Starts a new thread to get a new message context
		Thread serviceCallThread = new ProxyServiceCall(serviceCall, serviceResponse, messageContext);
		serviceCallThread.start();
		
		synchronized(serviceCallThread){
			//Sleeps while the threaded service call doesn't finish its work
			while( !doneServiceCall ){
				try{
					serviceCallThread.wait();
				}catch(InterruptedException e){
					logger.fine("ProxyDriverImpl - Problem sleeping");
				}
			}
		}

	}
	
	
	/**
	 * Gets the real provider of this driver
	 * @return The provider device
	 */
	public UpDevice getProvider() {
		return this.provider;
	}
	
	/**
	 * Gets the interface of this driver
	 * @return The driver's interface
	 */
	public UpDriver getDriver() {
		return this.driver;
	}

	public void init(Gateway gateway, String instanceId) {
		this.gateway = gateway;
	}

	/**
	 * Tears down this driver and its dependencies
	 */
	public void destroy() {}
	
	
	/**
	 * Class responsible for making a single service call on a new thread for getting a new message
	 * context of streams.
	 * @author Lucas Paranhos Quintella
	 *
	 */
	private class ProxyServiceCall extends Thread {
		
		private ServiceCall serviceCall;
		
		private ServiceResponse serviceResponse;
		
		private CallContext messageContextBefore;
		
		private CallContext messageContextAfter;
		
		private int numberChannels;
		
		/**
		 * Constructor
		 * @param serviceCall
		 * @param serviceResponse
		 * @param messageContextBefore
		 */
		public ProxyServiceCall(ServiceCall serviceCall,
				ServiceResponse serviceResponse,
				CallContext messageContextBefore){
			
			this.serviceCall = serviceCall;
			this.serviceResponse = serviceResponse;
			this.messageContextBefore = messageContextBefore;
			this.numberChannels = serviceCall.getChannels();		
			
		}

		
		/**
		 * Starts the thread which will basically do the service call and get a new context of streams.
		 */
		public synchronized void run() {
			
			//Calls the service and gets a new service response
			ServiceResponse newServiceResponse = null;
			try{
				newServiceResponse = ProxyDriverImpl.this.gateway.callService(ProxyDriverImpl.this.provider,
						this.serviceCall);				
			}catch(ServiceCallException e){
				logger.severe(e.getMessage());
			}
			
			//If the service type is stream, redirect the streams
			if(this.serviceCall.getServiceType().equals(ServiceCall.ServiceType.STREAM) ){
				
				//Gives some time to establish the streams correctly
				try {
					Thread.sleep(TIME_TO_MAKE_THE_CONTEXT);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				//Gets the new message context of streams
				this.messageContextAfter = this.messageContextBefore;
				//And redirect the new one with old one
				try {
					redirectStreams();
				} catch (ConnectivityException e) {
					logger.log(Level.SEVERE,"Error running.",e);
					this.serviceResponse.setError("Error during proxying. Cause:"+e.getMessage());
					
					//Lets the upper thread knows we have finished our work
					ProxyDriverImpl.this.doneServiceCall = true;
					logger.fine("The proxyied service call has been done.");
					notify();
					return;
				}
			}
			
			//Puts the received data into the original serviceResponse
			this.serviceResponse.setResponseData(newServiceResponse.getResponseData());
			
			//Lets the upper thread knows we have finished our work
			ProxyDriverImpl.this.doneServiceCall = true;
			logger.fine("The proxyied service call has been done.");
			notify();
			
		}
		
		
		/**
		 * Redirects the content of the given input to the given output
		 */
		private void redirectStreams() throws ConnectivityException{
			
			//Starts two threads for each channel
			for( int i = 0 ; i < this.numberChannels ; i++ ){
				
				DataInputStream input = this.messageContextBefore.getDataInputStream(i);
				DataOutputStream output = this.messageContextAfter.getDataOutputStream(i);
				Thread stream = new RedirectStream(input, output);
				stream.start();
				
				output = this.messageContextBefore.getDataOutputStream(i);
				input = this.messageContextAfter.getDataInputStream(i);
				stream = new RedirectStream(input, output);
				stream.start();
				
			}
			
		}
		
		
		
		/**
		 * Inner thread class responsible for redirecting the streams
		 * @author Lucas Paranhos Quintella
		 *
		 */
		private class RedirectStream extends Thread{
			
			/** The input stream */
			DataInputStream input;
			
			/** The output stream */
			DataOutputStream output;
			
			/**
			 * Constructor.
			 * @param input The input for reading data.
			 * @param output The output for writing data.
			 */
			public RedirectStream(DataInputStream input, DataOutputStream output) throws ConnectivityException{
				if(input == null || output == null ){
					logger.severe("Constructor: Input or output is null");
					throw new ConnectivityException("Problem getting the message context");
				}
				this.input = input;
				this.output = output;
			}
			
			/**
			 * Starts the thread. While it does have content on the input, writes it on the output.
			 */
			public void run(){
			
				//Creates the reader and writer for the input and output streams
	            BufferedReader reader = new BufferedReader(new InputStreamReader(this.input));
	            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(this.output));
	            
	            //Do it until some exception is caught
		        while(true){    
		            try{
			            if(reader.ready()){
		                	int available = this.input.available();
		                	
		                	StringBuilder builder = new StringBuilder();
		                	for(int i = 0; i < available; i++){
		                       	builder.append((char)reader.read());
		                    }
		                	
		                	writer.write(builder.toString());
				            writer.flush();
		                }
		            }catch(Exception e){
		            	//Exception caught. The streams might have been closed. Terminates the thread.
		            	break;
		            }
		        }
						
			}
			
		}
		
		
	}

	@Override
	public List<UpDriver> getParent() {
		return null; //TODO: [B&M] Tem que retornar o Pai do driver ou o pai do proxy = null?
	}
	

}
