/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unb.unbiquitous.ubiquitos.uos.ontologyEngine.exception;

/**
 *
 * @author anaozaki
 */
public class ReasonerNotDefinedException extends Exception {
    public ReasonerNotDefinedException() {
		super();
	}

	public ReasonerNotDefinedException(String message, Throwable cause) {
		super(message, cause);
	}

	public ReasonerNotDefinedException(String message) {
		super(message);
	}

	public ReasonerNotDefinedException(Throwable cause) {
		super(cause);
	}
}
