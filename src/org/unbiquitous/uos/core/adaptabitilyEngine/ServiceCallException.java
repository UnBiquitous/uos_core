package org.unbiquitous.uos.core.adaptabitilyEngine;

/**
 * Class representing errors in the process of calling a service.
 * 
 * @author Fabricio Nogueira Buzeto
 *
 */
public class ServiceCallException extends Exception {

	private static final long serialVersionUID = 1717939615887225653L;
	
	public ServiceCallException() {
		super();
	}

	public ServiceCallException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public ServiceCallException(String arg0) {
		super(arg0);
	}

	public ServiceCallException(Throwable arg0) {
		super(arg0);
	}


}
