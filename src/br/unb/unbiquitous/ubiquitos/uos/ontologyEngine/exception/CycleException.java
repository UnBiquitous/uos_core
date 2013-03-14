/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unb.unbiquitous.ubiquitos.uos.ontologyEngine.exception;

/**
 *
 * @author anaozaki
 */
public class CycleException extends Exception {
        public CycleException() {
		super();
	}

	public CycleException(String message, Throwable cause) {
		super(message, cause);
	}

	public CycleException(String message) {
		super(message);
	}

	public CycleException(Throwable cause) {
		super(cause);
	}
}
