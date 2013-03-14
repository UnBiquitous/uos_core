package br.unb.unbiquitous.ubiquitos.uos.security;

/**
 * Class Responsible for representing the errors of security.
 * 
 * @author Fabricio Nogueira Buzeto
 *
 */
public class SecurityException extends Exception {

	private static final long serialVersionUID = -943126248605414558L;

	public SecurityException() {
	}

	public SecurityException(String arg0) {
		super(arg0);
	}

	public SecurityException(Throwable arg0) {
		super(arg0);
	}

	public SecurityException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

}
