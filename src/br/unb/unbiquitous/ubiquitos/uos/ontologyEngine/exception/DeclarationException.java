/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unb.unbiquitous.ubiquitos.uos.ontologyEngine.exception;

/**
 *
 * @author anaozaki
 */
public class DeclarationException extends Exception {
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
