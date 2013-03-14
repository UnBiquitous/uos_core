package br.unb.unbiquitous.ubiquitos.uos.driverManager.drivers;

public interface Scrollable extends Pointer {

	static final String SCROLL_EVENT = "scroll_event";
	static final String DISTANCE = "distance";
	static final String DRIVER_NAME = "scrollable";
	
	/**
	 * Mouse wheel.
	 * 
	 * @param distance
	 *            Number of "notches" to move the mouse wheel. Negative values
	 *            indicate movement up/away from the user, positive values
	 *            indicate movement down/towards the user.
	 */
	public void scroll(int distance);

}
