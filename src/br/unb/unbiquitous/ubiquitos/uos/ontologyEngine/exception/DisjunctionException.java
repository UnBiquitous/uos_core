/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unb.unbiquitous.ubiquitos.uos.ontologyEngine.exception;

/**
 *
 * @author anaozaki
 */
public class DisjunctionException extends Exception {
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
