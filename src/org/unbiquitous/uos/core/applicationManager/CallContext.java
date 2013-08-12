package org.unbiquitous.uos.core.applicationManager;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.unbiquitous.uos.core.network.exceptions.NetworkException;
import org.unbiquitous.uos.core.network.model.NetworkDevice;


/**
 * 
 * Class Responsible for store information about the current context of request in the application.
 * 
 * @author Fabricio Nogueira Buzeto
 *
 */
public class CallContext {
	
	/**
	 * Device Object representing the caller device of the request. 
	 */
	private NetworkDevice callerDevice;
	
	private List<DataInputStream> dataInputStream;
	
	private List<DataOutputStream> dataOutputStream;

	/**
	 * This constructor is defined with a protected visibility level so only it's controller can 
	 * instantiate it.
	 */
	public CallContext() {
		this.dataInputStream = new ArrayList<DataInputStream>();
		this.dataOutputStream = new ArrayList<DataOutputStream>();
	}

	/* ************************************************************************************** *
	 * Property Acessors                                                                      *
	 * ************************************************************************************** */
	
	public NetworkDevice getCallerDevice() {
		return callerDevice;
	}
	
	public void setCallerDevice(NetworkDevice callerDevice) {
		this.callerDevice = callerDevice;
	}
	
	public DataInputStream getDataInputStream() {
		if(!dataInputStream.isEmpty()){
			return dataInputStream.get(0);
		}
		return null;
	}

	public DataInputStream getDataInputStream(int index) {
		if(dataInputStream.size() > index){
			return dataInputStream.get(index);
		}
		return null;
	}
	
	public DataOutputStream getDataOutputStream() {
		if(!dataOutputStream.isEmpty()){
			return dataOutputStream.get(0);
		}
		return null;
	}
	
	public DataOutputStream getDataOutputStream(int index) {
		if(dataOutputStream.size() > index){
			return dataOutputStream.get(index);
		}
		return null;
	}
	
	public synchronized void addDataStreams(DataInputStream dataInputStream,DataOutputStream dataOutputStream) throws NetworkException{
		if(dataInputStream == null || dataOutputStream == null){
			throw new NetworkException("DataInputStream and DataOutputStream can not be null");
		}
		this.dataInputStream.add(dataInputStream);
		this.dataOutputStream.add(dataOutputStream);
	}
	
}
