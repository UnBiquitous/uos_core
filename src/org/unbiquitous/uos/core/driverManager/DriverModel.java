package org.unbiquitous.uos.core.driverManager;

import static java.lang.String.*;
import org.unbiquitous.uos.core.messageEngine.dataType.UpDriver;

public class DriverModel {

	public static String TABLE = "DRIVER";
	public static String ROW_ID = "row_id";
	public static String ID = "id";
	public static String NAME = "name";
	public static String DEVICE = "device";
	
	private String id;
	private UpDriver driver;
	private String device;
	
	public DriverModel(String id, UpDriver driver, String device) {
		this.id = id;
		this.driver = driver;
		this.device = device;
	}
	
	public String id() {
		return id;
	}

	public UpDriver driver() {
		return driver;
	}
	
	public String device() {
		return device;
	}

	public String rowid() {
		return format("%s@%s",id,device);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null || ! (obj instanceof DriverModel) ){
			return false;
		}
		
		DriverModel d = (DriverModel) obj;
		
		return this.driver.equals(d.driver) && this.device.equals(d.device) && this.id.equals(d.id);
	}

}
