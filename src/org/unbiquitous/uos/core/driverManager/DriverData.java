package org.unbiquitous.uos.core.driverManager;

import org.unbiquitous.uos.core.messageEngine.dataType.UpDevice;
import org.unbiquitous.uos.core.messageEngine.dataType.UpDriver;

/**
 * Entity Responsible for handling the data about Drivers and its services on devices
 * 
 * @author Fabricio Nogueira Buzeto
 *
 */
public class DriverData {
	private String instanceID;
	private UpDriver driver;
	private UpDevice device; 
	
	public DriverData(UpDriver driver, UpDevice device,	String instanceID) {
		this.driver = driver;
		this.device = device;
		this.instanceID = instanceID;
	}

	public boolean equals(Object obj) {
		if (obj != null && obj instanceof DriverData){
			DriverData temp = (DriverData)obj;
			if (temp.driver != null && 
					temp.device != null && 
					temp.instanceID != null){
				return temp.driver.equals(this.driver) && 
						temp.device.equals(this.device) &&
						temp.instanceID.equals(this.instanceID);
			}
		}
		return false;
	}

	/**
	 * @return the instanceID
	 */
	public String getInstanceID() {
		return instanceID;
	}

	/**
	 * @return the driver
	 */
	public UpDriver getDriver() {
		return driver;
	}

	/**
	 * @return the device
	 */
	public UpDevice getDevice() {
		return device;
	}

}
