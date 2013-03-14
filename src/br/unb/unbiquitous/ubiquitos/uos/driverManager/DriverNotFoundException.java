package br.unb.unbiquitous.ubiquitos.uos.driverManager;

import java.util.Set;

public class DriverNotFoundException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 173927652294825702L;
	
	private Set<String> driversName;
	
	public DriverNotFoundException(String message, Set<String> driversName) {
		super(message);
		this.driversName = driversName;
	}
	
	public Set<String> getDriversName() {
		return this.driversName;
	}
	

}
