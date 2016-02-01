package org.unbiquitous.uos.core.deviceManager;

import org.unbiquitous.uos.core.messageEngine.dataType.UpDevice;

/**
 * Interested listeners must implement this interface to be notified by the device manager of device registration
 * related events.
 *
 * @author Luciano Santos
 */
public interface DeviceListener {
    /**
     * Called when a device is successfully registered in the device manager.
     *
     * @param device A reference to the device has just entered the smart-space.
     */
    void deviceRegistered(UpDevice device);

    /**
     * Called when a previously knwon device is unregistered in the device manager.
     *
     * @param device A reference to the device has just left the smart-space.
     */
    void deviceUnregistered(UpDevice device);
}
