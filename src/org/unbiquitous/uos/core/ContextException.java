package org.unbiquitous.uos.core;

public class ContextException extends RuntimeException {

	private static final long serialVersionUID = 8699837559689550437L;

	public ContextException() {
	}

	public ContextException(String arg0) {
		super(arg0);
	}

	public ContextException(Throwable arg0) {
		super(arg0);
	}

	public ContextException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

}
