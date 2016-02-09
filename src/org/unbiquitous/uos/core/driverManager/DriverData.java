package org.unbiquitous.uos.core.driverManager;

import org.unbiquitous.uos.core.messageEngine.dataType.UpDevice;
import org.unbiquitous.uos.core.messageEngine.dataType.UpDriver;

import static org.unbiquitous.uos.core.ClassLoaderUtils.chainHashCode;
import static org.unbiquitous.uos.core.ClassLoaderUtils.compare;

/**
 * Entity Responsible for handling the data about Drivers and its services on devices
 *
 * @author Fabricio Nogueira Buzeto
 */
public class DriverData {
    private String instanceID;
    private UpDriver driver;
    private UpDevice device;

    public DriverData(UpDriver driver, UpDevice device, String instanceID) {
        this.driver = driver;
        this.device = device;
        this.instanceID = instanceID;
    }

    /**
     * @return the instanceID
     */
    public String getInstanceID() {
        return instanceID;
    }

    /**
     * @return the driver
     */
    public UpDriver getDriver() {
        return driver;
    }

    /**
     * @return the device
     */
    public UpDevice getDevice() {
        return device;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof DriverData))
            return false;

        DriverData other = (DriverData) obj;
        return compare(driver, other.getDriver())
                && compare(instanceID, other.getInstanceID())
                && compare(device, other.getDevice());
    }

    @Override
    public int hashCode() {
        int hash = chainHashCode(super.hashCode(), this.driver);
        hash = chainHashCode(hash, this.instanceID);
        hash = chainHashCode(hash, this.device);
        return hash;
    }
}
