package org.unbiquitous.uos.core.messageEngine;

import org.unbiquitous.uos.core.network.exceptions.NetworkException;

/**
 * Class that encapsulate error from the Message Engine class.
 * 
 * @author Fabricio Nogueira Buzeto
 *
 */
public class MessageEngineException extends NetworkException {
	//TODO: [B&M] Ao enviar varias mensagens ta dando excecao por ter muitos arquivos abertos
	private static final long serialVersionUID = -7370295060218141745L;

	public MessageEngineException() {
		printStackTrace();
	}

	public MessageEngineException(String arg0) {
		super(arg0);
		printStackTrace();
	}

	public MessageEngineException(Throwable arg0) {
		super(arg0);
		printStackTrace();
	}

	public MessageEngineException(String arg0, Throwable arg1) {
		super(arg0, arg1);
		printStackTrace();
	}

	
}
