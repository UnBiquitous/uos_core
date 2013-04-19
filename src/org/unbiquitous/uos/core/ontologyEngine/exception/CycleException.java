/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unbiquitous.uos.core.ontologyEngine.exception;

/**
 *
 * @author anaozaki
 */
public class CycleException extends Exception {
	private static final long serialVersionUID = -1163152457897404794L;

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
