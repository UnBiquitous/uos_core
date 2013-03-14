package br.unb.unbiquitous.ubiquitos.uos.adaptabitilyEngine;

import br.unb.unbiquitous.ubiquitos.uos.messageEngine.messages.Notify;

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
