/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unbiquitous.uos.core.ontologyEngine.exception;

/**
 * 
 * @author anaozaki
 */
public class DeclarationException extends Exception {
	private static final long serialVersionUID = 542203298479248754L;

	public DeclarationException() {
		super();
	}

	public DeclarationException(String message, Throwable cause) {
		super(message, cause);
	}

	public DeclarationException(String message) {
		super(message);
	}

	public DeclarationException(Throwable cause) {
		super(cause);
	}
}
