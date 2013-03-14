/*
 * RadarListener.java
 *
 * Created on August 1, 2006, 4:18 PM
 */

package br.unb.unbiquitous.ubiquitos.network.radar;

import br.unb.unbiquitous.ubiquitos.network.model.NetworkDevice;

/**
 * An interface to be implemented by every object interested in receive
 * UbiquitOS Radar notifications, like "a new device has entered the smart-space"
 * and "device X has left the smart-space".
 *
 * @author Passarinho
 */
public interface RadarListener {
    
    /**
     * A method called by the UbiquitOS Radar when a new device enter
     * the UbiquitOS smart-space.
     *
     * @param device A reference to the device has just entered the smart-space.
     */
    public void deviceEntered(NetworkDevice device) ;
    
    /**
     * A method called by the UbiquitOS Radar when a new device leaves
     * the UbiquitOS smart-space.
     *
     * @param device A reference to the device has just left the smart-space.
     */
    public void deviceLeft(NetworkDevice device) ;
}
