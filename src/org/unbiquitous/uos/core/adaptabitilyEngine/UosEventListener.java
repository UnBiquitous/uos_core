package org.unbiquitous.uos.core.adaptabitilyEngine;

import org.unbiquitous.uos.core.messageEngine.messages.Notify;

/**
 * Interface representing applications that are able to receive event notifications through Notify Messages.
 * 
 * @author Fabricio Nogueira Buzeto
 *
 */
public interface UosEventListener {

	/**
	 * Method responsible for dealing with the event received.
	 * 
	 * @param event Notify message representing the event received.
	 */
	public void handleEvent(Notify event);
}
