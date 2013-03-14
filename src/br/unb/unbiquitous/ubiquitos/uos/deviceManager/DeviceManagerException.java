package br.unb.unbiquitous.ubiquitos.uos.deviceManager;

/**
 * Class that encapsulate error from the device manager class.
 * 
 * @author Fabricio Nogueira Buzeto
 *
 */
public class DeviceManagerException extends Exception {

	private static final long serialVersionUID = -2051552295405722322L;

	public DeviceManagerException() {}

	public DeviceManagerException(String arg0) {
		super(arg0);
	}

	public DeviceManagerException(Throwable arg0) {
		super(arg0);
	}

	public DeviceManagerException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

}
