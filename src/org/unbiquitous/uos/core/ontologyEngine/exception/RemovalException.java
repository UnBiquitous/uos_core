/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unbiquitous.uos.core.ontologyEngine.exception;

/**
 *
 * @author anaozaki
 */
public class RemovalException extends Exception {
	private static final long serialVersionUID = -1515850575662378936L;

	public RemovalException() {
        super();
    }

    public RemovalException(String message, Throwable cause) {
        super(message, cause);
    }

    public RemovalException(String message) {
        super(message);
    }

    public RemovalException(Throwable cause) {
        super(cause);
    }
}
