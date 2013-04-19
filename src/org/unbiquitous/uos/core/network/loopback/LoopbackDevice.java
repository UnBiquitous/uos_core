package org.unbiquitous.uos.core.network.loopback;

import org.unbiquitous.uos.core.network.model.NetworkDevice;

/**
 * 
 * This class represents the loopback network device. Each device has an unique ID which is to the
 * loopback context what the port is for a TCP/UDP connection.
 * 
 * @author Lucas Paranhos Quintella
 *
 */
public class LoopbackDevice extends NetworkDevice{
	
	/** The loopback network device type */
	private static final String NETWORK_DEVICE_TYPE = "Loopback";
	
	/** A counter for creating unique device's IDs*/
	private static long idCounter = 1;
	
	/** The device name is the same for all devices, cause it is loopback */
	protected final String deviceName = "This Device";
	
	/** ID of the network interface, or device */
	private long id;
	
	/**
	 * Initializes the class for providing correct IDs
	 */
	public static void initDevicesID(){ 
		idCounter = 1; 
	}

	/**
	 * Instantiates a new LoopbackDevice with an unique ID.
	 */
	public LoopbackDevice(){
		this.id = idCounter++;
	}
	
	/**
	 * Instantiates a new LoopbackDevice with the given ID. This method infers the user knows
	 * the given ID is a valid one.
	 * @param The ID of the device
	 */
	public LoopbackDevice(long id){
		this.id = id;
	}

	
	/**
	 * Getter of the device name
	 * @return The device name, which is in the format This Device:ID 
	 */
	public String getNetworkDeviceName() {
		return deviceName + ":" + id;
	}

	/**
	 * Getter of the device type
	 * @return The device type, which is Loopback
	 */
	public String getNetworkDeviceType() {
		return NETWORK_DEVICE_TYPE;
	}

	/**
	 * Getter of the device's ID.
	 * @return The ID of the device
	 */
	public long getDeviceId(){
		return id;
	}

}
