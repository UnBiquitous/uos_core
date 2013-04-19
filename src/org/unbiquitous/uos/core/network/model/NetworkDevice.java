package org.unbiquitous.uos.core.network.model;


/**
 * This asbtract class describes a abstraction of a device found in the smart-space by a radar(Bluetooth, Ethernet, etc.)
 *  It defines the generic methods to access the found device.
 * @author Passarinho
 */
public abstract class NetworkDevice {
	
	/**
	 * Returns the name of the device.
	 * @return String
	 */
	public abstract String getNetworkDeviceName();
	
	/**
	 * Returns a constant String that identifies the type of the device.
	 * (e.g "Bluetooth Device", "Ethernet Device", etc.)
	 * @return String
	 */
	public abstract String getNetworkDeviceType();	
	
}
