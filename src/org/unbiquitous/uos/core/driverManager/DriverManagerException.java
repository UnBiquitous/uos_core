package org.unbiquitous.uos.core.driverManager;

/**
 * Class that encapsulate error from the driver manager class.
 * 
 * @author Fabricio Nogueira Buzeto
 *
 */
public class DriverManagerException extends Exception {

	private static final long serialVersionUID = -1671602839178257084L;

	public DriverManagerException() {
	}

	public DriverManagerException(String arg0) {
		super(arg0);
	}

	public DriverManagerException(Throwable arg0) {
		super(arg0);
	}

	public DriverManagerException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

}
