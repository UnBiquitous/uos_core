package br.unb.unbiquitous.ubiquitos.uos.driverManager;

import br.unb.unbiquitous.ubiquitos.uos.messageEngine.dataType.UpDriver;

public class DriverModel {

	public static String TABLE = "DRIVER";
	public static String ROW_ID = "row_id";
	public static String ID = "id";
	public static String NAME = "name";
	public static String DEVICE = "device";
	
	private Long rowid;
	private String id;
	private UpDriver driver;
	private String device;
	
	public DriverModel(String id, UpDriver driver, String device) {
		this(null,id,driver,device);
	}

	public DriverModel(Long rowid, String id, UpDriver driver, String device) {
		this.rowid = rowid;
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

	public Long rowid() {
		return rowid;
	}
	
	void rowid(Long rowid) {
		this.rowid = rowid;
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
