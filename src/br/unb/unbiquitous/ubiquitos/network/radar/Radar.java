package br.unb.unbiquitous.ubiquitos.network.radar;

import br.unb.unbiquitous.ubiquitos.network.connectionManager.ConnectionManager;

/**
 * This Defines the interface of a Radar, describing all essential methods.
 * Every Radar from the UbiquitOS must implement this
 * 
 * @author Passarinho
 *
 */
public interface Radar extends Runnable {

	/**
	 * Start the devices discovery of the radar
	 */
	public void startRadar();
	
	/**
	 * Stop the devices discovery of the radar
	 */
	public void stopRadar();
	
	/**
	 * Sets the connection manager responsible for the interaction of this radar.
	 * 
	 */
	public void setConnectionManager(ConnectionManager connectionManager);
	
}
