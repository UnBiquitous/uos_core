package org.unbiquitous.uos.core.driverManager.drivers;


public interface Clickable extends Pointer {
	
	static final String BUTTON_PRESSED_EVENT = "button_pressed_event";
	static final String BUTTON_RELEASED_EVENT = "button_released_event";
	static final String BUTTON = "button";
	static final String DRIVER_NAME = "clickable";
	static final int BUTTON_LEFT = 1;
	static final int BUTTON_RIGHT = 2;
	
	
	/**
	 * Button pressed.
	 */
	public void buttonPressed(int button);
	
	/**
	 * Button released.
	 */
	public void buttonReleased(int button);
	
	
}
