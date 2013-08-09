package org.unbiquitous.uos.core.driverManager.drivers;

import org.unbiquitous.uos.core.driverManager.UosEventDriver;

public interface Keyboard extends UosEventDriver {

	static final String DRIVER_NAME = "keyboard";
    static final String KEYBOARD_EVENT = "keyboard_event";
    static final String MESSAGE = "message";
    
    /**
     * Send a character.
     * 
     * Ex.: A-Z, a-z, 0-9, <, >, !, @, #, $, %, ^, &, *, (, ), etc.
     * 
     * @param character Character to send.
     */
    void charTyped(String character);
	
    /**
     * Send a command.
     * 
     * Ex.: shift, ctrl, alt, etc.
     * 
     * @param command Command to send.
     */
    void commandTyped(String command);
}
