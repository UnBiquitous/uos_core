package org.unbiquitous.uos.core.driverManager.drivers;

import org.unbiquitous.uos.core.driverManager.UosEventDriver;

public interface Pointer extends UosEventDriver{

	static final String MOVE_EVENT = "move_event";
	static final String AXIS_X = "axis_x";
	static final String AXIS_Y = "axis_y";
	static final String DRIVER_NAME = "pointer";
	
	/**
	 * Move the pointer.	
	 * 
	 * @param axisX The distance in X axis.
	 * @param axisY The distance in Y axis.
	 */
	public void move(int axisX, int axisY);
	
}
