package org.unbiquitous.uos.core.deviceManager;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.unbiquitous.uos.core.UOSLogging;
import org.unbiquitous.uos.core.adaptabitilyEngine.Gateway;
import org.unbiquitous.uos.core.adaptabitilyEngine.ServiceCallException;
import org.unbiquitous.uos.core.connectivity.ConnectivityManager;
import org.unbiquitous.uos.core.driverManager.*;
import org.unbiquitous.uos.core.messageEngine.dataType.UpDevice;
import org.unbiquitous.uos.core.messageEngine.dataType.UpDriver;
import org.unbiquitous.uos.core.messageEngine.dataType.UpNetworkInterface;
import org.unbiquitous.uos.core.messageEngine.messages.Call;
import org.unbiquitous.uos.core.messageEngine.messages.Response;
import org.unbiquitous.uos.core.network.connectionManager.ConnectionManagerControlCenter;
import org.unbiquitous.uos.core.network.model.NetworkDevice;
import org.unbiquitous.uos.core.network.radar.RadarListener;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class responsible for managing the devices in the neighborhood of the current
 * device.
 *
 * @author Fabricio Nogueira Buzeto
 */
public class DeviceManager implements RadarListener {

    private static final String DEVICE_DRIVER_NAME = "uos.DeviceDriver";

    private static final String DRIVERS_NAME_KEY = "driversName";

    private static final String INTERFACES_KEY = "interfaces";

    private static final Logger logger = UOSLogging.getLogger();
    private static final ObjectMapper mapper = new ObjectMapper();

    private Gateway gateway;

    private ConnectionManagerControlCenter connectionManagerControlCenter;

    private DeviceDao deviceDao;

    private UpDevice currentDevice;

    private ConnectivityManager connectivityManager;

    private DriverManager driverManager;

    private Set<String> unknownDrivers;

    private Set<DriverModel> dependents;

    private Set<DeviceListener> deviceListeners;

    public DeviceManager(UpDevice currentDevice, DeviceDao deviceDao, DriverDao driverDao,
                         ConnectionManagerControlCenter connectionManagerControlCenter, ConnectivityManager connectivityManager,
                         Gateway gateway, DriverManager driverManager) {
        this.gateway = gateway;
        this.connectionManagerControlCenter = connectionManagerControlCenter;
        this.currentDevice = currentDevice;
        this.connectivityManager = connectivityManager;
        this.deviceDao = deviceDao;
        this.deviceDao.save(currentDevice);
        this.driverManager = driverManager;
        this.unknownDrivers = new HashSet<String>();
        this.dependents = new HashSet<DriverModel>();
        this.deviceListeners = new HashSet<DeviceListener>();
    }

    /**
     * Method responsible for registering a device in the neighborhood of the
     * current device.
     *
     * @param device Device to be registered.
     */
    public void registerDevice(UpDevice device) {
        deviceDao.save(device);
    }

    /**
     * Method responsible for finding the data about a device present in the
     * neighborhood.
     *
     * @param deviceName Device name to be found.
     * @return <code>UpDevice</code> with the data about the informed device.
     */
    public UpDevice retrieveDevice(String deviceName) {
        return deviceDao.find(deviceName);
    }

    /**
     * Method responsible for finding the data about a device present in the
     * neighborhood.
     *
     * @param networkAddress Address of the Device to be found.
     * @param networkType    NetworkType of Address of the Device to be found.
     * @return <code>UpDevice</code> with the data about the informed device.
     */
    public UpDevice retrieveDevice(String networkAddress, String networkType) {
        List<UpDevice> list = deviceDao.list(networkAddress, networkType);
        if (list != null && !list.isEmpty()) {
            UpDevice deviceFound = list.get(0);
            logger.fine("Device with addr '" + networkAddress + "' found on network '" + networkType + "' resolved to "
                    + deviceFound);
            return deviceFound;
        }
        logger.fine("No device found with addr '" + networkAddress + "' on network '" + networkType + "'.");
        return null;
    }

    /**
     * @see org.unbiquitous.uos.core.network.radar.RadarListener#deviceEntered(org.unbiquitous.uos.core.network.model.NetworkDevice)
     */
    @Override
    public void deviceEntered(NetworkDevice device) {
        if (device == null)
            return;

        // verify if device entered is the current device
        String deviceHost = connectionManagerControlCenter.getHost(device.getNetworkDeviceName());
        for (UpNetworkInterface networkInterface : this.currentDevice.getNetworks()) {
            String currentDeviceHost = connectionManagerControlCenter.getHost(networkInterface.getNetworkAddress());
            if (deviceHost != null && deviceHost.equals(currentDeviceHost)) {
                logger.fine("Host of device entered is the same of current device:" + device.getNetworkDeviceName());
                return;
            }
        }

        // verify if already know this device.
        UpDevice upDevice = retrieveDevice(deviceHost, device.getNetworkDeviceType());

        if (upDevice == null) {
            upDevice = doHandshake(device, upDevice);
            if (upDevice != null) {
                doDriversRegistry(device, upDevice);
                fireDeviceEvent(upDevice, DeviceEventType.ENTERED);
            }
        } else {
            logger.fine("Already known device " + device.getNetworkDeviceName());
        }
    }

    private void doDriversRegistry(NetworkDevice device, UpDevice upDevice) {
        try {
            Response response = gateway.callService(upDevice, new Call(DEVICE_DRIVER_NAME, "listDrivers"));
            if (response != null && response.getResponseData() != null && response.getResponseData("driverList") != null) {
                try {
                    JavaType mapType = mapper.getTypeFactory().constructParametrizedType(Map.class, Map.class, String.class, UpDriver.class);
                    Map<String, UpDriver> driverList = mapper.convertValue(response.getResponseData("driverList"), mapType);
                    registerRemoteDriverInstances(upDevice, driverList);
                } catch (IOException e) {
                    logger.log(Level.SEVERE, "Problems ocurred in the registering of drivers from device '" + upDevice.getName() + "' .", e);
                }
            }
        } catch (Exception e) {
            logger.severe("Not possible to discover services from device '" + device.getNetworkDeviceName() + "'. Possibly not a uOS Device");
        }
    }

    private void registerRemoteDriverInstances(UpDevice upDevice, Map<String, UpDriver> driversListMap) throws IOException {
        for (Map.Entry<String, UpDriver> entry : driversListMap.entrySet()) {
            String id = entry.getKey();
            UpDriver upDriver = entry.getValue();
            DriverModel driverModel = new DriverModel(id, upDriver, upDevice.getName());
            try {
                driverManager.insert(driverModel);
                // TODO : DeviceManager : Save the device information
                if (this.connectivityManager.doProxying()) {
                    this.connectivityManager.registerProxyDriver(upDriver, upDevice, id);
                }
            } catch (DriverManagerException e) {
                logger.log(Level.SEVERE,
                        "Problems ocurred in the registering of driver '" + upDriver.getName() + "' with instanceId '"
                                + id + "' in the device '" + upDevice.getName() + "' and it will not be registered.",
                        e);
            } catch (DriverNotFoundException e) {
                unknownDrivers.addAll(e.getDriversName());
                dependents.add(driverModel);

            } catch (RuntimeException e) {
                logger.log(Level.SEVERE,
                        "Problems ocurred in the registering of driver '" + upDriver.getName() + "' with instanceId '"
                                + id + "' in the device '" + upDevice.getName() + "' and it will not be registered.",
                        e);
            }
        }
        if (unknownDrivers.size() > 0)
            findDrivers(unknownDrivers, upDevice);
    }

    /**
     * @param upDevice
     * @param e
     * @return
     * @throws JSONException
     * @throws ServiceCallException
     */
    private void findDrivers(Set<String> unknownDrivers, UpDevice upDevice) throws IOException {
        Call call = new Call(DEVICE_DRIVER_NAME, "tellEquivalentDrivers", null);
        call.addParameter(DRIVERS_NAME_KEY, new ArrayList<String>(unknownDrivers));

        try {
            Response equivalentDriverResponse = gateway.callService(upDevice, call);

            if (equivalentDriverResponse != null && (equivalentDriverResponse.getError() == null || equivalentDriverResponse.getError().isEmpty())) {
                Object interfaces = equivalentDriverResponse.getResponseData(INTERFACES_KEY);
                if (interfaces != null) {
                    JavaType listType = mapper.getTypeFactory().constructParametrizedType(List.class, List.class, UpDriver.class);
                    List<UpDriver> drivers = mapper.convertValue(interfaces, listType);

                    try {
                        driverManager.addToEquivalenceTree(drivers);
                    } catch (InterfaceValidationException e) {
                        logger.severe("Not possible to add to equivalance tree due to wrong interface specification.");
                    }

                    for (DriverModel dependent : dependents) {
                        try {
                            driverManager.insert(dependent);
                        } catch (DriverManagerException e) {
                            logger.log(Level.SEVERE,
                                    "Problems ocurred in the registering of driver '" + dependent.driver().getName()
                                            + "' with instanceId '" + dependent.id() + "' in the device '"
                                            + upDevice.getName() + "' and it will not be registered.",
                                    e
                            );
                        } catch (DriverNotFoundException e) {
                            logger.severe("Not possible to register driver '" + dependent.driver().getName() + "' due to unkwnown equivalent driver.");
                        }
                    }

                } else {
                    logger.severe("Not possible to call service on device '" + upDevice.getName() + "' for no equivalent drivers on the service response.");
                }
            } else {
                logger.severe("Not possible to call service on device '" + upDevice.getName() + (equivalentDriverResponse == null ?
                        ": null"
                        : "': Cause : " + equivalentDriverResponse.getError())
                );
            }
        } catch (ServiceCallException e) {
            logger.severe("Not possible to call service on device '" + upDevice.getName());
        }
    }

    @SuppressWarnings("rawtypes")
    private UpDevice doHandshake(NetworkDevice device, UpDevice upDevice) {
        try {
            // Create a Dummy device just for calling it
            logger.fine("Trying to hanshake with device : " + device.getNetworkDeviceName());
            UpDevice dummyDevice = new UpDevice(device.getNetworkDeviceName())
                    .addNetworkInterface(device.getNetworkDeviceName(), device.getNetworkDeviceType());

            Call call = new Call(DEVICE_DRIVER_NAME, "handshake", null);
            call.addParameter("device", currentDevice);

            Response response = gateway.callService(dummyDevice, call);
            if (response != null && (response.getError() == null || response.getError().isEmpty())) {
                // in case of a success greeting process, register the device in
                // the neighborhood database
                Object responseDevice = response.getResponseData("device");
                if (responseDevice != null) {
                    UpDevice remoteDevice = (responseDevice instanceof String) ?
                        mapper.readValue((String) responseDevice, UpDevice.class) : mapper.convertValue(responseDevice, UpDevice.class);
                    registerDevice(remoteDevice);
                    logger.info("Registered device " + remoteDevice.getName());
                    return remoteDevice;
                } else {
                    logger.severe("Not possible complete handshake with device '" + device.getNetworkDeviceName()
                            + "' for no device on the handshake response.");
                }
            } else {
                logger.severe("Not possible to handshake with device '" + device.getNetworkDeviceName()
                        + (response == null ? ": No Response received." : "': Cause : " + response.getError()));
            }
        } catch (Exception e) {
            logger.severe(
                    "Not possible to handshake with device '" + device.getNetworkDeviceName() + "'. " + e.getMessage());
        }
        return upDevice;
    }

    /**
     * @see org.unbiquitous.uos.core.network.radar.RadarListener#deviceLeft(org.unbiquitous.uos.core.network.model.NetworkDevice)
     */
    @Override
    public void deviceLeft(NetworkDevice device) {
        if (device == null || device.getNetworkDeviceName() == null || device.getNetworkDeviceType() == null)
            return;
        // Remove what services this device has.
        logger.info(
                "Device " + device.getNetworkDeviceName() + " of type " + device.getNetworkDeviceType() + " leaving.");
        String host = connectionManagerControlCenter.getHost(device.getNetworkDeviceName());
        List<UpDevice> devices = deviceDao.list(host, device.getNetworkDeviceType());

        if (devices != null && !devices.isEmpty()) {
            UpDevice upDevice = devices.get(0);
            List<DriverModel> returnedDrivers = driverManager.list(null, upDevice.getName());
            if (returnedDrivers != null && !returnedDrivers.isEmpty()) {
                for (DriverModel rdd : returnedDrivers) {
                    driverManager.delete(rdd.id(), rdd.device());
                }
            }
            deviceDao.delete(upDevice.getName());
            fireDeviceEvent(upDevice, DeviceEventType.LEFT);
            logger.info(String.format("Device '%s' left", upDevice.getName()));
        } else {
            logger.info("Device not found in database.");
        }
    }

    public List<UpDevice> listDevices() {
        return deviceDao.list();
    }

    public DeviceDao getDeviceDao() {
        return deviceDao;
    }

    /**
     * Adds a new listener to the device manager, that will be notified whenever a previously unknown device is
     * successfully registered or a previously registered device is unregistered due to leaving the smartspace.
     *
     * @param listener the new listener to be added.
     * @throws NullPointerException if listener is null.
     */
    public void addDeviceListener(DeviceListener listener) {
        if (listener == null)
            throw new NullPointerException("listener");
        deviceListeners.add(listener);
    }

    /**
     * Removes a previously added listener. If listener is null or was not previously added,
     * nothing happens.
     *
     * @param listener the listener to be removed.
     */
    public void removeDeviceListener(DeviceListener listener) {
        deviceListeners.remove(listener);
    }

    private enum DeviceEventType {ENTERED, LEFT}

    private void fireDeviceEvent(UpDevice device, DeviceEventType type) {
        try {
            Method m = ((type == DeviceEventType.ENTERED) ?
                    DeviceListener.class.getMethod("deviceRegistered", UpDevice.class) :
                    DeviceListener.class.getMethod("deviceUnregistered", UpDevice.class));
            for (DeviceListener l : deviceListeners)
                try {
                    m.invoke(l, device);
                } catch (Throwable t) {
                    logger.log(Level.WARNING, "Failed to notify device listener of event.", t);
                }
        } catch (NoSuchMethodException e) {
            logger.log(Level.SEVERE, "Failed to load DeviceListener methods.", e);
        }
    }
}
