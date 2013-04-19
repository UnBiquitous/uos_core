/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unbiquitous.uos.core.ontologyEngine.exception;

/**
 * 
 * @author anaozaki
 */
public class DisjunctionException extends Exception {
	private static final long serialVersionUID = -441149317808260191L;

	public DisjunctionException() {
		super();
	}

	public DisjunctionException(String message, Throwable cause) {
		super(message, cause);
	}

	public DisjunctionException(String message) {
		super(message);
	}

	public DisjunctionException(Throwable cause) {
		super(cause);
	}
}
